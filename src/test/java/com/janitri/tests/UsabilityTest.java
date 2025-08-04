package com.janitri.tests;

import com.janitri.pages.LoginPage;
import com.janitri.utils.ConfigManager;
import com.janitri.utils.ReportManager;
import com.janitri.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Usability tests for the Janitri Dashboard login page
 */
public class UsabilityTest extends BaseTest {
    private LoginPage loginPage;
    private ConfigManager configManager;

    @BeforeMethod
    public void setupTest() {
        loginPage = new LoginPage(driver);
        configManager = ConfigManager.getInstance();
    }

    @Test(description = "Test form field tab order")
    public void testTabOrder() {
        // Focus on the first element
        driver.findElement(By.tagName("body")).click();
        
        // Send tab key to focus on first input
        Actions actions = new Actions(driver);
        actions.sendKeys(org.openqa.selenium.Keys.TAB).perform();
        
        // First element should be user ID field
        WebElement focusedElement = driver.switchTo().activeElement();
        Assert.assertEquals(focusedElement.getAttribute("id"), loginPage.getUserIdFieldId(),
            "First tab should focus on user ID field");
        
        // Tab to next element
        actions.sendKeys(org.openqa.selenium.Keys.TAB).perform();
        focusedElement = driver.switchTo().activeElement();
        Assert.assertEquals(focusedElement.getAttribute("id"), loginPage.getPasswordFieldId(),
            "Second tab should focus on password field");
        
        // Tab to next element (should be password visibility toggle or login button)
        actions.sendKeys(org.openqa.selenium.Keys.TAB).perform();
        focusedElement = driver.switchTo().activeElement();
        
        // Check if it's either password toggle or login button
        boolean isExpectedElement = focusedElement.getTagName().equalsIgnoreCase("button");
        Assert.assertTrue(isExpectedElement, 
            "Third tab should focus on a button element (either password toggle or login button)");
    }

    @Test(description = "Test form field labels and placeholders")
    public void testFormFieldLabelsAndPlaceholders() {
        WebElement userIdField = loginPage.getUserIdField();
        WebElement passwordField = loginPage.getPasswordField();
        
        // Check for placeholder or aria-label on user ID field
        String userIdPlaceholder = userIdField.getAttribute("placeholder");
        String userIdAriaLabel = userIdField.getAttribute("aria-label");
        
        boolean hasUserIdLabel = (userIdPlaceholder != null && !userIdPlaceholder.isEmpty()) || 
                                (userIdAriaLabel != null && !userIdAriaLabel.isEmpty());
        
        Assert.assertTrue(hasUserIdLabel, 
            "User ID field should have a placeholder or aria-label");
        
        // Check for placeholder or aria-label on password field
        String passwordPlaceholder = passwordField.getAttribute("placeholder");
        String passwordAriaLabel = passwordField.getAttribute("aria-label");
        
        boolean hasPasswordLabel = (passwordPlaceholder != null && !passwordPlaceholder.isEmpty()) || 
                                  (passwordAriaLabel != null && !passwordAriaLabel.isEmpty());
        
        Assert.assertTrue(hasPasswordLabel, 
            "Password field should have a placeholder or aria-label");
        
        // Check for visible labels (outside the input fields)
        List<WebElement> labels = driver.findElements(By.tagName("label"));
        boolean hasVisibleLabels = false;
        
        for (WebElement label : labels) {
            if (label.isDisplayed() && 
                (label.getText().toLowerCase().contains("user") || 
                 label.getText().toLowerCase().contains("email") || 
                 label.getText().toLowerCase().contains("id") || 
                 label.getText().toLowerCase().contains("password"))) {
                hasVisibleLabels = true;
                break;
            }
        }
        
        // This is a soft assertion - some modern UIs use only placeholders
        if (!hasVisibleLabels) {
            System.out.println("Note: No visible labels found for form fields. "
                + "While placeholders are present, visible labels improve usability.");
        }
    }

    @Test(description = "Test form field validation feedback")
    public void testFormFieldValidationFeedback() {
        // Test empty field validation
        loginPage.clearUserId();
        loginPage.clearPassword();
        
        // Try to click login button (it should be disabled)
        Assert.assertFalse(loginPage.isLoginButtonEnabled(), 
            "Login button should be disabled when fields are empty");
        
        // Test invalid email format
        loginPage.enterUserId("invalid-email");
        
        // Check if there's any validation message
        // This could be implemented in various ways, so we'll check a few common patterns
        boolean hasValidationFeedback = false;
        
        // Check for aria-invalid attribute
        String ariaInvalid = loginPage.getUserIdField().getAttribute("aria-invalid");
        if (ariaInvalid != null && ariaInvalid.equals("true")) {
            hasValidationFeedback = true;
        }
        
        // Check for validation message via JavaScript
        if (!hasValidationFeedback) {
            String validationMessage = (String) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].validationMessage;", loginPage.getUserIdField());
            
            if (validationMessage != null && !validationMessage.isEmpty()) {
                hasValidationFeedback = true;
            }
        }
        
