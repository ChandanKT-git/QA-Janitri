package com.janitri.tests;

import com.janitri.pages.LoginPage;
import com.janitri.utils.ConfigManager;
import com.janitri.utils.ReportManager;
import com.janitri.utils.TestUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Data validation tests for the Janitri Dashboard login page
 */
public class DataValidationTest extends BaseTest {
    private LoginPage loginPage;
    private ConfigManager configManager;

    @BeforeMethod
    public void setupTest() {
        loginPage = new LoginPage(driver);
        configManager = ConfigManager.getInstance();
    }

    @DataProvider(name = "invalidUserIds")
    public Object[][] getInvalidUserIds() {
        return new Object[][] {
            {""}, // Empty
            {"a"}, // Too short
            {"notanemail"}, // No @ symbol
            {"missing@domain"}, // Missing domain
            {"@missingusername.com"}, // Missing username
            {"special!chars#@domain.com"}, // Special characters
            {"spaces @domain.com"}, // Spaces
            {"double@@domain.com"}, // Double @ symbol
            {TestUtils.generateRandomString(100) + "@domain.com"} // Too long
        };
    }

    @DataProvider(name = "invalidPasswords")
    public Object[][] getInvalidPasswords() {
        return new Object[][] {
            {""}, // Empty
            {"a"}, // Too short
            {"12345"}, // Too short, only numbers
            {"abcdef"}, // Too short, only lowercase
            {"ABCDEF"}, // Too short, only uppercase
            {"abcABC"}, // No numbers or special chars
            {"abc123"}, // No uppercase or special chars
            {"ABC123"}, // No lowercase or special chars
            {TestUtils.generateRandomString(100)} // Too long
        };
    }

    @Test(dataProvider = "invalidUserIds", description = "Test validation of invalid user IDs")
    public void testInvalidUserIdValidation(String invalidUserId) {
        // Clear fields
        loginPage.clearUserId();
        loginPage.clearPassword();
        
        // Enter invalid user ID
        loginPage.enterUserId(invalidUserId);
        
        // Enter valid password to isolate user ID validation
        loginPage.enterPassword("ValidPassword123!");
        
        // Check if login button is disabled or if validation error is shown
        boolean validationWorking = false;
        
        // Check if button is disabled
        if (!loginPage.isLoginButtonEnabled()) {
            validationWorking = true;
        } else {
            // If button is enabled, try to click it and check for validation error
            loginPage.clickLoginButton();
            TestUtils.waitForPageLoad(driver);
            
            // Check for error message
            String errorMsg = loginPage.getErrorMessage();
            if (errorMsg != null && !errorMsg.isEmpty()) {
                validationWorking = true;
                System.out.println("Error message for invalid user ID '" + invalidUserId + "': " + errorMsg);
            }
            
            // Check for HTML5 validation message
            String validationMessage = (String) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].validationMessage;", loginPage.getUserIdField());
            
            if (validationMessage != null && !validationMessage.isEmpty()) {
                validationWorking = true;
                System.out.println("HTML5 validation message for invalid user ID '" + invalidUserId + "': " + validationMessage);
            }
        }
        
