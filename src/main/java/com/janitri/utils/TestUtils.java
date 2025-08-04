package com.janitri.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for common test operations
 */
public class TestUtils {
    private static final ConfigManager configManager = ConfigManager.getInstance();

    /**
     * Wait for an element to be visible
     * @param driver WebDriver instance
     * @param element WebElement to wait for
     * @param timeoutInSeconds Timeout in seconds
     */
    public static void waitForElementVisibility(WebDriver driver, WebElement element, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Wait for an element to be clickable
     * @param driver WebDriver instance
     * @param element WebElement to wait for
     * @param timeoutInSeconds Timeout in seconds
     */
    public static void waitForElementClickable(WebDriver driver, WebElement element, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Take a screenshot and save it to the screenshots directory
     * @param driver WebDriver instance
     * @param testName Name of the test
     * @return Path to the screenshot file
     */
    public static String takeScreenshot(WebDriver driver, String testName) {
        // Create screenshots directory if it doesn't exist
        Path screenshotsDir = Paths.get("screenshots");
        if (!Files.exists(screenshotsDir)) {
            try {
                Files.createDirectories(screenshotsDir);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        // Generate timestamp for unique filename
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(formatter);

        // Take screenshot
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String screenshotPath = "screenshots/" + testName + "_" + timestamp + ".png";
        
        try {
            Files.copy(screenshot.toPath(), Paths.get(screenshotPath));
            return screenshotPath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generate a random string of specified length
     * @param length Length of the random string
     * @return Random string
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (chars.length() * Math.random());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
    
    /**
     * Generate a random email address
     * @return Random email address
     */
    public static String generateRandomEmail() {
        return "test" + System.currentTimeMillis() + "@example.com";
    }
    
    /**
     * Generate a random phone number
     * @return Random 10-digit phone number
     */
    public static String generateRandomPhoneNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("9"); // Start with 9 for Indian mobile numbers
        for (int i = 0; i < 9; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    
    /**
     * Wait for page to load completely
     * @param driver WebDriver instance
     */
    public static void waitForPageLoad(WebDriver driver) {
        ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
            }
        };
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(configManager.getIntProperty("timeout", 30)));
        wait.until(pageLoadCondition);
    }
    
    /**
     * Scroll to an element
     * @param driver WebDriver instance
     * @param element Element to scroll to
     */
    public static void scrollToElement(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        try {
            Thread.sleep(500); // Small pause after scrolling
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Highlight an element (useful for debugging)
     * @param driver WebDriver instance
     * @param element Element to highlight
     */
    public static void highlightElement(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String originalStyle = element.getAttribute("style");
        js.executeScript("arguments[0].setAttribute('style', 'background: yellow; border: 2px solid red;');", element);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        js.executeScript("arguments[0].setAttribute('style', '" + originalStyle + "');", element);
    }
    
    /**
     * Check if an element exists
     * @param driver WebDriver instance
     * @param by Locator for the element
     * @return true if element exists, false otherwise
     */
    public static boolean isElementPresent(WebDriver driver, By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Safely click an element (with retry)
     * @param driver WebDriver instance
     * @param element Element to click
     */
    public static void safeClick(WebDriver driver, WebElement element) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                waitForElementClickable(driver, element, configManager.getIntProperty("timeout", 10));
                highlightElement(driver, element);
                element.click();
                return;
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                attempts++;
                if (attempts == 3) {
                    // Try with JavaScript as last resort
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                        return;
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to click element after multiple attempts", ex);
                    }
                }
            }
        }
    }
    
    /**
     * Measure page load time
     * @param driver WebDriver instance
     * @param url URL to navigate to
     * @return Page load time in milliseconds
     */
    public static long measurePageLoadTime(WebDriver driver, String url) {
        long start = System.currentTimeMillis();
        driver.get(url);
        waitForPageLoad(driver);
        long finish = System.currentTimeMillis();
        return finish - start;
    }
    
    /**
     * Check if page meets performance threshold
     * @param driver WebDriver instance
     * @param url URL to navigate to
     * @return true if page loads within threshold, false otherwise
     */
    public static boolean checkPerformanceThreshold(WebDriver driver, String url) {
        long loadTime = measurePageLoadTime(driver, url);
        int threshold = configManager.getIntProperty("performanceThreshold", 3000);
        return loadTime <= threshold;
    }
}