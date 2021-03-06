/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.integration.epcommerce.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.shoppingcart.PriceCalculator;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItemTaxSnapshot;
import com.elasticpath.money.Money;
import com.elasticpath.rest.ResourceTypeFactory;
import com.elasticpath.rest.command.ExecutionResultFactory;
import com.elasticpath.rest.definition.base.CostEntity;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemConfigurationEntity;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartItemModifiersRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.PricingSnapshotRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.transform.MoneyTransformer;

/**
 * The Class OrderSkuTransformer.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderSkuTransformerTest {
	private static final double EXPECTED_TAX = 11.0;
	private static final String DISPLAY_NAME = "display name";
	private static final int QUANTITY = 6;
	private static final int PARENT_QUANTITY = 2;
	private static final Locale LOCALE_EN = Locale.ENGLISH;
	private static final BigDecimal AMOUNT = new BigDecimal("111.11");
	private static final BigDecimal TAX = new BigDecimal("12.12");
	private static final BigDecimal TOTAL = new BigDecimal("123.23");
	private static final Currency CURRENCY = Currency.getInstance(Locale.CANADA);
	private static final String SKU_GUID = "sku_guid";

	@Mock
	private MoneyTransformer mockitoMoneyTransformer;
	@Mock
	private ProductSkuRepository mockitoProductSkuRepository;
	@Mock
	private PricingSnapshotRepository pricingSnapshotRepository;
	@Mock
	private CartItemModifiersRepository cartItemModifiersRepository;

	@InjectMocks
	private OrderSkuTransformer mockitoOrderSkuTransformer;

	/**
	 * Test transform to domain.
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testTransformToDomain() {
		mockitoOrderSkuTransformer.transformToDomain(null);
	}

	/**
	 * Test transform to entity on an order sku that is a non bundle component.
	 */
	@Test
	public void testTransformToEntityOrderSkuAsNonBundleComponent() {
		final Money amountMoney = Money.valueOf(AMOUNT, CURRENCY);
		final Money taxMoney = Money.valueOf(TAX, CURRENCY);
		final Money totalMoney = Money.valueOf(TOTAL, CURRENCY);

		final CostEntity amountCostEntity = ResourceTypeFactory.createResourceEntity(CostEntity.class);
		final CostEntity taxCostEntity = ResourceTypeFactory.createResourceEntity(CostEntity.class);
		final CostEntity totalCostEntity = ResourceTypeFactory.createResourceEntity(CostEntity.class);
	   	final PurchaseLineItemConfigurationEntity  purchaseLineItemConfiguration =
				ResourceTypeFactory.createResourceEntity(PurchaseLineItemConfigurationEntity.class);

		when(mockitoMoneyTransformer.transformToEntity(amountMoney, LOCALE_EN)).thenReturn(amountCostEntity);
		when(mockitoMoneyTransformer.transformToEntity(taxMoney, LOCALE_EN)).thenReturn(taxCostEntity);
		when(mockitoMoneyTransformer.transformToEntity(totalMoney, LOCALE_EN)).thenReturn(totalCostEntity);

		PurchaseLineItemEntity expectedPurchaseLineItemEntity = createPurchaseLineItemDto(Collections.singletonList(amountCostEntity),
				Collections.singletonList(taxCostEntity),
				Collections.singletonList(totalCostEntity), purchaseLineItemConfiguration);

		OrderSku orderSku = createMockOrderSku(amountMoney, taxMoney);
		final ShoppingItemPricingSnapshot pricingSnapshot = mock(ShoppingItemPricingSnapshot.class, RETURNS_DEEP_STUBS);

		when(pricingSnapshotRepository.getPricingSnapshotForOrderSku(orderSku)).thenReturn(ExecutionResultFactory.createReadOK(pricingSnapshot));
		when(pricingSnapshot.getPriceCalc().withCartDiscounts().getMoney()).thenReturn(amountMoney);

		PurchaseLineItemEntity purchaseLineItemEntity = mockitoOrderSkuTransformer.transformToEntity(orderSku, LOCALE_EN);

		assertEquals("The purchase line item DTOs should be the same.", expectedPurchaseLineItemEntity, purchaseLineItemEntity);
	}

	/**
	 * Test transform to entity from order sku that is a bundle component.
	 */
	@Test
	public void testTransformToEntityOrderSkuForLineItemComponent() {
		final OrderSku mockOrderSku = mock(OrderSku.class, "componentSku");
		final OrderSku mockParentOrderSku = mock(OrderSku.class, "parentSku");
		final ProductSku mockProductSku = mock(ProductSku.class);

		when(mockOrderSku.getDisplayName()).thenReturn(DISPLAY_NAME);
		when(mockOrderSku.getQuantity()).thenReturn(QUANTITY);
		when(mockOrderSku.getParent()).thenReturn(mockParentOrderSku);

		when(mockParentOrderSku.getQuantity()).thenReturn(PARENT_QUANTITY);
		when(mockOrderSku.getSkuGuid()).thenReturn(SKU_GUID);

		when(mockitoProductSkuRepository.getProductSkuWithAttributesByGuid(SKU_GUID)).thenReturn(ExecutionResultFactory.createReadOK(mockProductSku));

		PurchaseLineItemEntity purchaseLineItemEntity = mockitoOrderSkuTransformer.transformToEntity(mockOrderSku, LOCALE_EN);

		int expectedQuantity = QUANTITY / PARENT_QUANTITY;
		assertEquals("Quantity should be component's quantity divided by the parent's quantity.",
				expectedQuantity,
				purchaseLineItemEntity.getQuantity().intValue());
		assertNull("Amount should match.", purchaseLineItemEntity.getLineExtensionAmount());
		assertNull("Tax should match.", purchaseLineItemEntity.getLineExtensionTax());
		assertNull("Total should match.", purchaseLineItemEntity.getLineExtensionTotal());
	}

	/**
	 * Test for calculating tax for non-bundle.
	 */
	@Test
	public void testCalculateTaxForNonBundle() {
		final OrderSku orderSku = mock(OrderSku.class);
		final ShoppingItemTaxSnapshot taxSnapshot = mock(ShoppingItemTaxSnapshot.class);
		final Money expectedTax = Money.valueOf(BigDecimal.ZERO, CURRENCY);

		when(orderSku.getSkuGuid()).thenReturn("blubber");
		when(pricingSnapshotRepository.getTaxSnapshotForOrderSku(orderSku)).thenReturn(ExecutionResultFactory.createReadOK(taxSnapshot));
		when(taxSnapshot.getTaxAmount()).thenReturn(expectedTax.getAmount());
		when(mockitoProductSkuRepository.isProductBundle(orderSku.getSkuGuid())).thenReturn(ExecutionResultFactory.createReadOK(false));

		Money tax = mockitoOrderSkuTransformer.getTax(orderSku, CURRENCY);

		assertEquals("The taxes should be the same.", expectedTax, tax);
	}

	/**
	 * Test for calculating tax for bundle with constituent.
	 */
	@Test
	public void testCalculateTaxForBundles() {
		final OrderSku mockRootItem = mock(OrderSku.class, "root");
		final ShoppingItemTaxSnapshot rootItemTaxSnapshot = mock(ShoppingItemTaxSnapshot.class);
		final Money expectedParentTax = Money.valueOf(BigDecimal.ONE, CURRENCY);
		final OrderSku mockChildItem = mock(OrderSku.class, "child");
		final ShoppingItemTaxSnapshot childItemTaxSnapshot = mock(ShoppingItemTaxSnapshot.class);
		final Money expectedChildTax = Money.valueOf(BigDecimal.TEN, CURRENCY);

		when(mockitoProductSkuRepository.isProductBundle(mockRootItem.getSkuGuid())).thenReturn(ExecutionResultFactory.createReadOK(true));
		when(pricingSnapshotRepository.getTaxSnapshotForOrderSku(mockRootItem)).thenReturn(ExecutionResultFactory.createReadOK(rootItemTaxSnapshot));
		when(rootItemTaxSnapshot.getTaxAmount()).thenReturn(expectedParentTax.getAmount());
		when(mockRootItem.getChildren()).thenReturn(Collections.<ShoppingItem>singletonList(mockChildItem));

		final List<ShoppingItem> emptySet = Collections.emptyList();

		when(mockitoProductSkuRepository.isProductBundle(mockRootItem.getSkuGuid())).thenReturn(ExecutionResultFactory.createReadOK(true));
		when(pricingSnapshotRepository.getTaxSnapshotForOrderSku(mockChildItem))
			.thenReturn(ExecutionResultFactory.createReadOK(childItemTaxSnapshot));
		when(childItemTaxSnapshot.getTaxAmount()).thenReturn(expectedChildTax.getAmount());
		when(mockChildItem.getChildren()).thenReturn(emptySet);

		Money tax = mockitoOrderSkuTransformer.getTax(mockRootItem, CURRENCY);

		Money expectedTax = Money.valueOf(BigDecimal.valueOf(EXPECTED_TAX), CURRENCY);
		assertEquals(expectedTax, tax);
	}

	private OrderSku createMockOrderSku(final Money amount, final Money tax) {
		final OrderSku orderSku = mock(OrderSku.class);
		final ShoppingItemPricingSnapshot pricingSnapshot = mock(ShoppingItemPricingSnapshot.class);
		final ShoppingItemTaxSnapshot taxSnapshot = mock(ShoppingItemTaxSnapshot.class);
		final OrderShipment orderShipment = mock(OrderShipment.class);

		// Time to mock: orderSku.getPriceCalc().withCartDiscounts().getMoney(); ... good times.
		final PriceCalculator priceCalculator = mock(PriceCalculator.class, "outerPriceCalc");
		final PriceCalculator discountsPriceCalculator = mock(PriceCalculator.class, "withDiscountsPriceCalc");

		when(orderSku.getDisplayName()).thenReturn(DISPLAY_NAME);

		when(orderSku.getQuantity()).thenReturn(QUANTITY);

		when(pricingSnapshotRepository.getTaxSnapshotForOrderSku(orderSku)).thenReturn(ExecutionResultFactory.createReadOK(taxSnapshot));

		when(pricingSnapshot.getPriceCalc()).thenReturn(priceCalculator);
		//times 2

		when(priceCalculator.withCartDiscounts()).thenReturn(discountsPriceCalculator);
		//times 2

		when(discountsPriceCalculator.getMoney()).thenReturn(amount);
		//times 2

		when(orderSku.getShipment()).thenReturn(orderShipment);

		when(orderShipment.isInclusiveTax()).thenReturn(false);

		when(mockitoProductSkuRepository.isProductBundle(orderSku.getSkuGuid())).thenReturn(ExecutionResultFactory.createReadOK(false));

		when(orderSku.getParent()).thenReturn(null);

		when(taxSnapshot.getTaxAmount()).thenReturn(tax.getAmount());

		when(orderSku.getCurrency()).thenReturn(CURRENCY);

		when(orderSku.getSkuGuid()).thenReturn(SKU_GUID);

		when(mockitoProductSkuRepository.isProductBundle(SKU_GUID)).
				thenReturn(ExecutionResultFactory.createReadOK(true));

		createMockConfiguration();

		return orderSku;
	}

	private void createMockConfiguration() {
		//mock productSku/Product/Type/CartItemModifier relationships
		final ProductSku productSku = mock(ProductSku.class);
		final Product product = mock(Product.class);

		when(mockitoProductSkuRepository.getProductSkuWithAttributesByGuid(SKU_GUID)).thenReturn(ExecutionResultFactory.createReadOK(productSku));
		when(cartItemModifiersRepository.findCartItemModifiersByProduct(product)).thenReturn(Collections.emptyList());
	}

	private PurchaseLineItemEntity createPurchaseLineItemDto(final List<CostEntity> amountCostEntities,
			final List<CostEntity> taxCostEntities,
			final List<CostEntity> totalCostEntities,
			final PurchaseLineItemConfigurationEntity purchaseLineItemConfigurationEntity) {

		return PurchaseLineItemEntity.builder()
				.withName(DISPLAY_NAME)
				.withQuantity(QUANTITY)
				.withLineExtensionAmount(amountCostEntities)
				.withLineExtensionTax(taxCostEntities)
				.withLineExtensionTotal(totalCostEntities)
				.withConfiguration(purchaseLineItemConfigurationEntity)
				.build();
	}
}