        Assert.assertTrue(validationWorking, 
            "Validation should prevent login with invalid user ID: '" + invalidUserId + "'");
    }

    @Test(dataProvider = "invalidPasswords", description = "Test validation of invalid passwords")
    public void testInvalidPasswordValidation(String invalidPassword) {
        // Clear fields
        loginPage.clearUserId();
        loginPage.clearPassword();
        
        // Enter valid user ID to isolate password validation
        loginPage.enterUserId("valid@example.com");
        
        // Enter invalid password
        loginPage.enterPassword(invalidPassword);
        
        // Check if login button is disabled or if validation error is shown
        boolean validationWorking = false;
        
        // Check if button is disabled
        if (!loginPage.isLoginButtonEnabled()) {
            validationWorking = true;
        } else {
            // If button is enabled, try to click it and check for validation error
            loginPage.clickLoginButton();
            TestUtils.waitForPageLoad(driver);
            
            // Check for error message
            String errorMsg = loginPage.getErrorMessage();
            if (errorMsg != null && !errorMsg.isEmpty()) {
                validationWorking = true;
                System.out.println("Error message for invalid password '" + invalidPassword + "': " + errorMsg);
            }
            
            // Check for HTML5 validation message
            String validationMessage = (String) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].validationMessage;", loginPage.getPasswordField());
            
            if (validationMessage != null && !validationMessage.isEmpty()) {
                validationWorking = true;
                System.out.println("HTML5 validation message for invalid password '" + invalidPassword + "': " + validationMessage);
            }
        }
        
        Assert.assertTrue(validationWorking, 
            "Validation should prevent login with invalid password: '" + invalidPassword + "'");
    }

    @Test(description = "Test maximum length constraints")
    public void testMaxLengthConstraints() {
        // Get user ID field
        WebElement userIdField = loginPage.getUserIdField();
        
        // Check if maxlength attribute is set
        String userIdMaxLength = userIdField.getAttribute("maxlength");
        if (userIdMaxLength != null && !userIdMaxLength.isEmpty()) {
            int maxLength = Integer.parseInt(userIdMaxLength);
            System.out.println("User ID field has maxlength constraint: " + maxLength);
            
            // Try to enter a string longer than maxlength
            String longInput = TestUtils.generateRandomString(maxLength + 10);
            loginPage.enterUserId(longInput);
            
            // Check if input was truncated
            String actualValue = userIdField.getAttribute("value");
            Assert.assertTrue(actualValue.length() <= maxLength, 
                "User ID field should enforce maxlength constraint");
        } else {
            System.out.println("User ID field does not have maxlength attribute. Testing with long input.");
            
            // Try with a very long input
            String longInput = TestUtils.generateRandomString(1000);
            loginPage.enterUserId(longInput);
            
            // Check if input was accepted (this is just informational)
            String actualValue = userIdField.getAttribute("value");
            System.out.println("User ID field accepted " + actualValue.length() + " characters.");
        }
        
        // Get password field
        WebElement passwordField = loginPage.getPasswordField();
        
        // Check if maxlength attribute is set
        String passwordMaxLength = passwordField.getAttribute("maxlength");
        if (passwordMaxLength != null && !passwordMaxLength.isEmpty()) {
            int maxLength = Integer.parseInt(passwordMaxLength);
            System.out.println("Password field has maxlength constraint: " + maxLength);
            
            // Try to enter a string longer than maxlength
            String longInput = TestUtils.generateRandomString(maxLength + 10);
            loginPage.enterPassword(longInput);
            
            // Check if input was truncated
            String actualValue = passwordField.getAttribute("value");
            Assert.assertTrue(actualValue.length() <= maxLength, 
                "Password field should enforce maxlength constraint");
        } else {
            System.out.println("Password field does not have maxlength attribute. Testing with long input.");
            
            // Try with a very long input
            String longInput = TestUtils.generateRandomString(1000);
            loginPage.enterPassword(longInput);
            
            // Check if input was accepted (this is just informational)
            String actualValue = passwordField.getAttribute("value");
            System.out.println("Password field accepted " + actualValue.length() + " characters.");
        }
    }

    @Test(description = "Test input sanitization")
    public void testInputSanitization() {
        // Test with script tags to check for sanitization
        String scriptInput = "<script>alert('XSS')</script>";
        
        // Enter script in user ID field
        loginPage.clearUserId();
        loginPage.enterUserId(scriptInput);
        
        // Check if the value was sanitized or escaped
        String userIdValue = loginPage.getUserIdField().getAttribute("value");
        boolean userIdSanitized = !userIdValue.equals(scriptInput) || 
                                 !driver.getPageSource().contains("<script>alert('XSS')</script>");
        
        // Enter script in password field
        loginPage.clearPassword();
        loginPage.enterPassword(scriptInput);
        
        // Check if the value was sanitized or escaped
        String passwordValue = loginPage.getPasswordField().getAttribute("value");
        boolean passwordSanitized = !passwordValue.equals(scriptInput) || 
                                   !driver.getPageSource().contains("<script>alert('XSS')</script>");
        
        // Log results
        System.out.println("User ID field sanitized script input: " + userIdSanitized);
        System.out.println("Password field sanitized script input: " + passwordSanitized);
        
        // Assert sanitization
        Assert.assertTrue(userIdSanitized, "User ID field should sanitize or escape script input");
        Assert.assertTrue(passwordSanitized, "Password field should sanitize or escape script input");
    }

    @Test(description = "Test whitespace handling")
    public void testWhitespaceHandling() {
        // Test with leading and trailing whitespace
        String userIdWithWhitespace = "  test@example.com  ";
        String passwordWithWhitespace = "  password123  ";
        
        // Enter values with whitespace
        loginPage.clearUserId();
        loginPage.enterUserId(userIdWithWhitespace);
        
        loginPage.clearPassword();
        loginPage.enterPassword(passwordWithWhitespace);
        
        // Try to login
        loginPage.clickLoginButton();
        TestUtils.waitForPageLoad(driver);
        
        // Check if whitespace was trimmed (this is a best practice)
        // We can only check this indirectly by seeing if login succeeds or fails with a specific error
        String errorMsg = loginPage.getErrorMessage();
        
        // Log the result
        if (errorMsg != null && !errorMsg.isEmpty()) {
            System.out.println("Login with whitespace resulted in error: " + errorMsg);
            System.out.println("This may indicate that whitespace was not trimmed, or that the credentials were invalid.");
        } else {
            System.out.println("No error message after login with whitespace. "
                + "This may indicate that whitespace was trimmed, or that the login succeeded.");
        }
        
        // This is an informational test, not a strict assertion
        System.out.println("Note: Best practice is to trim whitespace from user input, "
            + "especially for credentials like email addresses.");
    }

    @AfterMethod
    public void tearDownTest(ITestResult result) {
        // Add test result to report manager
        ReportManager.getInstance().addTestResult(result.getName(), result, driver);
    }
}