package com.elasticpath.selenium.resultspane;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.elasticpath.selenium.common.AbstractPageObject;

/**
 * Price List Assignment Results Pane.
 */
public class PriceListAssignmentsResultPane extends AbstractPageObject {

	private static final String DELETE_PRICE_LIST_XPATH = "//div[text()='Delete Price List Assignment']";
	private static final String OPEN_PRICE_LIST_ASSIGNMENT_CSS = "div[widget-id='Open Price List Assignment']";
	private static final String PRICE_LIST_ASSIGNMENT_TABLE_CSS = "div[widget-id='Price List Assignment Search "
			+ "Result'][widget-type='Table'] ";
	private static final String PRICE_LIST_ASSIGNMENT_COLUMN_CSS = PRICE_LIST_ASSIGNMENT_TABLE_CSS + "div[parent-widget-id='Price List Assignment "
			+ "Search Result'] div[column-id='%s']";
	private static final String PRICE_LIST_ASSIGNMENT_COLUMN_NAME = "Price List Assignment";

	/**
	 * Constructor.
	 *
	 * @param driver WebDriver which drives this page.
	 */
	public PriceListAssignmentsResultPane(final WebDriver driver) {
		super(driver);
	}

	/**
	 * Verifies expected Price List Assignment exists.
	 *
	 * @param priceListAssignment the price list assignment.
	 */
	public void verifyPriceListAssignmentExists(final String priceListAssignment) {
		assertThat(selectItemInCenterPane(PRICE_LIST_ASSIGNMENT_TABLE_CSS, PRICE_LIST_ASSIGNMENT_COLUMN_CSS, priceListAssignment,
				PRICE_LIST_ASSIGNMENT_COLUMN_NAME))
				.as("Expected Price List Assignment does not exist - " + priceListAssignment)
				.isTrue();
	}

	/**
	 * Open the pricelist assignment.
	 *
	 * @param priceListAssignment the pricelist assignment
	 */
	public void openPriceListAssignment(final String priceListAssignment) {
		selectItemInCenterPane(PRICE_LIST_ASSIGNMENT_TABLE_CSS, PRICE_LIST_ASSIGNMENT_COLUMN_CSS, priceListAssignment,
				PRICE_LIST_ASSIGNMENT_COLUMN_NAME);
		getWaitDriver().waitForElementToBeClickable(By.cssSelector(OPEN_PRICE_LIST_ASSIGNMENT_CSS)).click();
	}

	/**
	 * Verifies if Price List Assignment is deleted.
	 *
	 * @param expectedPriceListAssignment the expected price list assignment.
	 */
	public void verifyPriceListAssignmentDeleted(final String expectedPriceListAssignment) {
		getWaitDriver().adjustWaitInterval(1);
		assertThat(selectItemInCenterPane(PRICE_LIST_ASSIGNMENT_TABLE_CSS, PRICE_LIST_ASSIGNMENT_COLUMN_CSS, expectedPriceListAssignment,
				PRICE_LIST_ASSIGNMENT_COLUMN_NAME))
				.as("Price List Assignment is not deleted - " + expectedPriceListAssignment)
				.isFalse();
		getWaitDriver().adjustWaitBackToDefault();
	}

	/**
	 * Deletes given price list assignment.
	 *
	 * @param priceListAssignment the price list assignment.
	 */
	public void deletePriceListAssignment(final String priceListAssignment) {
		assertThat(selectItemInCenterPane(PRICE_LIST_ASSIGNMENT_TABLE_CSS, PRICE_LIST_ASSIGNMENT_COLUMN_CSS, priceListAssignment,
				PRICE_LIST_ASSIGNMENT_COLUMN_NAME))
				.as("Unable to find Price List Assignment - " + priceListAssignment)
				.isTrue();
		getWaitDriver().waitForElementToBeClickable(By.xpath(DELETE_PRICE_LIST_XPATH)).click();
	}

	/**
	 * Verifies Price List Assignments result.
	 *
	 * @param priceListAssignment the price list assignment.
	 */
	public void verifyPLASearchResults(final String priceListAssignment) {
		assertThat(selectItemInCenterPane(PRICE_LIST_ASSIGNMENT_TABLE_CSS, PRICE_LIST_ASSIGNMENT_COLUMN_CSS, priceListAssignment,
				PRICE_LIST_ASSIGNMENT_COLUMN_NAME))
				.as("Unable to find Price List Assignment - " + priceListAssignment)
				.isTrue();
	}
}
