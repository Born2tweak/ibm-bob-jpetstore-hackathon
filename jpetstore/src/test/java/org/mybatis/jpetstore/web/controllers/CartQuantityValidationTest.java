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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mybatis.jpetstore.domain.Cart;
import org.mybatis.jpetstore.domain.Item;
import org.mybatis.jpetstore.service.CatalogService;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

/**
 * Focused tests for cart-quantity validation and stale-entry regression.
 * <p>
 * These tests document the new behaviour introduced for the hackathon feature: blank, non-numeric, and integer-overflow
 * quantities are rejected per-item with an escaped error message; zero/negative quantities continue to remove the item;
 * valid quantities update normally; and removing an item followed by re-adding it does not leave a stale entry in the
 * cart's internal map.
 */
@ExtendWith(MockitoExtension.class)
class CartQuantityValidationTest {

  @Mock
  private CatalogService catalogService;

  @InjectMocks
  private CartController cartController;

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private Item makeItem(String id, String price) {
    Item item = new Item();
    item.setItemId(id);
    item.setListPrice(new java.math.BigDecimal(price));
    return item;
  }

  private Cart cartWith(Item item) {
    Cart cart = new Cart();
    cart.addItem(item, true);
    return cart;
  }

  // -------------------------------------------------------------------------
  // Blank quantity
  // -------------------------------------------------------------------------

