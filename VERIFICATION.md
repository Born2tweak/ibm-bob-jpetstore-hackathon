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
