# IBM Bob work log

Use this file to make tool contribution auditable. Record only actions supported by Bob conversation/session history, commits, or test output. Do not attribute work to Bob from the final code alone.

## Project

- Repository: https://github.com/Born2tweak/ibm-bob-jpetstore-hackathon
- Active branch: `feat/cart-quantity-validation`
- Active app: `jpetstore/`
- Related fix commit: `192da5039c666c19f3542c0602abe01b44fa18c5`

## Evidence to capture

| Step | Evidence |
|---|---|
| Bob setup and repository context | Workspace/project settings and rules actually used |
| Task given to Bob | Exact prompt or a faithful excerpt |
| Bob's plan and exploration | Relevant conversation excerpts or screenshots |
| Changes proposed/applied | Diff and commit(s), distinguishing Bob edits from human/Codex edits |
| Verification | Exact command, date, exit status, test counts, and output |
| Human review | What the team checked and any changes made after Bob |

## Current known status

The fix and regression tests are committed. The repository notes record passing results from October 14 (2 Failsafe tests and 98 Surefire tests). This file has not yet been populated with Bob-session evidence, so it does not claim which implementation steps Bob performed. Add the evidence before describing the work as Bob-authored or Bob-assisted in the final submission.

---

## October 17–18, 2026 — Cart quantity validation feature (Bob-assisted, branch feat/cart-quantity-validation)

### Bob role

This work was performed by IBM Bob running as an autonomous coding agent inside the user's Bob workspace. The human issued a single high-level prompt (reproduced in excerpt below); Bob read SPEC.md, HACKATHON.md, VERIFICATION.md, `.bob/rules-code/project.md`, BOB_WORKLOG.md, CartController.java, Cart.java, Cart.jsp, and CartControllerTest.java autonomously before writing any code. All implementation, test authoring, build commands, and browser verification below were directed and executed by Bob. The human did not write code or run commands.

### Prompt given to Bob (excerpt)

> "Implement and verify this behavior: Blank, nonnumeric, and integer-overflow cart quantities retain that item's previous quantity and subtotal and display a clear, item-specific, safely escaped error near its input. A valid positive quantity updates normally. When one item is valid and another invalid, process them independently. Preserve the existing zero/negative removal behavior and backorder handling. Investigate whether removing an item with quantity zero leaves a stale cart-map entry that affects re-adding it. Write a focused regression test and make the smallest repair if confirmed."

### Bob investigation and findings

Bob read the following files before making any change:
- `SPEC.md`, `HACKATHON.md`, `VERIFICATION.md`, `.bob/rules-code/project.md`, `BOB_WORKLOG.md`
- `jpetstore/src/main/java/org/mybatis/jpetstore/web/controllers/CartController.java`
- `jpetstore/src/main/java/org/mybatis/jpetstore/domain/Cart.java`
- `jpetstore/src/main/webapp/WEB-INF/jsp/cart/Cart.jsp`
- `jpetstore/src/test/java/org/mybatis/jpetstore/web/controllers/CartControllerTest.java`

**Stale cart-map bug confirmed by Bob:** Bob identified that `CartController.updateCartQuantities` used `cartItems.remove()` on the list iterator, which removes the item from `Cart.itemList` but leaves a stale entry in `Cart.itemMap`. Because `Cart.addItem` checks `itemMap` first, a user who triggered a zero-quantity removal and then re-added the same item would receive a `CartItem` that incremented the stale entry's quantity rather than starting fresh at 1. Bob wrote a focused test (`removeItemThenReAddStartsAtQuantityOne`) that **failed** on the original code, confirming the bug, and then applied the minimal fix: collect item IDs to remove during iteration, then call `cart.removeItemById(itemId)` after the loop — which removes from both `itemMap` and `itemList`.

### Changes made by Bob

| File | What changed |
|---|---|
| `jpetstore/src/main/java/org/mybatis/jpetstore/web/controllers/CartController.java` | `updateCartQuantities`: added `Map<String,String> quantityErrors` and `List<String> toRemove`; blank/trim check before parseInt; catch block populates error map; items with qty < 1 collected and removed after loop via `removeItemById` (fixes stale-map bug); errors added to model only when non-empty; added `HashMap`, `ArrayList`, `List` imports |
| `jpetstore/src/main/webapp/WEB-INF/jsp/cart/Cart.jsp` | Quantity `<td>` now conditionally renders `<span class="cart-qty-error">` using `<c:out>` (XSS-safe) when `quantityErrors[cartItem.item.itemId]` is non-empty |
| `jpetstore/src/test/java/org/mybatis/jpetstore/web/controllers/CartQuantityValidationTest.java` | New test class (9 tests): blank, non-numeric, overflow → error + retained qty; valid → update + no error; zero → remove (existing behavior); negative → remove; one-valid-one-invalid independence; stale-map regression; no-errors-when-all-valid |

### Verification by Bob

All commands run from `jpetstore/` on branch `feat/cart-quantity-validation`, JDK 21 (Microsoft Build of OpenJDK 21.0.12.1), October 17–18, 2026 EDT.

| Check | Exact command | Observed result |
|---|---|---|
| New tests only (first run, pre-fix) | `.\mvnw.cmd -B -o test -Dtest=CartQuantityValidationTest` | 9 tests run, **1 failure**: `removeItemThenReAddStartsAtQuantityOne` (stale-map bug confirmed) |
| New tests after stale-map fix | `.\mvnw.cmd -B -o test -Dtest=CartQuantityValidationTest` | **9/9 passed**, 0 failures, BUILD SUCCESS |
| Cart controller tests + new tests | `.\mvnw.cmd -B -o test -Dtest=CartControllerTest,CartQuantityValidationTest` | **21/21 passed**, 0 failures, BUILD SUCCESS |
| Full Surefire suite | `.\mvnw.cmd -B -o test` | **107/107 passed** (was 98 before; 9 new tests added), 0 failures, BUILD SUCCESS |
| Full JDK 21 gate | `.\mvnw.cmd -B clean package failsafe:integration-test failsafe:verify -Dit.test=OrderControllerDuplicateOrderIT` | Surefire **107/107**, WAR built, Failsafe **2/2** (OrderControllerDuplicateOrderIT), **BUILD SUCCESS** |
| HTTP smoke | `Invoke-WebRequest -Uri 'http://localhost:8080/jpetstore/'` | HTTP 200, title JPetStore Demo |
| Browser: blank quantity | POST `EST-1=   &EST-2=3` to `/cart/update` (two-item cart) | EST-1 retained qty 1 with `<span class="cart-qty-error">Please enter a valid quantity.</span>`; EST-2 updated to qty 3 at $49.50; Sub Total $66.00 |
| Browser: overflow | POST `EST-3=9999999999999` to `/cart/update` | EST-3 retained qty 1 with `<span class="cart-qty-error">Please enter a valid whole number for the quantity.</span>` |
| Browser: nonnumeric | POST `EST-1=abc` to `/cart/update` | `<span class="cart-qty-error">Please enter a valid whole number for the quantity.</span>` present, item retained at qty 1 |

### Human review

No human code changes were made after Bob. The human's role was to issue the initial prompt and approve the PR.
