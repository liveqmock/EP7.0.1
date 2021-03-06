package com.elasticpath.cortex.dce.purchases

import cucumber.api.DataTable
import cucumber.api.groovy.EN
import cucumber.api.groovy.Hooks

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.CommonAssertion.assertCost
import static com.elasticpath.cortex.dce.CommonAssertion.assertAddress
import static com.elasticpath.cortex.dce.SharedConstants.*
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkExists

import static org.assertj.core.api.Assertions.assertThat

this.metaClass.mixin(Hooks)
this.metaClass.mixin(EN)

def final ADDRESS_FIELD = 'address'

final Map<String, String> ADDRESS = ["country-name": "CA", "locality": "Vancouver", "postal-code": "v7v7v7", "region": "BC", "street-address": "1111 EP Road"]

def final CARRIER_FIELD = 'carrier'

When(~'^I make a purchase$') { ->
	client.submitPurchase()
			.stopIfFailure()
}

Given(~'^I have previously made a purchase with \"(\\d+?)\" (?:physical|digital|bundle) item \"([^\"]+)\"$') {
	int quantity, String productName ->

		client.search(productName)
				.addToCart(quantity)
		client.selectAnyShippingOption()
				.submitPurchase()
				.follow()
				.stopIfFailure()
}

When(~'^I view the shipment line items') { ->
	client.shipments()
			.element()
			.lineitems()
			.stopIfFailure()
}

Then(~'^I see "(\\d+?)" shipment line items$') { int numLineItems ->
	assertThat(client.body.links.findAll { link -> link.rel == "element" })
			.size()
			.as("Number of shipment line items is not as expected")
			.isEqualTo(numLineItems)
}

And(~'^I see a back link to the shipment$') { ->
	client.shipment()
			.stopIfFailure()
}

When(~'^I view the shipment line item for item "(.+?)"$') { String productName ->
	client.shipments()
			.element()
			.lineitems()
			.findElement { lineitem ->
		lineitem[NAME_FIELD] == productName
	}
	.stopIfFailure()
}
Then(~'^I should see purchase line item configurable fields for item (.+) as:$') { String itemName, DataTable itemDetailsTable ->
	def Map<String, String> itemDetails = itemDetailsTable.asMap(String, String)

	client.GET("/")
			.defaultprofile()
			.purchases()
			.element()
			.lineitems()
			.findElement { lineitem ->
		lineitem[NAME_FIELD] == itemName
	}
	.stopIfFailure()

	for (Map.Entry<String, String> itemDetail : itemDetails.entrySet()) {
		assertThat(client.body.'configuration'.(itemDetail.key).toString())
				.as(itemDetail.key + " is not as expected")
				.isEqualTo(itemDetail.value)
	}
}

Then(~'^I open purchase line item (.+)$') { String itemName ->

	client.GET("/")
			.defaultprofile()
			.purchases()
			.element()
			.lineitems()
			.findElement { lineitem ->
		lineitem[NAME_FIELD] == itemName
	}
	.stopIfFailure()

}

Then(~'^I see the quantity "(\\d+?)" and name "(.+?)" fields on the shipment line item$') {
	int quantity, String productName ->

		assertThat(client.body.quantity)
				.as("Quantity is not as expected")
				.isEqualTo(quantity)
		assertThat(client.body.name.toString())
				.as("Name is not as expected")
				.isEqualTo(productName)
}

And(~'^I see a back link to the list of shipment line items$') { ->
	assertThat(client.body.links.findAll { link -> link.rel == "list" })
			.size()
			.as("Link back to the list of shipment line items was not found")
			.isEqualTo(1)
}

Then(~'^I can follow the shipment line item price link$') { ->
	client.price()
			.stopIfFailure()
}

And(~'^the purchase-price has fields amount: (.+?), currency: (.+?) and display: (.+?)$') {
	String expectedAmount, String expectedCurrency, String expectedDisplayName ->
		def costElement = client.body.'purchase-price'
		assertCost(costElement, expectedAmount, expectedCurrency, expectedDisplayName)
}

Then(~'^purchase item monetary total has fields amount: (.+), currency: (.+) and display: (.+)$') {
	def expectedAmount, def expectedCurrency, def expectedDisplayName ->
		def costElement = client.body.'monetary-total'
		assertCost(costElement, expectedAmount, expectedCurrency, expectedDisplayName)
}

Then(~'^purchase item tax total has fields amount: (.+), currency: (.+) and display: (.+)$') {
	def expectedAmount, def expectedCurrency, def expectedDisplayName ->
		def taxElement = client.body.'tax-total'
		assertCost(taxElement, expectedAmount, expectedCurrency, expectedDisplayName)
}

