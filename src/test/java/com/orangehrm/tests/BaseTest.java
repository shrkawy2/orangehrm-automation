package com.orangehrm.tests;

import com.orangehrm.config.ConfigReader;
import com.orangehrm.pages.AdminPage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utils.DriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

public abstract class BaseTest {

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverManager.getDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshot(result.getName());
            System.out.println("Failure URL: " + driver.getCurrentUrl());
        }
        DriverManager.quitDriver();
    }

    private void captureScreenshot(String testName) {
        try {
            Path dir = Paths.get("target", "screenshots");
            Files.createDirectories(dir);
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dest = dir.resolve(testName + "_" + Instant.now().toEpochMilli() + ".png");
            Files.copy(source.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved: " + dest.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        }
    }

    protected AdminPage loginAsAdmin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(ConfigReader.getBaseUrl());
        return loginPage.loginAs(ConfigReader.getUsername(), ConfigReader.getPassword());
    }
}
