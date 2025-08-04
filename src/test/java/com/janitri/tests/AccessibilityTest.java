package com.janitri.tests;

import com.janitri.pages.LoginPage;
import com.janitri.utils.AccessibilityUtils;
import com.janitri.utils.ConfigManager;
import com.janitri.utils.ReportManager;
import com.janitri.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * Accessibility tests for the Janitri Dashboard login page
 */
public class AccessibilityTest extends BaseTest {
    private LoginPage loginPage;
    private ConfigManager configManager;

    @BeforeMethod
    public void setupTest() {
        loginPage = new LoginPage(driver);
        configManager = ConfigManager.getInstance();
    }

    @Test(description = "Test basic accessibility compliance")
    public void testBasicAccessibilityCompliance() {
        // Run accessibility audit
        Map<String, Object> auditResults = AccessibilityUtils.runAccessibilityAudit(driver);
        List<String> issues = (List<String>) auditResults.get("issues");
        int totalIssues = (int) auditResults.get("totalIssues");
        boolean passesThreshold = (boolean) auditResults.get("passesThreshold");

        // Log issues
        System.out.println("Found " + totalIssues + " accessibility issues:");
        for (String issue : issues) {
            System.out.println("- " + issue);
        }

        // Assert that issues are within threshold
        int threshold = configManager.getIntProperty("accessibilityViolationThreshold", 0);
        Assert.assertTrue(passesThreshold,
                "Found " + totalIssues + " accessibility issues, exceeding threshold of " + threshold);
    }

    @Test(description = "Test keyboard navigation")
    public void testKeyboardNavigation() {
        // Test tab navigation through form elements
        driver.findElement(By.tagName("body")).sendKeys(Keys.TAB);

        // First tab should focus on user ID field
        WebElement focusedElement = driver.switchTo().activeElement();
        Assert.assertEquals(focusedElement.getAttribute("id"), loginPage.getUserIdFieldId(),
                "First tab should focus on user ID field");

        // Tab to password field
        focusedElement.sendKeys(Keys.TAB);
        focusedElement = driver.switchTo().activeElement();
        Assert.assertEquals(focusedElement.getAttribute("id"), loginPage.getPasswordFieldId(),
                "Second tab should focus on password field");

        // Tab to password visibility toggle
        focusedElement.sendKeys(Keys.TAB);
        focusedElement = driver.switchTo().activeElement();
        Assert.assertTrue(focusedElement.getAttribute("class").contains("password-toggle") ||
                focusedElement.getAttribute("aria-label").contains("password"),
                "Third tab should focus on password visibility toggle");

        // Tab to login button
        focusedElement.sendKeys(Keys.TAB);
        focusedElement = driver.switchTo().activeElement();
        Assert.assertTrue(focusedElement.getTagName().equalsIgnoreCase("button") &&
                focusedElement.getText().toLowerCase().contains("login"),
                "Fourth tab should focus on login button");

        // Test that Enter key works on login button
        loginPage.enterUserId("test@example.com");
        loginPage.enterPassword("password123");

        // Focus on login button and press Enter
        WebElement loginButton = loginPage.getLoginButton();
        loginButton.sendKeys(Keys.ENTER);

        // Verify that Enter key triggered the login action
        TestUtils.waitForPageLoad(driver);
        Assert.assertTrue(driver.getCurrentUrl().contains("login") ||
                driver.getCurrentUrl().contains("dashboard"),
                "Enter key should trigger login action");
    }

