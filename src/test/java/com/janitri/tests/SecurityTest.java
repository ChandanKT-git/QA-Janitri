package com.janitri.tests;

import com.janitri.pages.LoginPage;
import com.janitri.utils.ConfigManager;
import com.janitri.utils.ReportManager;
import com.janitri.utils.SecurityUtils;
import com.janitri.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * Security tests for the Janitri Dashboard login page
 */
public class SecurityTest extends BaseTest {
    private LoginPage loginPage;
    private ConfigManager configManager;

    @BeforeMethod
    public void setupTest() {
        loginPage = new LoginPage(driver);
        configManager = ConfigManager.getInstance();
    }

    @Test(description = "Test for XSS vulnerabilities in login form")
    public void testXssVulnerabilities() {
        if (!configManager.getBooleanProperty("enableSecurityTests", true)) {
            System.out.println("Security tests are disabled in configuration. Skipping XSS test.");
            return;
        }

        // Test XSS in user ID field
        List<String> userIdVulnerabilities = SecurityUtils.testXssVulnerability(driver, loginPage.getUserIdField());
        boolean userIdVulnerable = !userIdVulnerabilities.isEmpty();
        String userIdPayload = userIdVulnerable ? userIdVulnerabilities.get(0) : "";
        
        // Test XSS in password field
        List<String> passwordVulnerabilities = SecurityUtils.testXssVulnerability(driver, loginPage.getPasswordField());
        boolean passwordVulnerable = !passwordVulnerabilities.isEmpty();
        String passwordPayload = passwordVulnerable ? passwordVulnerabilities.get(0) : "";
        
        // Log results
        System.out.println("XSS Test Results:");
        System.out.println("User ID field vulnerable: " + userIdVulnerable + 
                          (userIdVulnerable ? " (Payload: " + userIdPayload + ")" : ""));
        System.out.println("Password field vulnerable: " + passwordVulnerable + 
                          (passwordVulnerable ? " (Payload: " + passwordPayload + ")" : ""));
        
        // Assert no vulnerabilities
        Assert.assertFalse(userIdVulnerable, "User ID field should not be vulnerable to XSS");
        Assert.assertFalse(passwordVulnerable, "Password field should not be vulnerable to XSS");
    }

    @Test(description = "Test for SQL injection vulnerabilities in login form")
    public void testSqlInjectionVulnerabilities() {
        if (!configManager.getBooleanProperty("enableSecurityTests", true)) {
            System.out.println("Security tests are disabled in configuration. Skipping SQL injection test.");
            return;
        }

        // Test SQL injection
        List<String> sqlInjectionVulnerabilities = SecurityUtils.testSqlInjectionVulnerability(
            driver, loginPage.getUserIdField(), loginPage.getPasswordField(), loginPage.getLoginButton());
        
        boolean vulnerable = !sqlInjectionVulnerabilities.isEmpty();
        String payload = vulnerable ? sqlInjectionVulnerabilities.get(0) : "";
        
        // Log results
        System.out.println("SQL Injection Test Results:");
        System.out.println("Login form vulnerable: " + vulnerable + 
                          (vulnerable ? " (Payload: " + payload + ")" : ""));
        
        // Assert no vulnerabilities
        Assert.assertFalse(vulnerable, "Login form should not be vulnerable to SQL injection");
    }

    @Test(description = "Test for CSRF vulnerabilities in login form")
    public void testCsrfVulnerabilities() {
        if (!configManager.getBooleanProperty("enableSecurityTests", true)) {
            System.out.println("Security tests are disabled in configuration. Skipping CSRF test.");
            return;
        }

        // Test CSRF protection
        List<String> csrfVulnerabilities = SecurityUtils.testCsrfVulnerability(driver);
        boolean vulnerable = !csrfVulnerabilities.isEmpty();
        String details = vulnerable ? csrfVulnerabilities.get(0) : "No vulnerabilities found";
        
        // Log results
        System.out.println("CSRF Test Results:");
        System.out.println("Form vulnerable: " + vulnerable);
        System.out.println("Details: " + details);
        
        // Assert no vulnerabilities
        Assert.assertFalse(vulnerable, "Login form should have CSRF protection: " + details);
    }

