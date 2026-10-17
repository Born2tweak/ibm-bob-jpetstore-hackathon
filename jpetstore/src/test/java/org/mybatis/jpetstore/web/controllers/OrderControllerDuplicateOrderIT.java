/*
 *    Copyright 2010-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.jpetstore.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.jpetstore.domain.Account;
import org.mybatis.jpetstore.domain.Cart;
import org.mybatis.jpetstore.domain.CartItem;
import org.mybatis.jpetstore.domain.Item;
import org.mybatis.jpetstore.domain.Order;
import org.mybatis.jpetstore.mapper.ItemMapper;
import org.mybatis.jpetstore.mapper.MapperTestContext;
import org.mybatis.jpetstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

/**
 * Regression test for the repeated-confirmation duplicate-order defect.
 * <p>
 * The confirmed POST to /order/new uses session["order"] to drive insertOrder. Before the fix, the controller left
 * session["order"] after a successful insert, so a second confirmation replayed the insert with a fresh
 * sequence number, creating a second order row and decrementing inventory a second time.
 * <p>
 * This test uses the real embedded HSQLDB (via MapperTestContext) so that ORDERS, LINEITEM and INVENTORY row counts and
 * values are verified against actual database state. Controller methods are invoked directly: this does not exercise
 * HTTP dispatch, form binding, JSP rendering, or a browser. The replay check fails without session cleanup and passes
 * with it.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { MapperTestContext.class, OrderServiceTestConfig.class })
class OrderControllerDuplicateOrderIT {

  @Autowired
  private OrderService orderService;

  @Autowired
  private ItemMapper itemMapper;

  @Autowired
  private JdbcTemplate jdbc;

  private OrderController orderController;

  // ── helpers ────────────────────────────────────────────────────────────────

  /** Build a minimal authenticated AccountSession for user j2ee. */
  private AccountController.AccountSession buildAccountSession() {
    Account account = new Account();
    account.setUsername("j2ee");
    account.setFirstName("ABC");
    account.setLastName("XYX");
    account.setAddress1("901 San Antonio Road");
    account.setAddress2("MS UCUP02-206");
    account.setCity("Palo Alto");
    account.setState("CA");
    account.setZip("94303");
    account.setCountry("USA");
    return new AccountController.AccountSession(account, List.of(), true);
  }

  /** Build a Cart containing one unit of EST-1 (Large Angelfish, $16.50). */
  private Cart buildCartWithEst1() {
    Item item = new Item();
    item.setItemId("EST-1");
    item.setListPrice(new java.math.BigDecimal("16.50"));

    CartItem cartItem = new CartItem();
    cartItem.setItem(item);
    cartItem.setQuantity(1);
    cartItem.setInStock(true);

    Cart cart = new Cart();
    cart.addItem(item, true);
    return cart;
  }

  /** Place the order through the full controller flow up to the ConfirmOrder page. */
  private HttpSession runCheckoutUpToConfirm(AccountController.AccountSession accountSession, Cart cart) {
    HttpSession session = new MockHttpSession();
    session.setAttribute("accountBean", accountSession);
    session.setAttribute("cart", cart);

    // GET /order/new — builds and stores session["order"]
    Model model = new ExtendedModelMap();
    orderController.newOrderForm(session, model);

    // POST /order/new with billing fields, confirmed=false
    Order billingFormOrder = new Order();
    billingFormOrder.setCardType("Visa");
    billingFormOrder.setCreditCard("999 9999 9999 9999");
    billingFormOrder.setExpiryDate("12/03");
    billingFormOrder.setBillToFirstName("ABC");
    billingFormOrder.setBillToLastName("XYX");
    billingFormOrder.setBillAddress1("901 San Antonio Road");
    billingFormOrder.setBillAddress2("MS UCUP02-206");
    billingFormOrder.setBillCity("Palo Alto");
    billingFormOrder.setBillState("CA");
    billingFormOrder.setBillZip("94303");
    billingFormOrder.setBillCountry("USA");

    orderController.newOrder(billingFormOrder, false, false, session, new ExtendedModelMap());

    return session;
  }

  // ── lifecycle ──────────────────────────────────────────────────────────────

  @BeforeEach
  void setUp() {
    orderController = new OrderController();
    // Inject the real OrderService (backed by embedded HSQLDB) into the controller
    try {
      java.lang.reflect.Field f = OrderController.class.getDeclaredField("orderService");
      f.setAccessible(true);
      f.set(orderController, orderService);
    } catch (Exception e) {
      throw new RuntimeException("Cannot inject orderService into OrderController", e);
    }
  }

  // ── tests ──────────────────────────────────────────────────────────────────

  /**
   * Replay the confirmation POST twice in the same session.
   * <p>
   * EXPECTED (after fix): second POST returns error view; exactly one ORDERS row and one LINEITEM row exist; inventory
   * decremented by exactly 1.
   * <p>
   * ACTUAL (unfixed baseline): second POST succeeds, producing a second ORDERS row and a second LINEITEM row; inventory
   * decremented by 2.
   * <p>
   * This test failed against the unfixed baseline because the second confirmation inserted another order.
   */
  @Test
  void replayConfirmPostInSameSessionMustNotInsertDuplicateOrder() {
    AccountController.AccountSession accountSession = buildAccountSession();
    Cart cart = buildCartWithEst1();

    // ── baseline DB state ──────────────────────────────────────────────────
    int ordersBefore = jdbc.queryForObject("SELECT COUNT(*) FROM ORDERS", Integer.class);
    int lineItemsBefore = jdbc.queryForObject("SELECT COUNT(*) FROM LINEITEM", Integer.class);
    int invBefore = jdbc.queryForObject("SELECT QTY FROM INVENTORY WHERE ITEMID = 'EST-1'", Integer.class);

    // ── checkout up to ConfirmOrder page ──────────────────────────────────
    HttpSession session = runCheckoutUpToConfirm(accountSession, cart);

    // ── first confirmation POST ────────────────────────────────────────────
    Model model1 = new ExtendedModelMap();
    String view1 = orderController.newOrder(new Order(), false, true, session, model1);

    assertThat(view1).as("First confirm must show ViewOrder").isEqualTo("order/ViewOrder");
    assertThat(model1.asMap()).containsEntry("message", "Thank you, your order has been submitted.");

    int ordersAfterFirst = jdbc.queryForObject("SELECT COUNT(*) FROM ORDERS", Integer.class);
    int lineItemsAfterFirst = jdbc.queryForObject("SELECT COUNT(*) FROM LINEITEM", Integer.class);
    int invAfterFirst = jdbc.queryForObject("SELECT QTY FROM INVENTORY WHERE ITEMID = 'EST-1'", Integer.class);

    assertThat(ordersAfterFirst).as("Exactly one order row after first confirm").isEqualTo(ordersBefore + 1);
    assertThat(lineItemsAfterFirst).as("Exactly one line-item row after first confirm").isEqualTo(lineItemsBefore + 1);
    assertThat(invAfterFirst).as("Inventory decremented by 1 after first confirm").isEqualTo(invBefore - 1);

    // Replay after the first confirmation completes.
    Model model2 = new ExtendedModelMap();
    String view2 = orderController.newOrder(new Order(), false, true, session, model2);

    // ── DATABASE ASSERTIONS THAT FAIL ON UNFIXED CODE ─────────────────────
    int ordersAfterSecond = jdbc.queryForObject("SELECT COUNT(*) FROM ORDERS", Integer.class);
    int lineItemsAfterSecond = jdbc.queryForObject("SELECT COUNT(*) FROM LINEITEM", Integer.class);
    int invAfterSecond = jdbc.queryForObject("SELECT QTY FROM INVENTORY WHERE ITEMID = 'EST-1'", Integer.class);

    // After the fix: second confirm must be rejected (no new DB rows).
    // Before the fix: ordersAfterSecond == ordersBefore+2, not ordersBefore+1.
    assertThat(ordersAfterSecond).as("Second confirm POST must NOT insert another order row; " + "unfixed: count was "
        + ordersAfterSecond + " but should stay at " + (ordersBefore + 1)).isEqualTo(ordersBefore + 1);

    assertThat(lineItemsAfterSecond).as("Second confirm POST must NOT insert another line-item row")
        .isEqualTo(lineItemsBefore + 1);

    assertThat(invAfterSecond).as("Inventory must not be decremented a second time").isEqualTo(invBefore - 1);

    // After the fix: second confirm must return the error view, not ViewOrder.
    assertThat(view2).as("Second confirm must return error view, not ViewOrder").isEqualTo("common/Error");
  }

  /**
   * A second legitimate order (new cart, new /order/new cycle) must succeed.
   * <p>
   * This guards that the fix does not block valid repeat purchases.
   */
  @Test
  void secondLegitimateOrderInSameSessionIsAccepted() {
    AccountController.AccountSession accountSession = buildAccountSession();

    // ── First complete purchase ────────────────────────────────────────────
    HttpSession session1 = runCheckoutUpToConfirm(accountSession, buildCartWithEst1());
    orderController.newOrder(new Order(), false, true, session1, new ExtendedModelMap());

    int ordersAfterFirst = jdbc.queryForObject("SELECT COUNT(*) FROM ORDERS", Integer.class);
    int lineItemsAfterFirst = jdbc.queryForObject("SELECT COUNT(*) FROM LINEITEM", Integer.class);
    int inventoryAfterFirst = jdbc.queryForObject("SELECT QTY FROM INVENTORY WHERE ITEMID = 'EST-1'", Integer.class);

    // ── Second legitimate purchase: fresh /order/new cycle ─────────────────
    // After first confirm the cart is reset; simulate a new add-to-cart + checkout.
    Cart cart2 = buildCartWithEst1();
    HttpSession session2 = session1;
    session2.setAttribute("accountBean", accountSession);
    session2.setAttribute("cart", cart2);
    orderController.newOrderForm(session2, new ExtendedModelMap());

    Order billing2 = new Order();
    billing2.setCardType("MasterCard");
    billing2.setCreditCard("1234 5678 9012 3456");
    billing2.setExpiryDate("06/25");
    billing2.setBillToFirstName("ABC");
    billing2.setBillToLastName("XYX");
    billing2.setBillAddress1("901 San Antonio Road");
    billing2.setBillCity("Palo Alto");
    billing2.setBillState("CA");
    billing2.setBillZip("94303");
    billing2.setBillCountry("USA");
    orderController.newOrder(billing2, false, false, session2, new ExtendedModelMap());

    Model model2 = new ExtendedModelMap();
    String view2 = orderController.newOrder(new Order(), false, true, session2, model2);

    assertThat(view2).as("Legitimate second purchase must produce ViewOrder").isEqualTo("order/ViewOrder");
    int ordersAfterSecond = jdbc.queryForObject("SELECT COUNT(*) FROM ORDERS", Integer.class);
    assertThat(ordersAfterSecond).as("Legitimate second purchase must add exactly one more order row")
        .isEqualTo(ordersAfterFirst + 1);
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM LINEITEM", Integer.class))
        .as("Legitimate second purchase must add one line item").isEqualTo(lineItemsAfterFirst + 1);
    assertThat(jdbc.queryForObject("SELECT QTY FROM INVENTORY WHERE ITEMID = 'EST-1'", Integer.class))
        .as("Legitimate second purchase must decrement inventory once").isEqualTo(inventoryAfterFirst - 1);
  }
}
