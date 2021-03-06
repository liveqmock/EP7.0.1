package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Create Catalog Dialog.
 */
public class EditShippingServiceLevelDialog extends AbstractDialog {

	private static final String EDIT_SHIPPING_SERVICE_LEVEL_PARENT_CSS = "div[widget-id='Edit Shipping Service Level'][widget-type='Shell'] ";
	private static final String CARRIER_COMBO_CSS = EDIT_SHIPPING_SERVICE_LEVEL_PARENT_CSS + "div[widget-id='Carrier'][widget-type='CCombo']";
	private static final String NAME_INPUT_CSS = EDIT_SHIPPING_SERVICE_LEVEL_PARENT_CSS + "div[widget-id=''] > input";
	private static final String PROPERTIES_TABLE_PARENT_CSS = EDIT_SHIPPING_SERVICE_LEVEL_PARENT_CSS + "div[widget-id='Property Table'] ";
	private static final String PROPERTY_VALUE_INPUT_CSS = PROPERTIES_TABLE_PARENT_CSS + "input";
	private static final String PROPERTY_VALUE_DIV_XPATH = "//div[@widget-id='Property Table'] //div[text()='<Enter a value>']";
	private static final String SAVE_BUTTON_CSS = EDIT_SHIPPING_SERVICE_LEVEL_PARENT_CSS + "div[widget-id='Save']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public EditShippingServiceLevelDialog(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Selects carrier.
	 *
	 * @param carrier the carrier.
	 */
	public void selectCarrier(final String carrier) {
		getWaitDriver().waitForElementToBeVisible(By.cssSelector(CARRIER_COMBO_CSS));
		assertThat(selectComboBoxItem(CARRIER_COMBO_CSS, carrier))
				.as("Unable to find carrier - " + carrier)
				.isTrue();
	}

	/**
	 * Inputs shipping service level name.
	 *
	 * @param name the name.
	 */
	public void enterName(final String name) {
		clearAndType(getWaitDriver().waitForElementToBeVisible(By.cssSelector(NAME_INPUT_CSS)), name);
	}

	/**
	 * Inputs property value.
	 *
	 * @param value the value.
	 */
	public void enterPropertyValue(final String value) {
		getWaitDriver().waitForElementToBeClickable(By.xpath(PROPERTY_VALUE_DIV_XPATH)).click();
		clearAndType(getDriver().findElement(By.cssSelector(PROPERTY_VALUE_INPUT_CSS)), value);
	}

	/**
	 * Clicks save button.
	 */
	public void clickSaveButton() {
		getDriver().findElement(By.cssSelector(SAVE_BUTTON_CSS)).click();
		waitTillElementDisappears(By.cssSelector(EDIT_SHIPPING_SERVICE_LEVEL_PARENT_CSS));
	}

}
