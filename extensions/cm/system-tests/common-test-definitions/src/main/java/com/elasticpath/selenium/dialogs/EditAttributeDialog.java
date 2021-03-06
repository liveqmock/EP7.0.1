package com.elasticpath.selenium.dialogs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Edit Attribute Dialog.
 */
public class EditAttributeDialog extends AbstractDialog {

	private static final String EDIT_ATTRUBUTE_PARENT_CSS
			= "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.AttributeAddDialog_WinTitle_Edit'] ";
	private static final String ATTRUBUTE_NAME_INPUT_CSS = EDIT_ATTRUBUTE_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.catalog.CatalogMessages.AttributeAddDialog_AttributeName'] > input";
	private static final String OK_BUTTON_CSS = EDIT_ATTRUBUTE_PARENT_CSS
			+ "div[automation-id='com.elasticpath.cmclient.core.CoreMessages.AbstractEpDialog_ButtonOK'][seeable='true']";


	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public EditAttributeDialog(final WebDriver driver) {
		super(driver);
	}


	/**
	 * Inputs attribute name.
	 *
	 * @param attributeName the attribute name.
	 */
	public void enterAttributeName(final String attributeName) {
		clearAndType(getWaitDriver().waitForElementToBeVisible(By.cssSelector(ATTRUBUTE_NAME_INPUT_CSS)), attributeName);

	}

	/**
	 * Click ok button.
	 */
	public void clickOKButton() {
		getDriver().findElement(By.cssSelector(OK_BUTTON_CSS)).click();
		waitTillElementDisappears(By.cssSelector(EDIT_ATTRUBUTE_PARENT_CSS));
		getWaitDriver().waitForPageLoad();
	}

}
