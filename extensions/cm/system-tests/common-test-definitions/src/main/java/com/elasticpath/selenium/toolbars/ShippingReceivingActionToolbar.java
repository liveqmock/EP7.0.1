package com.elasticpath.selenium.toolbars;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.dialogs.CompleteShipmentDialog;

/**
 * Shipping/Receiving Toolbar.
 */
public class ShippingReceivingActionToolbar extends AbstractToolbar {

	private static final String SELECT_WAREHOUSE_ICON_CSS = "div[widget-id='Select Warehouse'][appearance-id='toolbar-button']";
	private static final String WAREHOUSE_NAME_CSS = "div[widget-id='%s'][seeable='true']";
	private static final String COMPLETE_SHIPMENT_BUTTON_CSS = "div[widget-id='Complete Shipment']"; //TODO to be changed

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public ShippingReceivingActionToolbar(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Selects warehouse.
	 *
	 * @param warehouseName the warehouse name.
	 */
	public void selectWarehouse(final String warehouseName) {
		getWaitDriver().waitForElementToBeClickable(By.cssSelector(SELECT_WAREHOUSE_ICON_CSS)).click();
		getWaitDriver().waitForElementToBeClickable(By.cssSelector(String.format(WAREHOUSE_NAME_CSS, warehouseName))).click();

	}

	/**
	 * Clicks complete shipment button.
	 *
	 * @return CompleteShipmentDialog
	 */
	public CompleteShipmentDialog clickCompleteShipmentButton() {
		getWaitDriver().waitForElementToBeClickable(By.cssSelector(COMPLETE_SHIPMENT_BUTTON_CSS)).click();
		return new CompleteShipmentDialog(getDriver());
	}

	/**
	 * Verifies Complete Shipment button is present.
	 */
	public void verifyCompleteShipmentButtonIsPresent() {
		getWaitDriver().waitForPageLoad();
		assertThat(isElementPresent(By.cssSelector(COMPLETE_SHIPMENT_BUTTON_CSS)))
				.as("Unable to find Complete Shipment button")
				.isTrue();
	}

}
