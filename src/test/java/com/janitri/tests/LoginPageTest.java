package com.janitri.tests;

import com.janitri.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for Login page functionality
 */
public class LoginPageTest extends BaseTest {

    /**
     * Test to verify that login button is disabled when fields are empty
     */
    @Test
    public void testLoginButtonDisabledWhenFieldsAreEmpty() {
        LoginPage loginPage = new LoginPage(driver);
        
        // Verify login button state without entering any credentials
        boolean isLoginButtonEnabled = loginPage.isLoginButtonEnabled();
        
        // Assert that login button should be disabled when fields are empty
        Assert.assertFalse(isLoginButtonEnabled, "Login button should be disabled when fields are empty");
        
        System.out.println("Test Result: Login button is " + 
                          (isLoginButtonEnabled ? "enabled" : "disabled") + 
                          " when fields are empty");
    }

    /**
     * Test to verify password masking/unmasking functionality
     */
    @Test
    public void testPasswordMaskedButton() {
        LoginPage loginPage = new LoginPage(driver);
        
        // Enter some text in password field
        loginPage.enterPassword("test123");
        
        // Verify password is masked by default
        boolean isPasswordMaskedBefore = loginPage.isPasswordMasked();
        Assert.assertTrue(isPasswordMaskedBefore, "Password should be masked by default");
        
        // Toggle password visibility
        loginPage.togglePasswordVisibility();
        
        // Verify password is unmasked after toggle
        boolean isPasswordMaskedAfter = loginPage.isPasswordMasked();
        Assert.assertFalse(isPasswordMaskedAfter, "Password should be unmasked after toggle");
        
        // Toggle password visibility again
        loginPage.togglePasswordVisibility();
        
        // Verify password is masked again
        boolean isPasswordMaskedFinal = loginPage.isPasswordMasked();
        Assert.assertTrue(isPasswordMaskedFinal, "Password should be masked after second toggle");
        
        System.out.println("Test Result: Password masking toggle functionality works as expected");
    }

    /**
     * Test to verify invalid login shows error message
     */
    @Test
    public void testInvalidLoginShowsErrorMsg() {
        LoginPage loginPage = new LoginPage(driver);
        
        // Enter invalid credentials
        loginPage.enterUserId("invalid_user")
                .enterPassword("invalid_password");
        
        // Click login button
        loginPage.clickLoginButton();
        
        // Get error message
        String errorMessage = loginPage.getErrorMessage();
        
        // Assert that error message is not empty
        Assert.assertFalse(errorMessage.isEmpty(), "Error message should be displayed for invalid login");
        
        System.out.println("Test Result: Error message displayed for invalid login: " + errorMessage);
    }

    /**
     * Test to verify presence of page elements
     */
    @Test
    public void testPresenceOfPageElements() {
        LoginPage loginPage = new LoginPage(driver);
        
        // Verify page title is present
        boolean isPageTitlePresent = loginPage.isPageTitlePresent();
        Assert.assertTrue(isPageTitlePresent, "Page title should be present");
        
        // Verify user ID input field is present
        boolean isUserIdInputPresent = loginPage.isUserIdInputPresent();
        Assert.assertTrue(isUserIdInputPresent, "User ID input field should be present");
        
        // Verify password input field is present
        boolean isPasswordInputPresent = loginPage.isPasswordInputPresent();
        Assert.assertTrue(isPasswordInputPresent, "Password input field should be present");
        
        // Verify password visibility toggle is present
        boolean isPasswordVisibilityTogglePresent = loginPage.isPasswordVisibilityTogglePresent();
        Assert.assertTrue(isPasswordVisibilityTogglePresent, "Password visibility toggle should be present");
        
        System.out.println("Test Result: All required page elements are present");
    }
}