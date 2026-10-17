# Application architecture and verification map

## Scope

The active hackathon application is MyBatis JPetStore 6, a Java web app. It is not an IBM i application. The repository's COBOL examples are preserved reference material. This map reflects the checked-in source on `prep/jpetstore-bob`.

## Runtime path

```mermaid
flowchart LR
  Browser["Browser / JSP views"] --> Web["Spring MVC controllers"]
  Web --> Session["HTTP session: account, cart, pending order"]
  Web --> Services["Spring services"]
  Services --> Mappers["MyBatis mapper interfaces + XML SQL"]
  Mappers --> DB["Relational database"]
```

- Controllers handle HTTP routes, session state, and view selection.
- Services hold application operations. `OrderService.insertOrder` is transactional.
- MyBatis mapper interfaces and XML statements perform SQL.
- The app's runtime database is configured by its Spring/MyBatis resources; the duplicate-order integration tests use embedded HSQLDB.

## Checkout and the fix

```mermaid
sequenceDiagram
  participant U as User
  participant C as OrderController
  participant S as OrderService
  participant D as Database
  U->>C: GET /order/new
  C->>C: Build Order from account + cart; save session["order"]
  U->>C: POST /order/new (confirmed=false)
  C-->>U: Confirmation view
  U->>C: POST /order/new (confirmed=true)
  C->>S: insertOrder(sessionOrder)
  S->>D: Allocate ID, update inventory, insert order/status/lines
  C->>C: Remove session["order"]; reset cart
  C-->>U: ViewOrder
  U->>C: Replay confirmation POST
  C-->>U: Error view; no second insert
```

Before the fix, the successful confirmation left `session["order"]` in the session, so replaying the confirmation POST called `insertOrder` again. The fix removes that session attribute only after the insert succeeds. The local `sessionOrder` variable remains available for the response view.

The controller is intended to allow a fresh checkout cycle in the same session. The updated regression suite checks both cases through direct Java method calls; it does not dispatch HTTP requests or render JSPs:

- `replayConfirmPostInSameSessionMustNotInsertDuplicateOrder` — second confirmation does not add order or line-item rows or decrement inventory again.
- `secondLegitimateOrderInSameSessionIsAccepted` — a later order in the same session is accepted; fresh CI checks its order, line-item and inventory effects.

## Evidence map

| Claim | Evidence |
|---|---|
| The replay path could insert twice before the fix | Historical failure plus fresh CI sensitivity failure recorded in [VERIFICATION.md](VERIFICATION.md) |
| The fix blocks sequential replay after successful completion | `OrderControllerDuplicateOrderIT`, after-fix run recorded in [VERIFICATION.md](VERIFICATION.md) |
| A later order works in the same session | `secondLegitimateOrderInSameSessionIsAccepted` |
| Existing unit tests remain green | 98-test Surefire result recorded in [VERIFICATION.md](VERIFICATION.md) |
| Bob's contribution | Not inferable from source or commit alone; record supported details in [BOB_WORKLOG.md](BOB_WORKLOG.md) |

## Repository map

- `jpetstore/src/main/java/.../web/controllers/` — HTTP routes and session handling.
- `jpetstore/src/main/java/.../service/` — business operations, including transactional order insertion.
- `jpetstore/src/main/java/.../mapper/` and `jpetstore/src/main/resources/.../mapper/` — persistence interfaces and SQL mappings.
- `jpetstore/src/test/` — unit and database-backed integration tests.
- Root `picclaus/` and `cobolinfo/` — original IBM i COBOL examples, not part of the active JPetStore runtime.

## Current boundary

The sequential replay fix is implemented. Concurrent requests can both read the pending order before removal; simultaneous-request protection is outside the verified contract. The student track requires a useful feature, and none is implemented in this snapshot. See HACKATHON.md for the remaining gates.
