# IBM Bob Hackathon — JPetStore

A collaborative submission for the IBM Bob Hackathon, centered on a checkout fix and a Bob-assisted cart feature in an established Java application. Bob's specific contribution is recorded in the work log.

**Live demo:** https://ibm-bob-jpetstore-hackathon.onrender.com/jpetstore/ (Render free service; the first request after inactivity may take time). Use the seeded `j2ee` demo account and fake payment details only. The embedded database resets when the service restarts.

## Current project

- **Application:** [MyBatis JPetStore 6](jpetstore/README.md), imported from [mybatis/jpetstore-6](https://github.com/mybatis/jpetstore-6) at `1ffdf1e91b62aa32abfc4511f548c9bdc84b9bbf`.
- **Confirmed issue:** replaying order confirmation in the same session inserted a duplicate order and decremented inventory again.
- **Fix:** clear the session's pending order after successful insertion.
- **Fix evidence:** [CI verification](https://github.com/Born2tweak/ibm-bob-jpetstore-hackathon/actions/runs/38985945644) passed 98 unit tests, two direct-controller/database replay tests, WAR packaging, and a sensitivity check on the earlier fix revision. A public browser checkout on October 18 created one $33.00 order; repeating confirmation in the same session returned an error and My Orders still listed one order. Concurrent replay is not established.
- **Feature:** Bob-assisted cart quantity validation shows an item-specific error for invalid input while preserving the prior quantity and subtotal. Corrected positive input updates normally; the related stale cart-map removal bug is covered by a regression test. PR #2 added nine tests and was merged to `main` as `62ad28f`.
- **Bob provenance:** see [BOB_WORKLOG.md](BOB_WORKLOG.md). Record what Bob actually did from session evidence; do not infer tool attribution from the commit alone.

## Start here

1. Read [HACKATHON.md](HACKATHON.md) for the current readiness audit, official requirements, and build gaps.
2. Read [ARCHITECTURE.md](ARCHITECTURE.md) for the app and checkout flow.
3. Read [SPEC.md](SPEC.md) for the original issue specification and current addendum.
4. Read [VERIFICATION.md](VERIFICATION.md) for exact commands and historical results.
5. Open this repository in Bob and use `jpetstore/` as the active application.

Use JDK 21 for this build (the inherited Maven Enforcer rejects JDK 17). From PowerShell, in `jpetstore/`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd test-compile failsafe:integration-test failsafe:verify -Dit.test=OrderControllerDuplicateOrderIT -Dcargo.skip=true
.\mvnw.cmd clean package
.\mvnw.cmd cargo:run -P tomcat9
```

The local app is expected at http://localhost:8080/jpetstore/. See [VERIFICATION.md](VERIFICATION.md) for the dated public browser result and deployment source.

## Repository layout

- `jpetstore/` — active Java web application and its tests.
- `picclaus/`, `cobolinfo/`, and related planning files — Jack Woehr's original IBM i COBOL examples, preserved as background/reference material.
- `ARCHITECTURE.md`, `SPEC.md`, `VERIFICATION.md`, `HACKATHON.md` — project map and evidence.

The repository began as a fork of [Jack Woehr's IBM i COBOL examples](https://github.com/jwoehr/ibmi-cobol-examples). Those examples and their Apache 2.0 license remain intact. JPetStore is separately distributed under its included Apache 2.0 license and notice.