  /**
   * A blank quantity string must retain the previous quantity, show no removal, and place an item-specific error in the
   * quantityErrors model attribute.
   * <p>
   * This test will FAIL on the original code (which calls Integer.parseInt on a blank string, catches
   * NumberFormatException, and silently skips without setting any error).
   */
  @Test
  void blankQuantityRetainsPreviousQuantityAndSetsError() {
    Item item = makeItem("EST-1", "10.00");
    Cart cart = cartWith(item);
    // item starts at quantity 1

    HttpSession session = mock(HttpSession.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    Model model = new ExtendedModelMap();
    when(session.getAttribute("cart")).thenReturn(cart);
    when(request.getParameter("EST-1")).thenReturn("  "); // blank / whitespace

    String view = cartController.updateCartQuantities(request, session, model);

    assertThat(view).isEqualTo("cart/Cart");
    // quantity must be preserved
    assertThat(cart.getCartItemList()).hasSize(1);
    assertThat(cart.getCartItemList().get(0).getQuantity()).isEqualTo(1);
    // error map must exist and contain an entry for this item
    @SuppressWarnings("unchecked")
    Map<String, String> errors = (Map<String, String>) model.asMap().get("quantityErrors");
    assertThat(errors).isNotNull().containsKey("EST-1");
    assertThat(errors.get("EST-1")).isNotBlank();
  }

  // -------------------------------------------------------------------------
  // Non-numeric quantity
  // -------------------------------------------------------------------------

  /**
   * A non-numeric quantity must retain the previous quantity and record an error.
   * <p>
   * This test will FAIL on the original code (no error is placed in the model).
   */
  @Test
  void nonnumericQuantityRetainsPreviousQuantityAndSetsError() {
    Item item = makeItem("EST-2", "5.00");
    Cart cart = cartWith(item);

    HttpSession session = mock(HttpSession.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    Model model = new ExtendedModelMap();
    when(session.getAttribute("cart")).thenReturn(cart);
    when(request.getParameter("EST-2")).thenReturn("abc");

    String view = cartController.updateCartQuantities(request, session, model);

    assertThat(view).isEqualTo("cart/Cart");
    assertThat(cart.getCartItemList()).hasSize(1);
    assertThat(cart.getCartItemList().get(0).getQuantity()).isEqualTo(1);
    @SuppressWarnings("unchecked")
    Map<String, String> errors = (Map<String, String>) model.asMap().get("quantityErrors");
    assertThat(errors).isNotNull().containsKey("EST-2");
    assertThat(errors.get("EST-2")).isNotBlank();
  }

  // -------------------------------------------------------------------------
  // Integer-overflow quantity
  // -------------------------------------------------------------------------

  /**
   * An integer-overflow value (e.g. Long.MAX_VALUE as a string) must retain the previous quantity and record an error
   * rather than propagating silently.
   * <p>
   * This test will FAIL on the original code (no error is placed in the model).
   */
  @Test
  void overflowQuantityRetainsPreviousQuantityAndSetsError() {
    Item item = makeItem("EST-3", "20.00");
    Cart cart = cartWith(item);

    HttpSession session = mock(HttpSession.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    Model model = new ExtendedModelMap();
    when(session.getAttribute("cart")).thenReturn(cart);
    // Long.MAX_VALUE exceeds Integer range → NumberFormatException
    when(request.getParameter("EST-3")).thenReturn(String.valueOf(Long.MAX_VALUE));

    String view = cartController.updateCartQuantities(request, session, model);

    assertThat(view).isEqualTo("cart/Cart");
    assertThat(cart.getCartItemList()).hasSize(1);
    assertThat(cart.getCartItemList().get(0).getQuantity()).isEqualTo(1);
    @SuppressWarnings("unchecked")
    Map<String, String> errors = (Map<String, String>) model.asMap().get("quantityErrors");
    assertThat(errors).isNotNull().containsKey("EST-3");
    assertThat(errors.get("EST-3")).isNotBlank();
  }

  // -------------------------------------------------------------------------
  // Valid quantity
  // -------------------------------------------------------------------------

  /**
   * A valid positive quantity must update the item and produce no error map entry.
   */
  @Test
  void validQuantityUpdatesItemWithNoError() {
    Item item = makeItem("EST-4", "8.00");
    Cart cart = cartWith(item);

    HttpSession session = mock(HttpSession.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    Model model = new ExtendedModelMap();
    when(session.getAttribute("cart")).thenReturn(cart);
    when(request.getParameter("EST-4")).thenReturn("5");

    String view = cartController.updateCartQuantities(request, session, model);

    assertThat(view).isEqualTo("cart/Cart");
    assertThat(cart.getCartItemList()).hasSize(1);
    assertThat(cart.getCartItemList().get(0).getQuantity()).isEqualTo(5);
    assertThat(model.asMap()).doesNotContainKey("quantityErrors");
  }

  // -------------------------------------------------------------------------
  // Zero / negative — existing removal behaviour preserved
  // -------------------------------------------------------------------------

  /**
   * A zero quantity must still remove the item (original behaviour preserved).
   */
  @Test
  void zeroQuantityRemovesItem() {
    Item item = makeItem("EST-5", "3.00");
    Cart cart = cartWith(item);

    HttpSession session = mock(HttpSession.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    Model model = new ExtendedModelMap();
    when(session.getAttribute("cart")).thenReturn(cart);
    when(request.getParameter("EST-5")).thenReturn("0");

    String view = cartController.updateCartQuantities(request, session, model);

    assertThat(view).isEqualTo("cart/Cart");
    assertThat(cart.getCartItemList()).isEmpty();
    assertThat(model.asMap()).doesNotContainKey("quantityErrors");
  }

  /**
   * A negative quantity must still remove the item (original behaviour preserved).
   */
  @Test
  void negativeQuantityRemovesItem() {
    Item item = makeItem("EST-6", "3.00");
    Cart cart = cartWith(item);

    HttpSession session = mock(HttpSession.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    Model model = new ExtendedModelMap();
    when(session.getAttribute("cart")).thenReturn(cart);
    when(request.getParameter("EST-6")).thenReturn("-2");

    String view = cartController.updateCartQuantities(request, session, model);

    assertThat(view).isEqualTo("cart/Cart");
    assertThat(cart.getCartItemList()).isEmpty();
    assertThat(model.asMap()).doesNotContainKey("quantityErrors");
  }

  // -------------------------------------------------------------------------
  // Per-item independence (one valid + one invalid)
  // -------------------------------------------------------------------------

  /**
   * When one item has a valid quantity and another has an invalid quantity, each must be processed independently: the
   * valid item is updated, the invalid item retains its previous quantity, and only the invalid item has an error
   * entry.
   * <p>
   * This test will FAIL on the original code (no error is placed in the model).
   */
  @Test
  void validAndInvalidItemsAreProcessedIndependently() {
    Item item1 = makeItem("EST-10", "10.00");
    Item item2 = makeItem("EST-11", "20.00");
    Cart cart = new Cart();
    cart.addItem(item1, true); // qty 1
    cart.addItem(item2, true); // qty 1

    HttpSession session = mock(HttpSession.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    Model model = new ExtendedModelMap();
    when(session.getAttribute("cart")).thenReturn(cart);
    when(request.getParameter("EST-10")).thenReturn("4"); // valid
    when(request.getParameter("EST-11")).thenReturn("xyz"); // invalid

    String view = cartController.updateCartQuantities(request, session, model);

    assertThat(view).isEqualTo("cart/Cart");
    // EST-10 updated
    assertThat(cart.getCartItemList().stream().filter(ci -> ci.getItem().getItemId().equals("EST-10")).findFirst().get()
        .getQuantity()).isEqualTo(4);
    // EST-11 retained at 1
    assertThat(cart.getCartItemList().stream().filter(ci -> ci.getItem().getItemId().equals("EST-11")).findFirst().get()
        .getQuantity()).isEqualTo(1);
    // only EST-11 has an error
    @SuppressWarnings("unchecked")
    Map<String, String> errors = (Map<String, String>) model.asMap().get("quantityErrors");
    assertThat(errors).isNotNull().containsKey("EST-11").doesNotContainKey("EST-10");
  }

  // -------------------------------------------------------------------------
  // Stale cart-map regression: remove then re-add
  // -------------------------------------------------------------------------

  /**
   * Removing an item via {@code removeItemById} must remove it from both the internal map and list, so that a
   * subsequent {@code addItem} with the same item ID adds a fresh entry at quantity 1 rather than silently inheriting a
   * stale map entry.
   * <p>
   * This is a guard test for the structural correctness of {@link Cart}: it checks that the domain object itself
   * prevents a stale-entry problem, and it explicitly records that no repair was required in the production source for
   * this path.
   */
  @Test
  void removeItemThenReAddStartsAtQuantityOne() {
    Item item = makeItem("EST-20", "15.00");
    Cart cart = new Cart();
    cart.addItem(item, true); // qty 1

    // Simulate "Update Cart" with 0 → removes
    HttpSession session = mock(HttpSession.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    Model model = new ExtendedModelMap();
    when(session.getAttribute("cart")).thenReturn(cart);
    when(request.getParameter("EST-20")).thenReturn("0");
    cartController.updateCartQuantities(request, session, model);

    assertThat(cart.containsItemId("EST-20")).as("Item must not be in cart after zero-quantity update").isFalse();
    assertThat(cart.getCartItemList()).isEmpty();

    // Re-add the same item (simulates user clicking "Add to Cart" again)
    cart.addItem(item, true);

    assertThat(cart.containsItemId("EST-20")).isTrue();
    assertThat(cart.getCartItemList()).hasSize(1);
    assertThat(cart.getCartItemList().get(0).getQuantity())
        .as("Re-added item must start at quantity 1, not carry a stale quantity").isEqualTo(1);
  }

  // -------------------------------------------------------------------------
  // No errors map when all inputs are valid
  // -------------------------------------------------------------------------

  /**
   * When all items have valid quantities, the quantityErrors attribute must not be added to the model at all (absent,
   * not empty map).
   */
  @Test
  void noErrorsAttributeWhenAllQuantitiesAreValid() {
    Item item1 = makeItem("EST-30", "10.00");
    Item item2 = makeItem("EST-31", "5.00");
    Cart cart = new Cart();
    cart.addItem(item1, true);
    cart.addItem(item2, true);

    HttpSession session = mock(HttpSession.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    Model model = new ExtendedModelMap();
    when(session.getAttribute("cart")).thenReturn(cart);
    when(request.getParameter("EST-30")).thenReturn("2");
    when(request.getParameter("EST-31")).thenReturn("3");

    cartController.updateCartQuantities(request, session, model);

    assertThat(model.asMap()).doesNotContainKey("quantityErrors");
  }
}
