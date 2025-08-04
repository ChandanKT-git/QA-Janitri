package com.janitri.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for security testing
 */
public class SecurityUtils {
    private static final ConfigManager configManager = ConfigManager.getInstance();
    
    // Common XSS payloads for testing
    private static final String[] XSS_PAYLOADS = {
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert('XSS')>",
        "javascript:alert('XSS')",
        "<svg/onload=alert('XSS')>",
        "'\"><script>alert('XSS')</script>"
    };
    
    // Common SQL injection payloads for testing
    private static final String[] SQL_INJECTION_PAYLOADS = {
        "' OR '1'='1",
        "' OR '1'='1' --",
        "admin' --",
        "1' OR '1' = '1",
        "1'; DROP TABLE users; --"
    };

    /**
     * Test for XSS vulnerabilities in input fields
     * @param driver WebDriver instance
     * @param inputField Input field to test
     * @return List of potential vulnerabilities found
     */
    public static List<String> testXssVulnerability(WebDriver driver, WebElement inputField) {
        List<String> vulnerabilities = new ArrayList<>();
        
        if (!configManager.getBooleanProperty("checkForXssVulnerabilities", true)) {
            return vulnerabilities;
        }
        
        String originalUrl = driver.getCurrentUrl();
        
        for (String payload : XSS_PAYLOADS) {
            try {
                // Clear field and enter XSS payload
                inputField.clear();
                inputField.sendKeys(payload);
                
                // Check if alert dialog appears (indicates XSS vulnerability)
                if (isAlertPresent(driver)) {
                    vulnerabilities.add("XSS vulnerability detected with payload: " + payload);
                    driver.switchTo().alert().accept();
                }
                
                // Check if payload is reflected in page source
                if (driver.getPageSource().contains(payload)) {
                    vulnerabilities.add("Potential XSS vulnerability: Payload reflected in page source: " + payload);
                }
                
                // Navigate back to original URL if changed
                if (!driver.getCurrentUrl().equals(originalUrl)) {
                    driver.navigate().to(originalUrl);
                }
            } catch (Exception e) {
                // Continue with next payload if an exception occurs
                System.err.println("Error testing XSS payload: " + e.getMessage());
                try {
                    driver.navigate().to(originalUrl);
                } catch (Exception ex) {
                    // Ignore navigation errors
                }
            }
        }
        
        return vulnerabilities;
    }

    /**
     * Test for SQL injection vulnerabilities in input fields
     * @param driver WebDriver instance
     * @param usernameField Username input field
     * @param passwordField Password input field
     * @param submitButton Submit button
     * @return List of potential vulnerabilities found
     */
    public static List<String> testSqlInjectionVulnerability(WebDriver driver, 
                                                           WebElement usernameField,
                                                           WebElement passwordField,
                                                           WebElement submitButton) {
        List<String> vulnerabilities = new ArrayList<>();
        
        if (!configManager.getBooleanProperty("checkForSqlInjectionVulnerabilities", true)) {
            return vulnerabilities;
        }
        
        String originalUrl = driver.getCurrentUrl();
        
        for (String payload : SQL_INJECTION_PAYLOADS) {
            try {
                // Clear fields and enter SQL injection payload
                usernameField.clear();
                usernameField.sendKeys(payload);
                
                passwordField.clear();
                passwordField.sendKeys("password");
                
                // Submit form
                submitButton.click();
                
                // Wait for page to load
                TestUtils.waitForPageLoad(driver);
                
                // Check if login was successful (indicating potential SQL injection vulnerability)
                if (!driver.getCurrentUrl().equals(originalUrl) || 
                    !driver.findElements(By.xpath("//div[contains(text(), 'Invalid credentials') or contains(text(), 'Login failed')]")).isEmpty()) {
                    vulnerabilities.add("Potential SQL injection vulnerability with payload: " + payload);
                }
                
                // Navigate back to original URL if changed
                if (!driver.getCurrentUrl().equals(originalUrl)) {
                    driver.navigate().to(originalUrl);
                    // Wait for page to load
                    TestUtils.waitForPageLoad(driver);
                }
            } catch (Exception e) {
                // Continue with next payload if an exception occurs
                System.err.println("Error testing SQL injection payload: " + e.getMessage());
                try {
                    driver.navigate().to(originalUrl);
                } catch (Exception ex) {
                    // Ignore navigation errors
                }
            }
        }
        
        return vulnerabilities;
    }

    /**
     * Check if an alert is present
     * @param driver WebDriver instance
     * @return true if alert is present, false otherwise
     */
    private static boolean isAlertPresent(WebDriver driver) {
        try {
            driver.switchTo().alert();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Test for CSRF vulnerabilities
     * @param driver WebDriver instance
     * @return List of potential vulnerabilities found
     */
    public static List<String> testCsrfVulnerability(WebDriver driver) {
        List<String> vulnerabilities = new ArrayList<>();
        
        // Check if forms have CSRF tokens
        List<WebElement> forms = driver.findElements(By.tagName("form"));
        for (WebElement form : forms) {
            boolean hasToken = false;
            List<WebElement> hiddenInputs = form.findElements(By.xpath(".//input[@type='hidden']"));
            
            for (WebElement input : hiddenInputs) {
                String name = input.getAttribute("name");
                if (name != null && (name.toLowerCase().contains("token") || 
                                    name.toLowerCase().contains("csrf") || 
                                    name.toLowerCase().contains("nonce"))) {
                    hasToken = true;
                    break;
                }
            }
            
            if (!hasToken && !forms.isEmpty()) {
                vulnerabilities.add("Potential CSRF vulnerability: Form without CSRF token");
            }
        }
        
        return vulnerabilities;
    }
}