/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.domain.shoppingcart.impl;

import java.io.Serializable;
import java.math.BigDecimal;

import com.elasticpath.domain.rules.RuleAction;
import com.elasticpath.domain.shoppingcart.DiscountRecord;
import com.elasticpath.domain.shoppingcart.ShoppingCart;

/**
 * Represents a discount applied to a cart. Discounts can either be per item (use {ItemDiscountRecordImpl}),
 * for a subtotal (use {SubtotalDiscountRecordImpl}) or for shipping (use {ShippingDiscountRecordImpl}).
 */
public abstract class AbstractDiscountRecordImpl implements DiscountRecord, Serializable {

	/** Serial Version ID. **/
	private static final long serialVersionUID = 5000000002L;

	private final long ruleId;
	private final long actionId;
	private final BigDecimal discountAmount;
	private boolean superceded;

	/**
	 * Constructor.
	 *
	 * @param ruleId the ID of the rule that applied this discount
	 * @param actionId the ID of the rule action that applied this discount
	 * @param discountAmount The amount of this discount per discount record
	 */
	public AbstractDiscountRecordImpl(final long ruleId, final long actionId, final BigDecimal discountAmount) {
		this.ruleId = ruleId;
		this.actionId = actionId;
		this.discountAmount = discountAmount;
	}

	/**
	 * Calculates the number of coupon uses required for this discount to have been
	 * created by {@code action}.
	 * 
	 * @param action The action.
	 * @param shoppingCart the cart for cart related decisions
	 * @return The number of coupon uses required.
	 */
	@Override
	public abstract int getCouponUsesRequired(RuleAction action, ShoppingCart shoppingCart);

	/**
	 * 
	 * @return The dollar value of the discount.
	 */
	protected BigDecimal getDiscountAmount() {
		return discountAmount;
	}
	
	@Override
	public boolean isSuperceded() {
		return superceded;
	}

	/**
	 * 
	 * @param superceded The new valued for superceded.
	 */
	public void setSuperceded(final boolean superceded) {
		this.superceded = superceded;
	}

	@Override
	public long getRuleId() {
		return ruleId;
	}

	@Override
	public long getActionId() {
		return actionId;
	}

}
