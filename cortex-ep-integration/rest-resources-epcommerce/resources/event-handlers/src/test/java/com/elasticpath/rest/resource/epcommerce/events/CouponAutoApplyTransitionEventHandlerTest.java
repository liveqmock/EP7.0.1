/**
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.epcommerce.events;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.relos.rs.events.RoleTransitionEvent;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;

@RunWith(MockitoJUnitRunner.class)
public class CouponAutoApplyTransitionEventHandlerTest {
	private static final String EMAIL = "EMAIL";

	private static final String SHOPPING_CART_GUID = "SHOPPING_CART_GUID";

	private static final String NEW_USER_GUID = "NEW_USER_GUID";

	private static final String STORE_CODE = "STORE_CODE";

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private CouponAutoApplyTransitionEventHandler couponAutoApplyTransitionEventHandler;

	@Mock
	private RoleTransitionEvent mockEvent;

	@Mock
	private CartOrder mockCartOrder;

	@Mock
	private Store mockStore;

	@Before
	public void setUp() {
		when(mockEvent.getNewUserGuid()).thenReturn(NEW_USER_GUID);

		Customer mockCustomer = mock(Customer.class);
		when(customerRepository.findCustomerByGuid(NEW_USER_GUID)).thenReturn(ExecutionResultFactory.createReadOK(mockCustomer));
		when(mockCustomer.getEmail()).thenReturn(EMAIL);

		ShoppingCart mockShoppingCart = mock(ShoppingCart.class, Answers.RETURNS_DEEP_STUBS.get());
		when(shoppingCartRepository.getShoppingCart(NEW_USER_GUID, STORE_CODE))
				.thenReturn(ExecutionResultFactory.createReadOK(mockShoppingCart));
		when(mockShoppingCart.getShopper().getCustomer()).thenReturn(mockCustomer);
		when(mockShoppingCart.getGuid()).thenReturn(SHOPPING_CART_GUID);

		when(storeRepository.findStore(STORE_CODE)).thenReturn(ExecutionResultFactory.createReadOK(mockStore));

		when(cartOrderRepository.saveCartOrder(mockCartOrder)).thenReturn(ExecutionResultFactory.createReadOK(mockCartOrder));
	}

	@Test
	public void testCartOrderIsSavedWhenCouponAutoApplierUpdatesCartOrder() throws Exception {
		allowingRepositoryToFindCartOrder(true);
		when(cartOrderRepository.filterAndAutoApplyCoupons(mockCartOrder, mockStore, EMAIL)).thenReturn(ExecutionResultFactory.createReadOK(true));

		couponAutoApplyTransitionEventHandler.handleEvent(STORE_CODE, mockEvent);

		verifyMocks();
		verify(storeRepository).findStore(STORE_CODE);
		verify(cartOrderRepository).filterAndAutoApplyCoupons(any(CartOrder.class), any(Store.class), eq(EMAIL));
		verify(cartOrderRepository).saveCartOrder(mockCartOrder);
	}


	@Test
	public void testCartOrderIsNotSavedWhenNoCouponsAreAutoAppliedToCartOrder() throws Exception {
		allowingRepositoryToFindCartOrder(true);
		when(cartOrderRepository.filterAndAutoApplyCoupons(mockCartOrder, mockStore, EMAIL)).thenReturn(ExecutionResultFactory.createReadOK(false));

		couponAutoApplyTransitionEventHandler.handleEvent(STORE_CODE, mockEvent);

		verifyMocks();
		verify(storeRepository).findStore(STORE_CODE);
		verify(cartOrderRepository).filterAndAutoApplyCoupons(any(CartOrder.class), any(Store.class), eq(EMAIL));
		verify(cartOrderRepository, never()).saveCartOrder(mockCartOrder);
	}

	@Test
	public void testCartOrderNotSavedWhenCartOrderNotFound() throws Exception {
		allowingRepositoryToFindCartOrder(false);

		couponAutoApplyTransitionEventHandler.handleEvent(STORE_CODE, mockEvent);

		verifyMocks();
		verify(storeRepository, never()).findStore(STORE_CODE);
		verify(cartOrderRepository, never()).filterAndAutoApplyCoupons(any(CartOrder.class), any(Store.class), eq(EMAIL));
		verify(cartOrderRepository, never()).saveCartOrder(mockCartOrder);
	}

	private void allowingRepositoryToFindCartOrder(final boolean findCartOrder) {
		if (findCartOrder) {
			when(cartOrderRepository.findByCartGuid(SHOPPING_CART_GUID)).thenReturn(ExecutionResultFactory.createReadOK(mockCartOrder));
		} else {
			when(cartOrderRepository.findByCartGuid(SHOPPING_CART_GUID)).thenReturn(ExecutionResultFactory.<CartOrder> createNotFound());
		}
	}

	private void verifyMocks() {
		verify(shoppingCartRepository).getShoppingCart(NEW_USER_GUID, STORE_CODE);
		verify(cartOrderRepository).findByCartGuid(SHOPPING_CART_GUID);
	}

}
