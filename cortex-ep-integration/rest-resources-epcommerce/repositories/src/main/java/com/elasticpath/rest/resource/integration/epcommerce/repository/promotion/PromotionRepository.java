/**
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.promotion;

import java.util.Collection;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.rules.Coupon;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.rest.command.ExecutionResult;

/**
 * The facade for {@link com.elasticpath.domain.rules.Coupon} related operations.
 */
public interface PromotionRepository {

	/**
	 * Find a Rule by id.
	 *
	 * @param ruleId The rule id.
	 * @return Rule - The rule.
	 */
	ExecutionResult<Rule> findByRuleId(Long ruleId);

	/**
	 * Find a rule by promotion ID.
	 *
	 * @param promotionId the promotion ID.
	 * @return the rule.
	 */
	ExecutionResult<Rule> findByPromotionId(String promotionId);

	/**
	 * <p>
	 * Gets the list of shipping promotions applied to a particular shipping
	 * service level in the context of a shopping cart.
	 * </p>
	 * <p>
	 * The context of the shopping cart is required because the out of the box
	 * shipping promotions only apply in the context of the shoppers cart.
	 * </p>
	 *
	 * @param cartPricingSnapshot  The shopping cart pricing snapshot.
	 * @param shippingServiceLevel The shipping service level.
	 * @return Promotion guids for shipping promos applied to the shipping
	 * service level in the context of the shopping cart.  Maybe be empty if no promotions apply.
	 */
	Collection<String> getAppliedShippingPromotions(ShoppingCartPricingSnapshot cartPricingSnapshot, ShippingServiceLevel shippingServiceLevel);

	/**
	 * <p>
	 * Get the list of applied promotions for a line item of a cart.
	 * </p>
	 * <p>
	 * NOTE: This method will only report on the promotions that exist on the ShoppingCart parameter as is.
	 * You likely want to pass in an enriched ShoppingCart to make effective use of this method.
	 * </p>
	 *
	 * @param shoppingCart The shopping cart must include promotion rule records as a precondition.
	 *                     This method will only report what already exists on the cart and will not modify the cart.
	 * @param lineItemId   line item id
	 * @param pricingSnapshot the pricing snapshot that corresponds to the provided shopping cart.  Contains promotional details.
	 * @return promotion ids
	 */
	Collection<String> getAppliedCartLineitemPromotions(ShoppingCart shoppingCart, ShoppingCartPricingSnapshot pricingSnapshot, String lineItemId);

	/**
	 * Get the list of applied promotions for a cart pricing snapshot.
	 *
	 * @param pricingSnapshot the pricing snapshot.
	 * @return promotion ids
	 */
	Collection<String> getAppliedCartPromotions(ShoppingCartPricingSnapshot pricingSnapshot);

	/**
	 * <p>
	 * Gets the list of promotions triggered by the given coupon
	 * in the context of a shopping cart.
	 * </p>
	 * <p>
	 * The context of the shopping cart is required because the out of the box
	 * coupon promotions only apply in the context of the shoppers cart.
	 * </p>
	 *
	 * @param pricingSnapshot The shopping cart pricing snapshot
	 * @param coupon       The coupon
	 * @return Promotion guids for promos triggered by the coupon in the context of the shopping cart.
	 * Maybe be empty if no promotions apply.
	 */
	Collection<String> getAppliedPromotionsForCoupon(ShoppingCartPricingSnapshot pricingSnapshot, Coupon coupon);

	/**
	 * <p>
	 * Gets the list of promotions triggered by the given coupon
	 * in the context of an order.
	 * </p>
	 * <p>
	 * The context of the order is required because the out of the box
	 * coupon promotions only apply in the context of the shoppers cart.
	 * </p>
	 *
	 * @param order  The order.
	 * @param coupon The coupon
	 * @return Promotion guids for promos triggered by the coupon in the context of the order.
	 * Maybe be empty if no promotions apply.
	 */
	Collection<String> getAppliedPromotionsForCoupon(Order order, Coupon coupon);

	/**
	 * <p>
	 * Gets the list of promotions applied to the order.
	 * </p>
	 *
	 * @param order The order.
	 * @return Promotion guids for promos applied to the order.
	 * Maybe be empty if no promotions apply.
	 */
	Collection<String> getAppliedPromotionsForPurchase(Order order);

	/**
	 * Gets the list of promotions that apply incentives on a product.
	 *
	 * @param storeCode the store code.
	 * @param itemId    the item ID.
	 * @return Promotion guids for promos triggering insentives on the product.
	 */
	Collection<String> getAppliedPromotionsForItem(String storeCode, String itemId);
}
