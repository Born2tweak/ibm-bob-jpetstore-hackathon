# Three-minute demo runbook

The recording must be **at most three minutes**, prerecorded, uploaded to YouTube, and linked in the AngelHack submission. This script targets about 2:35, leaving room for loading and speech pauses. Record a browser window that does not show personal tabs or notifications. Use only the seeded demo account and fake payment values. Warm the [public app](https://ibm-bob-jpetstore-hackathon.onrender.com/jpetstore/) before recording because its free host can sleep.

| Time | Show | Say |
| --- | --- | --- |
| 0:00–0:20 | Title slide, then repository README | “We started with the established MyBatis JPetStore app. In the Explore, Fix, and Build track, we addressed a checkout replay bug and added cart feedback.” |
| 0:20–0:40 | `BOB_WORKLOG.md`, cart feature section | “IBM Bob explored the cart controller, cart model, JSP, and tests. It implemented item-specific quantity errors and found a stale cart-map entry after removal. The checkout replay fix is separately attributed.” |
| 0:40–1:25 | Public store: sign in, add EST-1, enter `abc`, update; then enter `2`, update | “Bad quantity input now explains the problem and keeps the previous quantity and total. A valid correction updates the total.” Pause briefly on the inline error and then on the changed subtotal. |
| 1:25–2:10 | Proceed to Checkout, keep the prefilled fake card/details, Continue, Confirm | “The checkout creates one order. This demo uses fake payment data and an in-memory database.” Show the submitted order number and total. |
| 2:10–2:30 | Browser Back to the confirmation form, click Confirm again; then My Account > My Orders | “A repeated confirmation in the same session no longer inserts another order. The page reports that the pending order is gone, and order history contains only the first order.” |
| 2:30–2:40 | Final slide with test count and URLs | “The JDK 21 gate covered 107 unit tests and two targeted checkout integration tests. Next we would add durable storage and concurrent-request protection.” |

## Before recording

1. Open a fresh browser session and confirm the live site, catalog, sign-in, and cart load. If the free instance restarted, wait for the cold start and reset state.
2. Open the repository README and `BOB_WORKLOG.md` in separate tabs. Use readable browser zoom. Hide private bookmarks, account menus, and desktop notifications.
3. Rehearse the click path once. Use the seeded `j2ee` demo account. Do not show or enter real payment data.
4. On Windows, open **Clipchamp** and choose **Record & create > Screen**. Select the microphone, start recording, choose the browser window, and narrate the table above. Microsoft's [screen recording instructions](https://support.microsoft.com/en-us/clipchamp/how-to-make-a-screen-recording) describe the window and microphone selection. Export at 1080p or another readable resolution. Stop below 3:00 and watch the export once for readable error text, order total, audio, and accidental private content.
5. Upload the file in [YouTube Studio](https://support.google.com/youtube/answer/57407). Set visibility to **Unlisted** or **Public**; [YouTube says](https://support.google.com/youtube/answer/157177) anyone with an Unlisted link can watch without a Google account. Open the final link in a signed-out browser to verify access. Put that exact link in the repository and AngelHack form.

## Source and claim boundaries

- The Bob-assisted cart feature and nine new tests are in PR #2 and documented in `BOB_WORKLOG.md`. Do not call the earlier replay fix Bob-authored.
- Browser replay evidence is **sequential in one session**. Do not claim concurrent double-submit protection.
- Render's free service can sleep; the embedded HSQLDB resets on restart. Orders in this demo are not durable.
