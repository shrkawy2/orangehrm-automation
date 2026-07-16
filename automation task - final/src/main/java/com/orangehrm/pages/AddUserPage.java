package com.orangehrm.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AddUserPage extends BasePage {

    private static final By DROPDOWNS = By.cssSelector(".oxd-form .oxd-select-text"); // [0]=Role [1]=Status
    private static final By DROPDOWN_OPTION = By.cssSelector(".oxd-select-dropdown .oxd-select-option");
    private static final By EMPLOYEE_NAME_INPUT = By.cssSelector("input[placeholder='Type for hints...']");
    private static final By AUTOCOMPLETE_OPTIONS =
            By.cssSelector(".oxd-autocomplete-dropdown .oxd-autocomplete-option");
    private static final By TEXT_INPUTS = By.cssSelector(".oxd-form input[autocomplete='off']");
    private static final By PASSWORD_INPUTS = By.cssSelector("input[type='password']");
    private static final By SAVE_BUTTON = By.cssSelector("button[type='submit']");

    public AddUserPage(WebDriver driver) {
        super(driver);
    }

    public AddUserPage selectUserRole(String role) {
        selectFromDropdown(0, role);
        return this;
    }

    public AddUserPage selectStatus(String status) {
        selectFromDropdown(1, status);
        return this;
    }

    private void selectFromDropdown(int index, String visibleText) {
        List<WebElement> dropdowns = waitVisibleAll(DROPDOWNS);
        dropdowns.get(index).click();

        List<WebElement> options = waitVisibleAll(DROPDOWN_OPTION);
        options.stream()
                .filter(o -> o.getText().trim().equalsIgnoreCase(visibleText))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dropdown option not found: " + visibleText))
                .click();
    }

    /**
     * The OXD autocomplete widget on this build doesn't respond to click-based
     * selection (native click, Actions, JS click, dispatched MouseEvent
     * sequences, native pointer input all fail with the field marked "Invalid").
     * The mouse selection path is blocked by strict Vue.js validation on this
     * component. The keyboard selection path works, provided we:
     *   1. Clear the input for real (Ctrl+A + Delete, not .clear() - which
     *      doesn't reset the Vue-controlled model).
     *   2. Wait for OXD's debounce so suggestions actually render before we
     *      press Arrow Down.
     *   3. Insert small settle delays that mirror the natural human pause
     *      between seeing the list appear and reaching for the arrow key.
     *   4. Wait for the input's display value to actually update from the
     *      typed text to the selected employee name - Vue can commit the
     *      selection internally without the display catching up, and if we
     *      Save while the field still shows the typed letter, the form
     *      rejects it as "Invalid".
     */
    public AddUserPage typeEmployeeName(String name) throws InterruptedException {
        WebElement input = waitVisible(EMPLOYEE_NAME_INPUT);

        // Real clear via keyboard - .clear() alone doesn't reset the Vue model.
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);

        input.sendKeys(name);

        // Wait for OXD's debounce - suggestions must render before we press
        // arrow key, otherwise it just moves the cursor with no effect.
        new WebDriverWait(driver, Duration.ofSeconds(7))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(AUTOCOMPLETE_OPTIONS));

        // The arrow-down/enter sequence occasionally lands before Vue has
        // bound the dropdown's keyboard handlers, leaving the field showing
        // just the typed text with the dropdown still open. Retry the
        // keyboard sequence (re-fetching the input each time, since Vue can
        // swap the element) until the display value actually updates.
        TimeoutException lastFailure = null;
        for (int attempt = 0; attempt < 5; attempt++) {
            Thread.sleep(500);
            WebElement liveInput = find(EMPLOYEE_NAME_INPUT);
            liveInput.sendKeys(Keys.ARROW_DOWN);
            Thread.sleep(200);
            liveInput.sendKeys(Keys.ENTER);

            try {
                // Wait for the input value to update from what we typed to the
                // actual selected employee name - proves Vue's model committed
                // the choice and the display caught up.
                new WebDriverWait(driver, Duration.ofSeconds(2))
                        .until(d -> {
                            String value = find(EMPLOYEE_NAME_INPUT).getAttribute("value");
                            return value != null && !value.equalsIgnoreCase(name) && value.length() > name.length();
                        });
                return this;
            } catch (TimeoutException e) {
                lastFailure = e;
            }
        }
        throw lastFailure;
    }

    public AddUserPage setUsername(String username) {
        List<WebElement> textInputs = findAll(TEXT_INPUTS);
        WebElement employeeNameField = find(EMPLOYEE_NAME_INPUT);
        WebElement usernameField = textInputs.stream()
                .filter(el -> !el.equals(employeeNameField))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Username field not found"));

        usernameField.clear();
        usernameField.sendKeys(username);
        return this;
    }

    public AddUserPage setPassword(String password) {
        List<WebElement> passwordInputs = waitVisibleAll(PASSWORD_INPUTS);
        passwordInputs.get(0).clear();
        passwordInputs.get(0).sendKeys(password);
        passwordInputs.get(1).clear();
        passwordInputs.get(1).sendKeys(password);
        return this;
    }

    public AdminPage save() {
        click(SAVE_BUTTON);
        return new AdminPage(driver);
    }
}