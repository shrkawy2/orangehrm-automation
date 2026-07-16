package com.orangehrm.tests;

import com.orangehrm.pages.AddUserPage;
import com.orangehrm.pages.AdminPage;
import com.orangehrm.utils.TestDataGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UserManagementTest extends BaseTest {

    @Test(description = "Add a new system user, verify record count increases, "
            + "then delete it and verify the count reverts")
    public void addAndDeleteUser_shouldUpdateRecordCount() throws InterruptedException {
        String username = TestDataGenerator.uniqueUsername();
        String password = TestDataGenerator.strongPassword();

        AdminPage adminPage = loginAsAdmin();
        adminPage.openAdminTab();

        int initialCount = adminPage.getRecordCount();
        System.out.println("Initial record count (before add): " + initialCount);

        AddUserPage addUserPage = adminPage.clickAddButton();
        AdminPage adminPageAfterSave = addUserPage
                .selectUserRole("Admin")
                .selectStatus("Enabled")
                .typeEmployeeName("a")
                .setUsername(username)
                .setPassword(password)
                .save();

               //  driver.navigate().refresh();

        int countAfterAdd = adminPageAfterSave.getRecordCount();
        System.out.println("Record count after add: " + countAfterAdd);
        Assert.assertEquals(countAfterAdd, initialCount + 1,
                "Record count should increase by 1 after adding a user");


        Assert.assertTrue(countAfterAdd > initialCount,
                "Record count should be greater than initial count after adding a user");

        adminPageAfterSave.searchByUsername(username);
        Assert.assertTrue(adminPageAfterSave.hasSearchResults(),
                "Newly created user '" + username + "' should be found in search results");

        adminPageAfterSave.deleteFirstSearchResult();

        adminPageAfterSave.resetSearch();
        int countAfterDelete = adminPageAfterSave.getRecordCount();
        System.out.println("Record count after delete: " + countAfterDelete);
        Assert.assertEquals(countAfterDelete, initialCount,
                "Record count should revert to the original value after deleting the user");

        System.out.println("---- Summary ----");
        System.out.println("Before: " + initialCount + " | After Add: " + countAfterAdd + " | After Delete: " + countAfterDelete);
    }
}