And(~'^I see the cost field has amount: (.+?), currency: (.+?) and display: (.+?)$') {
	String expectedAmount, String expectedCurrency, String expectedDisplayName ->
		def costElement = client.body.cost
		assertCost(costElement, expectedAmount, expectedCurrency, expectedDisplayName)
}

And(~'^I see the total field has amount: (.+?), currency: (.+?) and display: (.+?)$') {
	String expectedAmount, String expectedCurrency, String expectedDisplayName ->
		def costElement = client.body.total
		assertCost(costElement, expectedAmount, expectedCurrency, expectedDisplayName)
}

And(~'^I see the tax type is (.+) currency is (.+) cost is (.+)$') { taxType, currency, amount ->
	assertThat(client["cost"].find {
		it ->
			it.title == taxType && it.currency == currency
	}.display)
			.as("Display is not as expected")
			.isEqualTo(amount)
}

And(~'^I can follow the back link to the shipment line item$') { ->
	assertThat(client.body.links.findAll { link -> link.rel == "lineitem" })
			.size()
			.as("Link back to the shipment line item was not found")
			.isEqualTo(1)
	client.lineitem()
			.stopIfFailure()
}

When(~'^I have previously made a purchase with a physical item (.+) in a tax free state code is (.+)$') { String productName, String taxFreePostCode ->
	client
			.addNewUSAddressForZeroTaxState()
			.search(productName)
			.addToCart(1)
			.selectShippingAddressByPostalCode(taxFreePostCode)
			.selectAnyShippingOption()
			.submitPurchase()
			.follow()
			.stopIfFailure()
}

When(~'^I view the details of a line item (.+) in a shipment$') { String productName ->
	client.findElement { lineitem ->
		lineitem[NAME_FIELD] == productName
	}
	.stopIfFailure()
}

When(~'^I navigate to shipment$') { ->
	client.shipments()
			.element()
			.stopIfFailure()
}

When(~'^I navigate to the billing address') { ->
	client.billingaddress()
			.stopIfFailure()
}

Then(~'^I can see the shipment status (.+)$') { String shipmentStatus ->
	assertThat(client.body.status.code.toString())
			.as("The shipment status is not as expected")
			.isEqualTo(shipmentStatus)
}

Then(~'^I can follow a link back to the list of shipments') { ->
	client.list()
			.stopIfFailure()
}
Then(~'^I can follow a link back to the purchase') { ->
	client.purchase()
			.stopIfFailure()
}

When(~'^I follow the shipping address link$') { ->
	client.destination()
			.stopIfFailure()
}
When(~'^I follow the shipment tax link$') { ->
	client.tax()
			.stopIfFailure()
}

When(~'^I navigate to the shipment line items$') { ->
	client.shipments()
			.element()
			.lineitems()
			.stopIfFailure()
}

Given(~'^I have purchased physical and digital items: "(.+?)" "(.+?)" "(.+?)"$') {
	String prod1, String prod2, String prod3 ->

		List<String> productList = new ArrayList<String>()
		productList.add(prod1)
		productList.add(prod2)
		productList.add(prod3)

		for (String product : productList) {
			client.search(product)
					.addToCart(1)
		}

		client.selectAnyShippingOption()
				.submitPurchase()
				.follow()
				.stopIfFailure()
}

And(~'^I can follow the line item link for product "(.+?)" and back to the list$') { String productName ->
	client.findElement { lineitem ->
		lineitem[NAME_FIELD] == productName
	}
	.list()
			.stopIfFailure()
}


def isProductLinkExists(def prodName) {
	def prodExists = false
	client.body.links.findAll {
		if (it.rel == "element") {
			client.GET(it.uri)
			if (client["name"] == prodName) {
				prodExists = true
			}
		}
	}
	return prodExists
}

And(~'I do not see a link to line item "(.+?)"$') { String prodName ->
	assertThat(isProductLinkExists(prodName))
			.as("A link to $prodName should not exist")
			.isFalse()
}

Given(~'^I have previously made a purchase with item "(.+?)" quantity "(.+?)" and item "(.+?)" quantity "(.+?)"$') {
	def prod1, def qty1, def prod2, def qty2 ->

		client.search(prod1)
				.addToCart(qty1)
				.search(prod2)
				.addToCart(qty2)

		client.selectAnyShippingOption()
				.submitPurchase()
				.follow()
				.stopIfFailure()
}

And(~'^I go back to the purchase$') { ->
	client.list()
			.shipment()
			.list()
			.purchase()
			.stopIfFailure()
}

