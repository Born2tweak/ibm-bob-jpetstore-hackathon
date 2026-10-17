# Verification record

## October 13, 2026 (EDT) — baseline smoke check

Run from `jpetstore/` in the team repository on branch `prep/jpetstore-bob`.

| Check | Exact command | Observed result |
| --- | --- | --- |
| Fresh build and tests | `.\mvnw.cmd clean package` | Exit 0, `BUILD SUCCESS`; Surefire: 98 tests, 0 failures, 0 errors, 0 skipped; WAR at `target/jpetstore.war`. |
| Local Tomcat | `.\mvnw.cmd cargo:run -P tomcat9` | Tomcat 9.0.120 launched on port 8080 and deployed `jpetstore.war`; Maven process must remain running. |
| HTTP smoke check | `Invoke-WebRequest -Uri 'http://localhost:8080/jpetstore/' -UseBasicParsing` | HTTP 200; page title `JPetStore Demo`; response contained `Catalog`. URL: http://localhost:8080/jpetstore/ |
| Import integrity | SHA-256 comparison of all 163 upstream tracked paths against imported counterparts after build | 0 mismatches. The upstream checkout remained unchanged. |

The build emitted OpenRewrite JSP XML parsing warnings and a Tomcat `emptySessionPath` property warning, but it exited successfully and the home page responded. This smoke check does not establish checkout behavior, repeated-confirmation behavior, browser interaction, or a public deployment. No candidate issue had been reproduced and no fix had been implemented at this point. There is no live judge URL yet.

To repeat: from `jpetstore/`, run `.\mvnw.cmd clean package`, then `.\mvnw.cmd cargo:run -P tomcat9` and open http://localhost:8080/jpetstore/. The WAR and Cargo files under `target/` are generated and ignored; do not commit them.

---

## October 14, 2026 (EDT) — duplicate-confirmation bug: reproduction and fix

All commands below were run from `jpetstore/` on branch `prep/jpetstore-bob`. At the time of these runs, the fix was not yet staged or committed.

### Regression gate — before fix (unfixed controller)

| Check | Exact command | Observed result |
| --- | --- | --- |
| Compilation of new IT files | `.\mvnw.cmd test-compile` | `BUILD SUCCESS`; `OrderControllerDuplicateOrderIT` and `OrderServiceTestConfig` compiled without errors. Pre-existing OpenRewrite JSP warnings present (unchanged). |
| Regression gate, unfixed controller | `.\mvnw.cmd failsafe:integration-test failsafe:verify -Dit.test=OrderControllerDuplicateOrderIT -Dcargo.skip=true` | `BUILD FAILURE`. Tests run: 2, Failures: **1**, Errors: 0, Skipped: 0. `replayConfirmPostInSameSessionMustNotInsertDuplicateOrder` **FAILED**: `AssertionFailedError: [Second confirm POST must NOT insert another order row; unfixed: count was 4 but should stay at 3] expected: 3 but was: 4` (line 202). `secondLegitimateOrderInNewSessionCycleIsAccepted` PASSED. |

The failure confirms the defect: a second `POST /order/new?confirmed=true` in the same session calls `insertOrder` a second time because `session["order"]` is never removed, producing a second `ORDERS` row and a second `LINEITEM` row and decrementing inventory twice.

### Fix applied

**File:** `jpetstore/src/main/java/org/mybatis/jpetstore/web/controllers/OrderController.java`
**Change:** One line added in `newOrder`, in the `confirmed && sessionOrder != null` branch, immediately after `orderService.insertOrder(sessionOrder)`:

```java
session.removeAttribute("order");
```

No other production source files were modified by this fix.

### Regression gate — after fix

| Check | Exact command | Observed result |
| --- | --- | --- |
| Regression gate, fixed controller | `.\mvnw.cmd test-compile failsafe:integration-test failsafe:verify -Dit.test=OrderControllerDuplicateOrderIT -Dcargo.skip=true` | `BUILD SUCCESS`. Tests run: 2, Failures: **0**, Errors: 0, Skipped: 0. Both `replayConfirmPostInSameSessionMustNotInsertDuplicateOrder` and `secondLegitimateOrderInNewSessionCycleIsAccepted` **PASSED**. |
| Full Surefire suite, fixed controller | `.\mvnw.cmd test` | `BUILD SUCCESS`; Surefire: **98 tests, 0 failures, 0 errors, 0 skipped**. No regressions introduced. |

