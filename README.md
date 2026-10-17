# IBM Bob Hackathon — JPetStore

A submission for the IBM Bob Hackathon, centered on a verified change to an unfamiliar, established Java application. Bob's specific contribution is recorded only where session evidence supports it.

## Current project

- **Application:** [MyBatis JPetStore 6](jpetstore/README.md), imported from [mybatis/jpetstore-6](https://github.com/mybatis/jpetstore-6) at `1ffdf1e91b62aa32abfc4511f548c9bdc84b9bbf`.
- **Confirmed issue:** replaying order confirmation in the same session inserted a duplicate order and decremented inventory again.
- **Fix:** clear the session's pending order after successful insertion.
- **Regression evidence:** fresh [CI verification](https://github.com/Born2tweak/ibmi-cobol-hackathon/actions/runs/38985945644) passed 98 unit tests, both targeted direct-controller/database tests, and WAR packaging on b2cec101e03fa6cd780b324f55b91429fa022821. The repeat-purchase check reuses the same session. Removing the fix in the disposable runner caused exactly the expected duplicate-order assertion failure. HTTP/browser and concurrent behavior are not established by these tests.
- **Feature:** required by the student track and still missing. See the readiness gates and bounded recommendation in [HACKATHON.md](HACKATHON.md).
- **Bob provenance:** see [BOB_WORKLOG.md](BOB_WORKLOG.md). Record what Bob actually did from session evidence; do not infer tool attribution from the commit alone.

## Start here

1. Read [HACKATHON.md](HACKATHON.md) for the current readiness audit, official requirements, and build gaps.
2. Read [ARCHITECTURE.md](ARCHITECTURE.md) for the app and checkout flow.
3. Read [SPEC.md](SPEC.md) for the confirmed fix and open feature decision.
4. Read [VERIFICATION.md](VERIFICATION.md) for exact commands and historical results.
5. Open this repository in Bob and use `jpetstore/` as the active application.

Use JDK 21 for this build (the inherited Maven Enforcer rejects JDK 17). From PowerShell, in `jpetstore/`:

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