Then(~'^I see (.+?) address$') { String name ->
	assertAddress(client[ADDRESS_FIELD], ADDRESS)
}

Then(~'^I can follow a link back to the shipment$') { ->
	client.shipment()
			.stopIfFailure()
}

Then(~'^I follow the shipping option link$') { ->
	client.shippingoption()
			.stopIfFailure()
}

Then(~'^I follow the shipment total link$') { ->
	client.total()
			.stopIfFailure()
}

Then(~'^I see shipping (.+) carrier information (.+)') { String shippingDisplayName, String carrierName ->
	assertThat(client[DISPLAY_NAME_FIELD].toString())
			.as("The display name is not as expected")
			.isEqualTo(shippingDisplayName)
	assertThat(client[CARRIER_FIELD].toString())
			.as("The shipping carrier is not as expected")
			.isEqualTo(carrierName)
}

Then(~'^I can follow the link to options for that shipment line item$') { ->
	assertLinkExists(client, "options")
	client.options()
			.stopIfFailure()
}

Then(~'^I view purchase line item option (.+)$') { String optionDisplayName ->
	client.GET("/")
			.defaultprofile()
			.purchases()
			.element()
			.lineitems()
			.element()
			.options()
	client.findElement { option ->
		option[DISPLAY_NAME_FIELD] == optionDisplayName
	}
	.stopIfFailure()
}

And(~'^I should see item option value is (.+)$') { String valueDisplayName ->
	client.value()
			.stopIfFailure()
	assertThat(client[DISPLAY_NAME_FIELD].toString())
			.as("The display name is not as expected")
			.isEqualTo(valueDisplayName)
}

And(~'^I can view the \"([^\"]+)\" option for that shipment line item$') {
	String optionDisplayName ->

		client.findElement { option ->
			option[DISPLAY_NAME_FIELD] == optionDisplayName
		}
		.stopIfFailure()
}

And(~'^I can view the \"([^\"]+)\" value for that option$') {
	String valueDisplayName ->

		client.value()
				.stopIfFailure()
		assertThat(client[DISPLAY_NAME_FIELD].toString())
				.as("The display name is not as expected")
				.isEqualTo(valueDisplayName)
}

And(~'^I can follow back links from a shipment option value all the way to the purchase$') { ->
	client.option()
			.list()
			.lineitem()
			.list()
			.shipment()
			.list()
			.purchase()
			.stopIfFailure()
}

When(~'^I go to the purchases$') { ->
	client.GET("/")
			.defaultprofile()
			.purchases()
			.element()
			.stopIfFailure()
}

Then(~'^I should see purchase status (.+)$') { String valueStatus ->
	assertThat(client["status"])
			.as("The purchase status is not as expected")
			.isEqualTo(valueStatus)
}

Then(~'I should not see any element under Shipment') { ->
	client.shipments()
	assertLinkDoesNotExist(client, "element")
}

Then(~'I do not see a link to options on a single sku item') { ->
	assertLinkDoesNotExist(client, "options")
}

Then(~'^I can follow the total link$') { ->
	client.total()
			.stopIfFailure()
}

And(~'^I can follow line item back links all the way to the purchase$') { ->
	client.lineitem()
			.list()
			.shipment()
			.list()
			.purchase()
			.stopIfFailure()
}

Then(~'^I can follow the price link$') { ->
	client.price()
			.stopIfFailure()
}

And(~'^I can follow the back link to the shipment line item option$') { ->
	client.option()
			.stopIfFailure()
}

And(~'^I can follow the back link to the shipment line item options list$') { ->
	client.list()
			.stopIfFailure()
}

When(~'^Adding an item with item code (.+) and quantity (.*) to the cart$') {
	def itemCode, def quantity ->
		client.GET("/")
				.searches()
				.keywordsearchform()
				.itemkeywordsearchaction(
				[
						'keywords' : itemCode,
						'page-size': 5
				])
				.follow()
				.element()
				.addtocartform()
				.addtodefaultcartaction(quantity: quantity)
				.stopIfFailure()
}

Then(~'the order is submitted') { ->
	createdPurchase = client.GET("/")
			.defaultcart()
			.order()
			.purchaseform()
			.submitorderaction()
			.follow()
			.save()
}

Then(~'the payment token created is used for the order') { ->
	client.paymentmeans()
			.element()
			.stopIfFailure()

	assertThat(client["display-name"])
			.as("Token display name is not as expected")
			.isEqualTo(TEST_TOKEN_DISPLAY_NAME)
}

And(~'^The store the shopper is in fulfils shipments with no delay$') { ->
	// This can't be controlled for Cortex, but the default store's warehouse has a pick-pack delay of 0
}