### Diff summary

```
--- a/jpetstore/src/main/java/org/mybatis/jpetstore/web/controllers/OrderController.java
+++ b/jpetstore/src/main/java/org/mybatis/jpetstore/web/controllers/OrderController.java
@@ -146,6 +146,7 @@ public class OrderController {
     if (confirmed) {
       if (sessionOrder != null) {
         orderService.insertOrder(sessionOrder);
+        session.removeAttribute("order");
         session.setAttribute("cart", new Cart());
```

The production change is one line in one file. The accompanying regression test and test configuration are new files. This record and `SPEC.md` document the work; all five files are included in the fix commit.


---

## October 16, 2026 (EDT) — independent review and fresh execution attempt

Reviewed app revision: c6c2cfca405660d70760f0812cb536cf69624810, before the test changes in this audit. An independent read-only review confirmed the production change against import baseline 2844bec6 was exactly one line and correct for sequential replay after successful insertion.

Evidence precision: the historical red run stopped at the duplicate ORDERS assertion. It did not reach the later LINEITEM/inventory assertions. The successful fixed-code run checked all those assertions. These are direct-controller/database integration tests, not HTTP/browser tests.

The review found that the legitimate repeat-purchase check used a new session. The test now reuses the original session and asserts one additional order, one additional line item, and one additional inventory decrement. Stale test comments were corrected. No production code was changed by this audit.

Fresh local command attempted from jpetstore/:

~~~sh
sh mvnw -B test-compile failsafe:integration-test failsafe:verify -Dit.test=OrderControllerDuplicateOrderIT
~~~

Result: blocked before Maven startup by java.net.UnknownHostException: repo.maven.apache.org while downloading Maven. No tests executed, so this is not a fresh pass or a product-test failure.

A root .github/workflows/checkout-regression.yml workflow was prepared to package the app and run the full unit suite plus the two targeted regression tests, then check that removing only the production cleanup line causes the expected replay assertion failure. This sensitivity check changes source only inside its disposable CI runner. Run status must be observed before claiming that workflow passed.

The full browser journey, concurrent confirmation requests, and a live deployment remain unverified.


### Fresh GitHub CI results

Tested application/workflow revision: b2cec101e03fa6cd780b324f55b91429fa022821. [Run 38985945644](https://github.com/Born2tweak/ibmi-cobol-hackathon/actions/runs/38985945644), October 16 EDT / October 17 UTC.

The first CI attempt used JDK 17 and stopped at the inherited Enforcer's Java-version check before tests ran. The workflow was corrected to JDK 21 and setup-java v5; the application POM and production code were not changed.

| Check | Result |
| --- | --- |
| sh mvnw -B clean package failsafe:integration-test failsafe:verify -Dit.test=OrderControllerDuplicateOrderIT | BUILD SUCCESS; 98 Surefire tests, 0 failures/errors/skips; 2 targeted Failsafe tests, 0 failures/errors/skips; target/jpetstore.war produced |
| Disposable sensitivity check: remove only the session-cleanup line, recompile, run targeted Failsafe | Expected BUILD FAILURE; 2 tests executed, exactly 1 failure, 0 errors/skips; replay assertion expected ORDERS=3 but observed 4; legitimate same-session purchase still passed |
| Sensitivity-result validation | Workflow verified a fresh XML report, exact failing test and duplicate-order assertion, and no infrastructure/skipped-test substitute |
| Independent review | Read-only review found no correctness blocker in the scoped test and CI gate; same-session coverage finding addressed |
| Production scope | Still exactly the one-line fix compared with the imported baseline; this audit changed tests, documentation and CI only |

Fixed-code and sensitivity reports are separate run artifacts. The intentional failing Maven invocation in the sensitivity step is evidence that the regression catches the defect; the enclosing CI check passed after validating that failure.

Subsequent documentation/rule-only updates do not change the application or tested Java files. HTTP dispatch, JSP/browser behavior, simultaneous requests, and deployed operation remain unverified. These test results establish a narrow fix, not hackathon completeness.
