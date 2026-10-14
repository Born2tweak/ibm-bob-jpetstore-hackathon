# Preliminary JPetStore specification

## Goal and provenance

Use the unchanged MyBatis JPetStore 6 import (`mybatis/jpetstore-6` at `1ffdf1e91b62aa32abfc4511f548c9bdc84b9bbf`) as the submission baseline. This document is Codex preparation for IBM Bob, not Bob output. Preserve the original app behavior unless a reproducible issue and agreed expected behavior justify a change.

## Candidate issue report

1. **Repeated order confirmation — CONFIRMED AND FIXED (October 14, 2026).** `OrderController.newOrder` on `confirmed=true` inserted the `sessionOrder` and replaced the cart, but left `session["order"]` in place. A second POST to `/order/new` with `confirmed=true` in the same session called `insertOrder` again, producing a second `ORDERS` row, a second `LINEITEM` row, and a second inventory decrement. **Reproduced in `OrderControllerDuplicateOrderIT.replayConfirmPostInSameSessionMustNotInsertDuplicateOrder`** (embedded HSQLDB, no container required): unfixed run showed `expected: 3 but was: 4` ORDERS rows. **Fixed** by adding `session.removeAttribute("order")` immediately after `orderService.insertOrder(sessionOrder)` in `OrderController.java`. After fix: both IT tests pass (2/2), full Surefire suite passes (98/98, 0 failures). Source: `jpetstore/src/main/java/org/mybatis/jpetstore/web/controllers/OrderController.java`. See `VERIFICATION.md` for exact commands and output.
2. **Invalid cart input.** `CartController.updateCartQuantities` catches `NumberFormatException` and silently keeps the prior quantity. Source: `jpetstore/src/main/java/org/mybatis/jpetstore/web/controllers/CartController.java`. Reproduce by posting `abc`, a blank value, and an overflowing integer in the quantity field; inspect the page and session cart. Decide whether preserving the old value is acceptable or whether a visible validation message is needed. This is a usability hypothesis, not a confirmed defect.
3. **Checkout correctness.** `OrderController.newOrderForm` checks that a cart exists, but does not explicitly require a nonempty cart. Source: `OrderController.java`. Reproduce with a signed-in account and an empty cart, then follow `/order/new` through confirmation; inspect saved orders and expected product behavior. This is provisional until the flow is observed.

## Backorder constraint

`jpetstore/src/main/webapp/WEB-INF/jsp/catalog/Item.jsp` displays “Back ordered.” for inventory `<= 0`. `jpetstore/src/main/webapp/help.html` says requests above stock are backordered. Negative inventory has not been reproduced here and cannot be called a defect solely from the decrement SQL. Do not block backorders or change the stock model without verifying intended behavior.

## Provisional feature

Choose a bounded user-facing feature only after the issue is reproduced and the baseline is exercised. Record its user need, acceptance checks, Bob contribution, and before/after evidence then. No feature is selected or implemented yet.