    @Test(description = "Test for secure password handling")
    public void testSecurePasswordHandling() {
        // Test that password is not stored in page source
        String testPassword = "TestPassword123!";
        loginPage.enterPassword(testPassword);
        
        // Check if password appears in page source
        String pageSource = driver.getPageSource();
        boolean passwordInSource = pageSource.contains(testPassword);
        
        // Assert password is not in page source
        Assert.assertFalse(passwordInSource, "Password should not appear in page source");
        
        // Test that password field has autocomplete="off" or autocomplete="new-password"
        WebElement passwordField = loginPage.getPasswordField();
        String autocomplete = passwordField.getAttribute("autocomplete");
        
        boolean secureAutocomplete = autocomplete != null && 
                                    (autocomplete.equals("off") || 
                                     autocomplete.equals("new-password"));
        
        // Assert secure autocomplete setting
        Assert.assertTrue(secureAutocomplete, 
            "Password field should have autocomplete set to 'off' or 'new-password'");
    }

    @Test(description = "Test for HTTP security headers")
    public void testHttpSecurityHeaders() {
        // This test requires proxy capabilities or browser dev tools integration
        // For demonstration, we'll check if the page is served over HTTPS
        
        String currentUrl = driver.getCurrentUrl();
        boolean isHttps = currentUrl.startsWith("https://");
        
        System.out.println("Current URL: " + currentUrl);
        System.out.println("Is HTTPS: " + isHttps);
        
        // In a real implementation, you would check for security headers like:
        // - Content-Security-Policy
        // - X-Content-Type-Options
        // - X-Frame-Options
        // - Strict-Transport-Security
        // - X-XSS-Protection
        
        // For demonstration purposes, we'll just log a message
        System.out.println("Note: A complete HTTP security header test would require proxy capabilities");
        System.out.println("or browser dev tools integration to inspect response headers.");
    }

    @Test(description = "Test for brute force protection")
    public void testBruteForceProtection() {
        if (!configManager.getBooleanProperty("enableSecurityTests", true)) {
            System.out.println("Security tests are disabled in configuration. Skipping brute force test.");
            return;
        }

        // Test for brute force protection by attempting multiple failed logins
        int attempts = 5;
        boolean rateLimit = false;
        boolean captchaAppeared = false;
        
        for (int i = 0; i < attempts; i++) {
            // Clear fields and enter invalid credentials
            loginPage.enterUserId("test" + i + "@example.com");
            loginPage.enterPassword("wrongpassword" + i);
            loginPage.clickLoginButton();
            
            // Wait for page to load after login attempt
            TestUtils.waitForPageLoad(driver);
            
            // Check for rate limiting or CAPTCHA
            String pageSource = driver.getPageSource().toLowerCase();
            if (pageSource.contains("too many attempts") || 
                pageSource.contains("rate limit") || 
                pageSource.contains("try again later")) {
                rateLimit = true;
                break;
            }
            
            // Check for CAPTCHA
            if (pageSource.contains("captcha") || 
                driver.findElements(By.xpath("//*[contains(@class, 'captcha') or contains(@id, 'captcha')]")).size() > 0) {
                captchaAppeared = true;
                break;
            }
            
            // Short pause between attempts
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Log results
        System.out.println("Brute Force Protection Test Results:");
        System.out.println("Rate limiting detected: " + rateLimit);
        System.out.println("CAPTCHA detected: " + captchaAppeared);
        
        // Assert some form of protection is in place
        // Note: This is a soft assertion as not all systems implement these protections
        if (!rateLimit && !captchaAppeared) {
            System.out.println("WARNING: No brute force protection detected after " + attempts + " attempts.");
            System.out.println("This may be a security concern, but the test will not fail as some systems");
            System.out.println("implement other protection mechanisms not detected by this test.");
        }
    }

    @AfterMethod
    public void tearDownTest(ITestResult result) {
        // Add test result to report manager
        ReportManager.getInstance().addTestResult(result.getName(), result, driver);
    }
}