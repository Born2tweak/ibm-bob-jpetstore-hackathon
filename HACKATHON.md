# Hackathon readiness audit

> **Current status, October 18:** The dated audit below predates PR #2 and the public deployment. The collaborative team has the merged Bob-assisted cart validation feature (`62ad28f`), a live [Render demo](https://ibm-bob-jpetstore-hackathon.onrender.com/jpetstore/), and public browser evidence for invalid/valid cart input, checkout, and same-session repeat confirmation. See [BOB_WORKLOG.md](BOB_WORKLOG.md) and [VERIFICATION.md](VERIFICATION.md). Submission assets and the AngelHack submission receipt remain to be completed; the historical "missing" gates below are superseded. The app uses in-memory HSQLDB, so orders can reset on restart.

Reviewed October 16, 2026 (EDT). Active application: JPetStore in jpetstore/. Active branch: prep/jpetstore-bob. Deadline: October 18 at 11:59 PM EDT.

## Verdict

The Java application fits the student track's unfamiliar-application direction. The sequential duplicate-confirmation fix is a credible, bounded maintenance change. The current entry is incomplete: a useful feature, live app verification, and Bob workflow evidence are still missing. Passing this regression alone does not establish a complete or competitive submission.

## Requirement and rubric map

Sources: [official event page](https://hackathon.angelhack.com/web/events/building-with-ibm-bob) and [IBM event page](https://developer.ibm.com/events/building-with-ibm-bob-angelhack-x-ibm-virtual-hackathon/), checked October 16.

| Area | Current evidence | Remaining gate |
|---|---|---|
| Explore, Fix, and Build student track | Existing JPetStore application; reproduced issue and committed fix | Implement one useful feature; documenting the fix alone does not satisfy the build step |
| Bob adoption (30%) | Bob was configured and used according to the team conversation | Record actual Bob prompts/actions, human corrections, commits and outcomes in BOB_WORKLOG.md |
| Relevance/innovation (30%) | Concrete repeated-business-effect problem | Demonstrate a related useful capability and explain its user value |
| Functionality/execution (30%) | Fresh JDK 21 CI: 98 unit tests, 2 targeted integration tests and WAR packaging passed; removing the fix caused the expected assertion failure | Real HTTP/browser checkout and externally accessible live app |
| Clarity (10%) | README and architecture map exist | Align the final story with observed behavior and actual Bob contribution |
| Submission package | Source, spec and historical verification in GitHub | Live URL, PDF deck, YouTube video at most 3 minutes, description at most 100 words, track/stack, in-window improvements |

The separate linked rules document could not be retrieved. Full terms/rule coverage and the team's registrations remain unverified. The public track text says a seeded issue; this project fixes a discovered upstream issue. Keep that distinction honest and check organizer guidance if no clarification already exists.

## Engineering scope

The production diff against the imported baseline is exactly one added line: remove the pending session order after successful insertion. Schema, SQL, pricing, inventory policy and backorders are unchanged.

The regression invokes controller methods directly using MockHttpSession and the real transaction-managed service/MyBatis/HSQLDB. It checks sequential replay after the first request completes. It does not test servlet dispatch, form binding, JSP rendering, browser behavior, or simultaneous confirmations. Do not claim general idempotency or concurrent double-submit protection.

The repeat-purchase test has been tightened to reuse the same session and check order, line-item and inventory effects. The changed test passed in [fresh CI](https://github.com/Born2tweak/ibm-bob-jpetstore-hackathon/actions/runs/38985945644). The root-level CI workflow packages the app, runs unit plus targeted integration tests, then verifies that removing the fix causes the expected duplicate-order assertion failure. Nested upstream workflows in jpetstore/.github/ are reference material, not active repository workflows.

## Immediate work, in order

1. CI is green for the updated test. Perform a real checkout, replay, and another checkout in the same browser session; direct-method tests do not replace this.
2. Complete one useful feature. A bounded candidate is recoverable cart-quantity validation: explain invalid input beside the item, retain the prior quantity, allow correction, and preserve the existing below-one removal/backorder behavior. Current source silently ignores nonnumeric input. This is a recommendation, not implemented scope.
3. Deploy the tested application and check the live flow externally.
4. Capture actual Bob session evidence. The event page advertises participant Pro+ access during the hackathon; verify redemption with the organizers if the account is still limited to the exhausted trial allowance.
5. Prepare presentation assets after these build/compliance gaps are closed.

## Provenance and links

JPetStore was imported from mybatis/jpetstore-6 at 1ffdf1e91b62aa32abfc4511f548c9bdc84b9bbf. The fix is in 192da5039c666c19f3542c0602abe01b44fa18c5. Original IBM i examples remain reference material. Preserve both sets of licenses and the JPetStore notice.

See ARCHITECTURE.md, SPEC.md, VERIFICATION.md and BOB_WORKLOG.md. The default branch remains stale until PR #1 is merged or the active branch is made the submission target.
