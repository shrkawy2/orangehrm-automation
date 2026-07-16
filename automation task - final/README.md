# OrangeHRM Automation — QC Assessment (Final)

Selenium (Java) + TestNG UI automation and REST Assured API automation
against `https://opensource-demo.orangehrmlive.com`.

## Stack
- Java 17, Maven, TestNG 7
- Selenium 4 + WebDriverManager
- REST Assured (bonus task)
- Page Object Model, ThreadLocal driver

## Structure
```
src/main/java/com/orangehrm/
  config/ConfigReader.java      # externalized config
  utils/DriverManager.java      # thread-local driver factory
  utils/TestDataGenerator.java  # unique username generator
  pages/                        # POM: BasePage, LoginPage, AdminPage, AddUserPage
  api/CandidateApiClient.java   # REST Assured client for the bonus task

src/test/java/com/orangehrm/tests/
  BaseTest.java                 # driver lifecycle + screenshot on failure
  UserManagementTest.java       # main task: full UI flow
  CandidateApiTest.java         # bonus task: add/delete candidate via API
```

## Run
```bash
mvn clean test
```

## The tricky part: Employee Name autocomplete

The main UI test (`UserManagementTest`) drives the entire 13-step flow through
the UI. The one step that took real work to get right was selecting an employee
from the OXD autocomplete widget in the Add User form. This widget doesn't
respond to click-based selection - a raw `.click()` on the option element, JS
click, W3C Actions hover+click, dispatched MouseEvent sequences, and native
pointer input via W3C Actions API were all tried and all failed with the field
ending up marked "Invalid".

The keyboard selection path works, provided we:

1. **Clear the input via keyboard** (`Ctrl+A` + `Delete`), not `.clear()` -
   the Vue-controlled input doesn't reset its internal model on `.clear()`, so
   any subsequent `sendKeys()` appends onto stale state.
2. **Wait for OXD's debounce** so suggestions actually render before we press
   Arrow Down (otherwise Arrow Down just moves the cursor in the input).
3. **Insert small settle delays** between suggestions appearing, pressing Arrow
   Down, and pressing Enter. Without them, the arrow key can fire before the
   widget has bound its keydown handler to the newly-rendered option.

See `AddUserPage.typeEmployeeName()` for the implementation with commentary.

## Other notable design decisions

**No `@FindBy` / `PageFactory`** - elements are located fresh via `By` locators
on every interaction, so DOM re-renders (common on OrangeHRM's Vue SPA) never
leave us holding a stale WebElement.

**Record count via `document.body.innerText` + regex** instead of a CSS/XPath
locator - the "(N) Records Found" label's markup splits the number and text
across nested elements in a way locators don't catch reliably across OrangeHRM
releases.

**Unique username per run** (`qa_auto_<epoch millis>`) - reruns don't collide
with leftover users from prior runs.

**Screenshot + URL captured on every failure**, saved to `target/screenshots/`
with timestamped filenames.
