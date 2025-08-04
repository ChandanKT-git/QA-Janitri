package com.janitri.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Page Object class for the Login page
 */
public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // Locators
    private final By userIdInput = By.id("userId"); // Assuming ID is "userId"
    private final By passwordInput = By.id("password"); // Assuming ID is "password"
    private final By loginButton = By.xpath("//button[contains(text(), 'Login')]");
    private final By passwordVisibilityToggle = By.xpath("//button[contains(@class, 'password-toggle') or contains(@class, 'eye-icon')]");
    private final By errorMessage = By.xpath("//div[contains(@class, 'error-message') or contains(@class, 'alert')]");
    private final By pageTitle = By.xpath("//h1[contains(text(), 'Janitri') or contains(text(), 'Login')]");
    private final By forgotPasswordLink = By.xpath("//a[contains(text(), 'Forgot') and contains(text(), 'Password')]");
    private final By rememberMeCheckbox = By.xpath("//input[@type='checkbox'][contains(@id, 'remember') or contains(@name, 'remember')]");
    private final By signUpLink = By.xpath("//a[contains(text(), 'Sign up') or contains(text(), 'Register') or contains(text(), 'Create account')]");
    private final By loginForm = By.xpath("//form[.//input[@id='userId'] or .//input[@id='password']]");
    
    /**
     * Constructor for LoginPage
     * @param driver WebDriver instance
     */
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    /**
     * Enter user ID in the user ID input field
     * @param userId User ID to enter
     * @return LoginPage instance for method chaining
     */
    public LoginPage enterUserId(String userId) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(userIdInput)).sendKeys(userId);
        return this;
    }
    
    /**
     * Enter password in the password input field
     * @param password Password to enter
     * @return LoginPage instance for method chaining
     */
    public LoginPage enterPassword(String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput)).sendKeys(password);
        return this;
    }
    
    /**
     * Click the login button
     */
    public void clickLoginButton() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }
    
    /**
     * Toggle password visibility
     */
    public void togglePasswordVisibility() {
        wait.until(ExpectedConditions.elementToBeClickable(passwordVisibilityToggle)).click();
    }
    
    /**
     * Check if login button is enabled
     * @return true if login button is enabled, false otherwise
     */
    public boolean isLoginButtonEnabled() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton)).isEnabled();
    }
    
    /**
     * Check if password is masked (type="password")
     * @return true if password is masked, false otherwise
     */
    public boolean isPasswordMasked() {
        String type = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput)).getAttribute("type");
        return "password".equals(type);
    }
    
    /**
     * Get error message text if present
     * @return Error message text or empty string if not present
     */
    public String getErrorMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Check if page title is present
     * @return true if page title is present, false otherwise
     */
    public boolean isPageTitlePresent() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if user ID input field is present
     * @return true if user ID input field is present, false otherwise
     */
    public boolean isUserIdInputPresent() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(userIdInput)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if password input field is present
     * @return true if password input field is present, false otherwise
     */
    public boolean isPasswordInputPresent() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if password visibility toggle is present
     * @return true if password visibility toggle is present, false otherwise
     */
    public boolean isPasswordVisibilityTogglePresent() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(passwordVisibilityToggle)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Clear user ID input field
     * @return LoginPage instance for method chaining
     */
    public LoginPage clearUserId() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(userIdInput)).clear();
        return this;
    }
    
    /**
     * Clear password input field
     * @return LoginPage instance for method chaining
     */
    public LoginPage clearPassword() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput)).clear();
        return this;
    }
    
    /**
     * Get the WebElement for the user ID field
     * @return WebElement for user ID field
     */
    public WebElement getUserIdField() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(userIdInput));
    }
    
    /**
     * Get the WebElement for the password field
     * @return WebElement for password field
     */
    public WebElement getPasswordField() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
    }
    
    /**
     * Get the WebElement for the login button
     * @return WebElement for login button
     */
    public WebElement getLoginButton() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton));
    }
    
    /**
     * Get the ID attribute of the user ID field
     * @return ID attribute of user ID field
     */
    public String getUserIdFieldId() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(userIdInput)).getAttribute("id");
    }
    
    /**
     * Get the ID attribute of the password field
     * @return ID attribute of password field
     */
    public String getPasswordFieldId() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput)).getAttribute("id");
    }
    
    /**
     * Check if "Forgot Password" link is present
     * @return true if "Forgot Password" link is present, false otherwise
     */
    public boolean isForgotPasswordLinkPresent() {
        try {
            return driver.findElement(forgotPasswordLink).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click on "Forgot Password" link if present
     * @return true if clicked successfully, false otherwise
     */
    public boolean clickForgotPasswordLink() {
        try {
            driver.findElement(forgotPasswordLink).click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if "Remember Me" checkbox is present
     * @return true if "Remember Me" checkbox is present, false otherwise
     */
    public boolean isRememberMeCheckboxPresent() {
        try {
            return driver.findElement(rememberMeCheckbox).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Toggle "Remember Me" checkbox if present
     * @return true if toggled successfully, false otherwise
     */
    public boolean toggleRememberMeCheckbox() {
        try {
            driver.findElement(rememberMeCheckbox).click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if "Sign Up" or "Register" link is present
     * @return true if "Sign Up" link is present, false otherwise
     */
    public boolean isSignUpLinkPresent() {
        try {
            return driver.findElement(signUpLink).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Click on "Sign Up" or "Register" link if present
     * @return true if clicked successfully, false otherwise
     */
    public boolean clickSignUpLink() {
        try {
            driver.findElement(signUpLink).click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Perform login with given credentials
     * @param userId User ID to enter
     * @param password Password to enter
     */
    public void login(String userId, String password) {
        enterUserId(userId);
        enterPassword(password);
        clickLoginButton();
    }
    
    /**
     * Get form attributes for security testing
     * @return Map of form attributes
     */
    public Map<String, String> getFormAttributes() {
        Map<String, String> attributes = new HashMap<>();
        try {
            WebElement form = driver.findElement(loginForm);
            attributes.put("method", form.getAttribute("method"));
            attributes.put("action", form.getAttribute("action"));
            attributes.put("enctype", form.getAttribute("enctype"));
            attributes.put("autocomplete", form.getAttribute("autocomplete"));
        } catch (Exception e) {
            // Form not found or attributes not available
        }
        return attributes;
    }
    
    /**
     * Measure login form rendering time
     * @return Time in milliseconds for form to render
     */
    public long measureFormRenderTime() {
        long startTime = System.currentTimeMillis();
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginForm));
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Check if page has proper HTML lang attribute for accessibility
     * @return true if HTML lang attribute is present, false otherwise
     */
    public boolean hasHtmlLangAttribute() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String lang = (String) js.executeScript("return document.documentElement.lang");
        return lang != null && !lang.isEmpty();
    }
}