        // Check for error classes
        if (!hasValidationFeedback) {
            String classes = loginPage.getUserIdField().getAttribute("class");
            if (classes != null && (classes.contains("error") || classes.contains("invalid"))) {
                hasValidationFeedback = true;
            }
        }
        
        // This is a soft assertion - validation might happen on submit only
        if (!hasValidationFeedback) {
            System.out.println("Note: No immediate validation feedback detected for invalid email format. "
                + "Real-time validation feedback improves usability.");
        }
    }

    @Test(description = "Test responsive design at different screen sizes")
    public void testResponsiveDesign() {
        // Store original window size
        Dimension originalSize = driver.manage().window().getSize();
        
        try {
            // Test small mobile size (e.g., iPhone SE)
            driver.manage().window().setSize(new Dimension(375, 667));
            TestUtils.waitForPageLoad(driver);
            
            // Check if login form is still accessible
            Assert.assertTrue(loginPage.isUserIdInputPresent(), 
                "User ID field should be visible on mobile size");
            Assert.assertTrue(loginPage.isPasswordInputPresent(), 
                "Password field should be visible on mobile size");
            
            // Test tablet size
            driver.manage().window().setSize(new Dimension(768, 1024));
            TestUtils.waitForPageLoad(driver);
            
            // Check if login form is still accessible
            Assert.assertTrue(loginPage.isUserIdInputPresent(), 
                "User ID field should be visible on tablet size");
            Assert.assertTrue(loginPage.isPasswordInputPresent(), 
                "Password field should be visible on tablet size");
            
            // Test desktop size
            driver.manage().window().setSize(new Dimension(1366, 768));
            TestUtils.waitForPageLoad(driver);
            
            // Check if login form is still accessible
            Assert.assertTrue(loginPage.isUserIdInputPresent(), 
                "User ID field should be visible on desktop size");
            Assert.assertTrue(loginPage.isPasswordInputPresent(), 
                "Password field should be visible on desktop size");
            
            // Test large desktop size
            driver.manage().window().setSize(new Dimension(1920, 1080));
            TestUtils.waitForPageLoad(driver);
            
            // Check if login form is still accessible
            Assert.assertTrue(loginPage.isUserIdInputPresent(), 
                "User ID field should be visible on large desktop size");
            Assert.assertTrue(loginPage.isPasswordInputPresent(), 
                "Password field should be visible on large desktop size");
        } finally {
            // Restore original window size
            driver.manage().window().setSize(originalSize);
        }
    }

    @Test(description = "Test additional login page features")
    public void testAdditionalFeatures() {
        // Check for "Remember Me" functionality
        boolean hasRememberMe = loginPage.isRememberMeCheckboxPresent();
        System.out.println("'Remember Me' functionality present: " + hasRememberMe);
        
        // Check for "Forgot Password" link
        boolean hasForgotPassword = loginPage.isForgotPasswordLinkPresent();
        System.out.println("'Forgot Password' link present: " + hasForgotPassword);
        
        // Check for "Sign Up" or "Register" link
        boolean hasSignUp = loginPage.isSignUpLinkPresent();
        System.out.println("'Sign Up' or 'Register' link present: " + hasSignUp);
        
        // These are soft assertions as these features may or may not be required
        if (!hasForgotPassword) {
            System.out.println("Note: No 'Forgot Password' link detected. "
                + "This is an important usability feature for login pages.");
        }
    }

    @Test(description = "Test form auto-focus")
    public void testFormAutoFocus() {
        // Reload the page to test initial focus
        driver.navigate().refresh();
        TestUtils.waitForPageLoad(driver);
        
        // Check if focus is automatically set to user ID field
        WebElement focusedElement = driver.switchTo().activeElement();
        boolean isUserIdFocused = focusedElement.equals(loginPage.getUserIdField());
        
        // This is a soft assertion as auto-focus is a nice-to-have feature
        if (!isUserIdFocused) {
            System.out.println("Note: User ID field is not auto-focused on page load. "
                + "Auto-focus improves usability by reducing the number of user interactions.");
        }
    }

    @AfterMethod
    public void tearDownTest(ITestResult result) {
        // Add test result to report manager
        ReportManager.getInstance().addTestResult(result.getName(), result, driver);
    }
}