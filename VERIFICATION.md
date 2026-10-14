# Verification record — October 13, 2026 (EDT)

Run from `jpetstore/` in the team repository on branch `prep/jpetstore-bob`.

| Check | Exact command | Observed result |
| --- | --- | --- |
| Fresh build and tests | `.\mvnw.cmd clean package` | Exit 0, `BUILD SUCCESS`; Surefire: 98 tests, 0 failures, 0 errors, 0 skipped; WAR at `target/jpetstore.war`. |
| Local Tomcat | `.\mvnw.cmd cargo:run -P tomcat9` | Tomcat 9.0.120 launched on port 8080 and deployed `jpetstore.war`; Maven process must remain running. |
| HTTP smoke check | `Invoke-WebRequest -Uri 'http://localhost:8080/jpetstore/' -UseBasicParsing` | HTTP 200; page title `JPetStore Demo`; response contained `Catalog`. URL: http://localhost:8080/jpetstore/ |
| Import integrity | SHA-256 comparison of all 163 upstream tracked paths against imported counterparts after build | 0 mismatches. The upstream checkout remained unchanged. |

The build emitted OpenRewrite JSP XML parsing warnings and a Tomcat `emptySessionPath` property warning, but it exited successfully and the home page responded. This smoke check does not establish checkout behavior, repeated-confirmation behavior, browser interaction, or a public deployment. No candidate issue has been reproduced and no fix or feature has been implemented. There is no live judge URL yet.

To repeat: from `jpetstore/`, run `.\mvnw.cmd clean package`, then `.\mvnw.cmd cargo:run -P tomcat9` and open http://localhost:8080/jpetstore/. The WAR and Cargo files under `target/` are generated and ignored; do not commit them.
