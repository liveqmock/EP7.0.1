package com.elasticpath.selenium.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.ByWidgetId;
import com.elasticpath.selenium.common.CM;

/**
 * Expired Password Dialog.
 */
public class ExpiredPasswordDialog extends AbstractDialog {

	private static final String EXPIRED_PASSWORD_PARENT_CSS = "div[widget-id='Elastic Path Commerce - Password Expired']";
	private static final String CURRENT_PASSWORD_INPUT_CSS = "div[widget-id='Current Password'] input";
	private static final String NEW_PASSWORD_INPUT_CSS = "div[widget-id='New Password'] input";
	private static final String CONFIRM_NEW_PASSWORD_INPUT_CSS = "div[widget-id='Confirm New Password'] input";
	private static final String SIGN_IN_BUTTON_CSS = "div[widget-id='CorePluginResource.EpLoginDialog_LoginButton']";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public ExpiredPasswordDialog(final WebDriver driver) {
		super(driver);

		assertThat(isElementPresent(By.cssSelector(EXPIRED_PASSWORD_PARENT_CSS)))
				.as("Unable to find expired password dialog")
				.isTrue();
	}

	/**
	 * Enters current password value.
	 *
	 * @param currentPassword the username.
	 */
	public void enterCurrentPassword(final String currentPassword) {
		clearAndType(getWaitDriver().waitForElementToBeVisible(By.cssSelector(CURRENT_PASSWORD_INPUT_CSS)), currentPassword);
	}

	/**
	 * Enters new password value.
	 *
	 * @param newPassword the password.
	 */
	public void enterNewPassword(final String newPassword) {
		clearAndType(getWaitDriver().waitForElementToBeClickable(By.cssSelector(NEW_PASSWORD_INPUT_CSS)), newPassword);
	}

	/**
	 * Enters new password value.
	 *
	 * @param newPassword the password.
	 */
	public void enterConfirmNewPassword(final String newPassword) {
		clearAndType(getWaitDriver().waitForElementToBeClickable(By.cssSelector(CONFIRM_NEW_PASSWORD_INPUT_CSS)), newPassword);
	}

	/**
	 * Clicks on Sign In.
	 *
	 * @return the CM.
	 */
	public CM clickSignIn() {
		getWaitDriver().waitForElementToBeClickable(ByWidgetId.id(SIGN_IN_BUTTON_CSS)).click();
		return new CM(getDriver());
	}
}