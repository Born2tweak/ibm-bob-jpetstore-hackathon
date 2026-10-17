# IBM Bob Hackathon — JPetStore

A hackathon project using IBM Bob to understand and change an unfamiliar, established Java application, with tests that demonstrate whether the change is trustworthy.

## Current project

- **Application:** [MyBatis JPetStore 6](jpetstore/README.md), imported from [mybatis/jpetstore-6](https://github.com/mybatis/jpetstore-6) at `1ffdf1e91b62aa32abfc4511f548c9bdc84b9bbf`.
- **Confirmed issue:** replaying order confirmation in the same session inserted a duplicate order and decremented inventory again.
- **Fix:** clear the session's pending order after successful insertion.
- **Regression evidence:** two database-backed integration tests cover replay rejection and a legitimate later order. The recorded run passed 2/2; the recorded Surefire suite passed 98/98. Both were run October 14; rerun before submission.
- **Feature:** no separate feature is implemented yet. Check the hackathon requirements before adding scope.
- **Bob provenance:** see [BOB_WORKLOG.md](BOB_WORKLOG.md). Record what Bob actually did from session evidence; do not infer tool attribution from the commit alone.

## Start here

1. Read [HACKATHON.md](HACKATHON.md) for scope and submission status.
2. Read [ARCHITECTURE.md](ARCHITECTURE.md) for the app and checkout flow.
3. Read [SPEC.md](SPEC.md) for the confirmed fix and open feature decision.
4. Read [VERIFICATION.md](VERIFICATION.md) for exact commands and historical results.
5. Open this repository in Bob and use `jpetstore/` as the active application.

From PowerShell, in `jpetstore/`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd test-compile failsafe:integration-test failsafe:verify -Dit.test=OrderControllerDuplicateOrderIT -Dcargo.skip=true
.\mvnw.cmd clean package
.\mvnw.cmd cargo:run -P tomcat9
```

The app is expected at http://localhost:8080/jpetstore/.

## Repository layout

- `jpetstore/` — active Java web application and its tests.
- `picclaus/`, `cobolinfo/`, and related planning files — Jack Woehr's original IBM i COBOL examples, preserved as background/reference material.
- `ARCHITECTURE.md`, `SPEC.md`, `VERIFICATION.md`, `HACKATHON.md` — project map and evidence.

The repository began as a fork of [Jack Woehr's IBM i COBOL examples](https://github.com/jwoehr/ibmi-cobol-examples). Those examples and their Apache 2.0 license remain intact. JPetStore is separately distributed under its included Apache 2.0 license and notice.
