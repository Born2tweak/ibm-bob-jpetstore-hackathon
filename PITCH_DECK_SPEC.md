# Pitch deck specification — JPetStore checkout reliability

**Status:** specification only. No slide deck or PDF has been produced for submission.

## Purpose and format

Make a five-slide, 16:9 PDF for the **Students / Early Career — Explore, Fix, and Build** track. The audience is AngelHack judges who may have only a few minutes. The deck must introduce the current team, explain the imported application and the two distinct improvements, show IBM Bob's actual role, ground the technical claims in evidence, and name realistic next steps. The companion [demo runbook](DEMO_RUNBOOK.md) covers the three-minute video; the deck should support that story rather than repeat every click.

Current team line, supplied by the user on October 18: **Bloodhounds — Andrian Kolliegbo and Teaco Zinnah**. Recheck the portal roster before final export.

## Slide-by-slide content

| # | Slide title | On-slide copy and evidence | Visual direction |
| --- | --- | --- | --- |
| 1 | **JPetStore checkout reliability** | Subtitle: “A repeat confirmation creates one order. Bad cart input gets a useful answer.” Team: “Bloodhounds — Andrian Kolliegbo · Teaco Zinnah.” Track: “IBM Bob Hackathon · Explore, Fix, and Build.” | One large title on a dark navy field; team and track smaller. No stock artwork. |
| 2 | **The checkout defect** | “The original app kept a pending order in the session after saving it. A second confirmation inserted another order and decremented inventory again.” Then, prominent fix: “Clear the pending session order after the successful insert.” Small scope note: “Verified for sequential confirmation in one session.” | Use a simple before/after sequence or one short code excerpt from `OrderController.java`. Attribute this fix separately from Bob's cart work. |
| 3 | **Bob-assisted cart validation** | “IBM Bob explored the cart controller, cart model, JSP, and tests.” Show `abc` → inline item-specific error, prior quantity and subtotal retained; `2` → valid update and recalculated subtotal. Add: “Bob found and repaired a stale cart-map entry after removal. Nine new tests cover the feature.” | Prefer an authentic, cropped screenshot of the public cart's error state if available. Keep item ID, error, quantity, and subtotal visible together. |
| 4 | **Public browser verification** | Four observed checkpoints: invalid `abc` → inline error and **$16.50** retained; quantity `2` → **$33.00** subtotal; checkout → one **$33.00** order; repeat Confirm in the same session → `order was null`, and My Orders still showed one order. | A compact evidence table or two authentic screenshots. Label this “October 18 public browser test.” Do not imply a persistent order number; the in-memory database can reset. |
| 5 | **What runs today and what comes next** | Stack: “Java 21 · Spring MVC · MyBatis · HSQLDB · Tomcat 9 · Docker · Render · IBM Bob IDE.” Evidence: “107 unit tests and two targeted checkout integration tests; public cart and checkout tested in a browser.” Roadmap: “Durable order storage” and “Protection against concurrent confirmation requests.” Include the GitHub and live demo links. | Dark navy close. Make the links readable and clickable in the PDF. The roadmap should remain concrete and brief. |

## Source and attribution rules

- JPetStore is an imported [MyBatis JPetStore 6](https://github.com/mybatis/jpetstore-6) application. The repository also preserves [Jack Woehr's IBM i examples](https://github.com/jwoehr/ibmi-cobol-examples); do not present either upstream project as newly authored by this team. Source and licenses are explained in [README.md](README.md).
- The checkout replay fix and its two targeted integration tests are documented in [VERIFICATION.md](VERIFICATION.md). Bob's supported role is the cart feature in [BOB_WORKLOG.md](BOB_WORKLOG.md) and [PR #2](https://github.com/Born2tweak/ibm-bob-jpetstore-hackathon/pull/2). Do not label the earlier replay fix Bob-authored.
- The current public flow is recorded in [VERIFICATION.md](VERIFICATION.md). The deployment and updated documentation merged in [PR #3](https://github.com/Born2tweak/ibm-bob-jpetstore-hackathon/pull/3); its [Checkout regression run](https://github.com/Born2tweak/ibm-bob-jpetstore-hackathon/actions/runs/39174131588) passed on the PR head.
- The [live app](https://ibm-bob-jpetstore-hackathon.onrender.com/jpetstore/) uses in-memory HSQLDB on a free Render service. It can sleep and reset orders on restart. The repeated-confirmation evidence is sequential in one session; concurrent POST behavior was not tested.

## Final-deck acceptance check

1. Confirm the exact AngelHack roster and spelling before export.
2. Build exactly five slides; export a PDF; inspect every slide at presentation size for wrapping and legibility.
3. Check that all figures and wording match the dated verification record. Use only authentic app screenshots, with demo data and no personal tabs or account details.
4. Open the PDF and its embedded GitHub/live links. Add the PDF to the public repository and check it can be opened while signed out.
5. Insert the verified PDF URL into [SUBMISSION_DRAFT.md](SUBMISSION_DRAFT.md) and the AngelHack submission form. Keep the ≤3-minute YouTube video and live demo URL accessible to judges.
