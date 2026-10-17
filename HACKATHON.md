# Hackathon plan and status

## Decision

Use MyBatis JPetStore 6 as the submission application. Jack Woehr said the pet-store demo is on target for judges; IBM i exploration can continue after the hackathon. Do not pivot the submission to IBM i during the final days.

## Current state

- **Active branch:** `prep/jpetstore-bob`.
- **App:** `jpetstore/`, imported from upstream commit `1ffdf1e91b62aa32abfc4511f548c9bdc84b9bbf`.
- **Issue:** duplicate order confirmation was reproduced against embedded HSQLDB and fixed.
- **Fix commit:** `192da5039c666c19f3542c0602abe01b44fa18c5`.
- **Recorded verification (October 14):** targeted Failsafe tests 2/2 pass; Surefire tests 98/98 pass. These are historical results; rerun before final submission.
- **Separate feature:** not yet implemented. Confirm whether the rules require one; add only a bounded feature that can be completed and verified before the deadline.
- **Bob usage/provenance:** capture the actual prompts, changes, human decisions, and test evidence in [BOB_WORKLOG.md](BOB_WORKLOG.md). Current Git artifacts alone do not establish which edits Bob made.
- **IBM i examples:** retained as reference; they are not the active application.

## Final path

1. Pull `prep/jpetstore-bob`; review the five-file fix commit and confirm the working tree.
2. Rerun the targeted regression tests and full suite; update [VERIFICATION.md](VERIFICATION.md) with fresh output.
3. Check submission requirements for a separate feature, demo/public URL, video, and required form fields. Do not claim missing deliverables are complete.
4. Record a concise demo: explain the checkout flow, show the reproduced failure evidence, show the one-line session cleanup, then show both regression cases passing.
5. Make the active project obvious on the default branch or submit the active branch URL explicitly.
6. Submit before October 18, 2026 at 11:59 PM EDT.

## Scope guardrails

- Keep the backorder behavior; nonpositive inventory is explicitly presented as backordered.
- Do not claim production readiness or a public deployment; current evidence is local.
- Do not claim Bob authored work unless the worklog/session evidence supports it.
- Preserve the original JPetStore LICENSE and NOTICE and the original IBM i example license.
