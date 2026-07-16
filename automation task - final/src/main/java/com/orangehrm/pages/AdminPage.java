package com.orangehrm.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminPage extends BasePage {

    private static final By ADMIN_MENU_LINK = By.cssSelector("a[href*='viewAdminModule']");
    private static final By ADD_BUTTON = By.cssSelector("button[type='button'].oxd-button--secondary");
    // The username filter input has no placeholder/name attribute to key off,
    // so it's located via its label instead.
    private static final By SEARCH_USERNAME_INPUT =
            By.xpath("//label[text()='Username']/ancestor::div[contains(@class,'oxd-input-group')]//input");
    private static final By SEARCH_BUTTON = By.cssSelector("button[type='submit']");
    private static final By RESET_BUTTON = By.xpath("//button[normalize-space()='Reset']");
    private static final By RESULT_ROWS = By.cssSelector(".oxd-table-body .oxd-table-row");
    private static final By ADMIN_TITLE = By.xpath("//h6[text()='Admin']");
    private static final By SPINNER = By.className("oxd-loading-spinner");
    private static final By TRASH_ICON = By.cssSelector(".oxd-table-cell-actions .oxd-icon.bi-trash");
    private static final By CONFIRM_DELETE_BUTTON =
            By.xpath("//button[contains(@class,'oxd-button--label-danger')]");

    private static final Pattern RECORD_COUNT_PATTERN = Pattern.compile("\\((\\d+)\\)\\s*Records? Found");

    public AdminPage(WebDriver driver) {
        super(driver);
    }

    public void openAdminTab() {
        click(ADMIN_MENU_LINK);
        waitVisible(ADMIN_TITLE);
        waitForPageLoad();
    }

    private void waitForPageLoad() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(SPINNER));
        } catch (Exception ignored) {
        }
    }

    /**
     * Reads "(N) Records Found" straight from rendered page text via JS.
     * A locator-based approach failed here because the label's markup splits the
     * number and text across nested elements/text nodes in a way neither
     * contains(text(),...) nor CSS class selectors caught reliably.
     */
    public int getRecordCount() {
        waitForPageLoad();
        wait.until(d -> getBodyText().contains("Records Found"));

        String bodyText = getBodyText();
        Matcher matcher = RECORD_COUNT_PATTERN.matcher(bodyText);
        if (!matcher.find()) {
            throw new IllegalStateException("Could not find record count in page text");
        }
        return Integer.parseInt(matcher.group(1));
    }

    public AddUserPage clickAddButton() {
        click(ADD_BUTTON);
        return new AddUserPage(driver);
    }

    public void searchByUsername(String username) {
        type(SEARCH_USERNAME_INPUT, username);
        click(SEARCH_BUTTON);
        waitForPageLoad();
    }

    public void resetSearch() {
        click(RESET_BUTTON);
        waitForPageLoad();
    }

    public void deleteFirstSearchResult() {
        waitVisibleAll(RESULT_ROWS);
        find(TRASH_ICON).click();
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(CONFIRM_DELETE_BUTTON));
        confirmButton.click();
        waitForPageLoad();
    }

    public boolean hasSearchResults() {
        waitForPageLoad();
        List<WebElement> rows = findAll(RESULT_ROWS);
        return !rows.isEmpty()
                && !rows.get(0).getText().toLowerCase().contains("no records found");
    }
}