    @Test(description = "Test screen reader accessibility")
    public void testScreenReaderAccessibility() {
        // Check for proper ARIA attributes
        WebElement userIdField = loginPage.getUserIdField();
        WebElement passwordField = loginPage.getPasswordField();
        WebElement loginButton = loginPage.getLoginButton();

        // Check for labels and accessible names
        Assert.assertTrue(
                userIdField.getAttribute("aria-label") != null || userIdField.getAttribute("placeholder") != null,
                "User ID field should have aria-label or placeholder for screen readers");

        Assert.assertTrue(
                passwordField.getAttribute("aria-label") != null || passwordField.getAttribute("placeholder") != null,
                "Password field should have aria-label or placeholder for screen readers");

        // Check for proper input types
        Assert.assertEquals(userIdField.getAttribute("type"), "text",
                "User ID field should have proper input type");

        Assert.assertEquals(passwordField.getAttribute("type"), "password",
                "Password field should have proper input type");

        // Check for form validation attributes
        String userIdRequired = userIdField.getAttribute("required");
        String userIdAriaRequired = userIdField.getAttribute("aria-required");
        Assert.assertTrue(userIdRequired != null || userIdAriaRequired != null,
                "User ID field should indicate it is required");

        String passwordRequired = passwordField.getAttribute("required");
        String passwordAriaRequired = passwordField.getAttribute("aria-required");
        Assert.assertTrue(passwordRequired != null || passwordAriaRequired != null,
                "Password field should indicate it is required");

        // Check for error message accessibility
        loginPage.enterUserId("invalid@example.com");
        loginPage.enterPassword("wrongpassword");
        loginPage.clickLoginButton();

        // Wait for error message
        TestUtils.waitForPageLoad(driver);

        // Check if error message has proper ARIA attributes
        WebElement errorMessage = driver
                .findElement(By.xpath("//div[contains(@class, 'error') or contains(@class, 'alert')]"));
        if (errorMessage.isDisplayed()) {
            Assert.assertTrue(errorMessage.getAttribute("role") != null ||
                    errorMessage.getAttribute("aria-live") != null,
                    "Error message should have proper ARIA attributes for screen readers");
        }
    }

    @Test(description = "Test color contrast and text size")
    public void testColorContrastAndTextSize() {
        // Check text size of important elements
        WebElement userIdField = loginPage.getUserIdField();
        WebElement passwordField = loginPage.getPasswordField();
        WebElement loginButton = loginPage.getLoginButton();

        // Get computed font sizes using JavaScript
        String userIdFontSize = (String) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return window.getComputedStyle(arguments[0]).getPropertyValue('font-size');",
                        userIdField);

        String passwordFontSize = (String) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return window.getComputedStyle(arguments[0]).getPropertyValue('font-size');",
                        passwordField);

        String buttonFontSize = (String) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return window.getComputedStyle(arguments[0]).getPropertyValue('font-size');",
                        loginButton);

        // Parse font sizes (typically in px)
        float userIdSize = Float.parseFloat(userIdFontSize.replace("px", ""));
        float passwordSize = Float.parseFloat(passwordFontSize.replace("px", ""));
        float buttonSize = Float.parseFloat(buttonFontSize.replace("px", ""));

        // Assert minimum font sizes for readability (typically 12px minimum)
        Assert.assertTrue(userIdSize >= 12, "User ID field font size should be at least 12px");
        Assert.assertTrue(passwordSize >= 12, "Password field font size should be at least 12px");
        Assert.assertTrue(buttonSize >= 12, "Login button font size should be at least 12px");

        // Check color contrast using JavaScript (simplified check)
        // A more comprehensive check would require a color contrast analysis library
        boolean hasContrastIssue = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript(
                        "var button = arguments[0];"
                                + "var style = window.getComputedStyle(button);"
                                + "var bgColor = style.backgroundColor;"
                                + "var textColor = style.color;"
                                + "// Very basic check - not a full WCAG contrast check"
                                + "if (bgColor.includes('255, 255, 255') && textColor.includes('255, 255, 255')) {"
                                + "  return true; // Light text on light background"
                                + "}"
                                + "if (bgColor.includes('0, 0, 0') && textColor.includes('0, 0, 0')) {"
                                + "  return true; // Dark text on dark background"
                                + "}"
                                + "return false;",
                        loginButton);

        Assert.assertFalse(hasContrastIssue, "Login button should have sufficient color contrast");
    }

    @AfterMethod
    public void tearDownTest(ITestResult result) {
        // Add test result to report manager
        ReportManager.getInstance().addTestResult(result.getName(), result, driver);
    }
}