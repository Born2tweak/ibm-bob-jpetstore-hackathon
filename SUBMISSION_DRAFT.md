# AngelHack submission copy — draft

## Track

Students / Early Career — Explore, Fix, and Build

## Project description (90 words; maximum 100)

JPetStore is an existing Java web app we used for the Explore, Fix, and Build track. We reproduced and fixed a checkout bug that created a second order when confirmation was repeated in one session. With IBM Bob, we added item-specific cart quantity feedback and repaired a stale cart entry after removal. The public demo lets judges try both flows. A JDK 21 build covers 107 unit tests and two checkout integration tests. We document the upstream source, Bob's contribution, and browser evidence. Orders reset when the demo service restarts.

## Improvements made during the hackathon

- Fixed sequential same-session order-confirmation replay by clearing the pending session order after a successful insert; added two direct-controller/database integration tests and a CI sensitivity check.
- Used IBM Bob to add per-item validation for blank, nonnumeric, and overflowing cart quantities; valid quantities still update. Bob also repaired the stale cart-map entry after zero/negative removal. Nine focused tests cover the feature.
- Added JDK 21 CI, documented architecture and provenance, packaged the WAR in a Docker image, and deployed the app to a public Render service. Browser-tested cart validation, checkout, replay, and order history.

## Tech stack

Java 21; Spring MVC 5.3; MyBatis; JSP/JSTL; Maven; HSQLDB (in memory); Tomcat 9; Docker; GitHub Actions; Render; IBM Bob IDE.

## Links

- Repository: https://github.com/Born2tweak/ibm-bob-jpetstore-hackathon
- Live app: https://ibm-bob-jpetstore-hackathon.onrender.com/jpetstore/
- Bob work log: https://github.com/Born2tweak/ibm-bob-jpetstore-hackathon/blob/main/BOB_WORKLOG.md
- Demo video: **add verified judge-accessible YouTube URL**
- Pitch deck PDF: **add after export and upload**

## Claim limits

The replay test covers sequential requests in one session. The embedded database and demo orders are reset by service restart. The checkout replay fix and Bob-assisted cart feature have distinct provenance in the repository.
