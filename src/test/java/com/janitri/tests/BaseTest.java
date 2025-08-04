package com.janitri.tests;

import com.janitri.utils.ConfigManager;
import com.janitri.utils.DriverFactory;
import com.janitri.utils.ReportManager;
import com.janitri.utils.TestUtils;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

/**
 * Base test class that handles browser setup and teardown
 */
public class BaseTest {
    protected WebDriver driver;
    protected ConfigManager configManager;
    protected String baseUrl;

    /**
     * Setup method that runs before the test suite
     * Initializes the ConfigManager and ReportManager
     */
    @BeforeSuite
    public void setUpSuite() {
        // Initialize ConfigManager
        configManager = ConfigManager.getInstance();

        // Initialize ReportManager
        ReportManager.getInstance().initReports();
    }

    /**
     * Setup method that runs before each test method
     * Initializes the WebDriver and navigates to the base URL
     */
    @BeforeMethod
    public void setUp() {
        // Get configuration values
        String browser = configManager.getProperty("browser", "chrome");
        baseUrl = configManager.getProperty("baseUrl", "https://dev-dash.janitri.in/");
        boolean headless = configManager.getBooleanProperty("headless", false);

        // Initialize the driver using DriverFactory
        driver = DriverFactory.createDriver(browser, headless);

        // Navigate to the application URL
        driver.get(baseUrl);

        // Wait for page to load
        TestUtils.waitForPageLoad(driver);
    }

    /**
     * Teardown method that runs after each test method
     * Captures screenshot on failure if configured and quits the WebDriver instance
     */
    @AfterMethod
    public void tearDown(ITestResult result) {
        // Capture screenshot on failure if configured
        boolean screenshotOnFailure = configManager.getBooleanProperty("screenshotOnFailure", true);
        if (screenshotOnFailure && result.getStatus() == ITestResult.FAILURE) {
            TestUtils.takeScreenshot(driver, result.getName());
        }

        // Add test result to report manager
        ReportManager.getInstance().addTestResult(result.getName(), result, driver);

        // Quit the driver
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Teardown method that runs after the test suite
     * Generates reports
     */
    @AfterSuite
    public void tearDownSuite() {
        // Generate reports
        ReportManager.getInstance().generateReports();
    }
}