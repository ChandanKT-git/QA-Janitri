package com.janitri.tests;

import com.janitri.pages.LoginPage;
import com.janitri.utils.ConfigManager;
import com.janitri.utils.ReportManager;
import com.janitri.utils.TestUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Performance tests for the Janitri Dashboard login page
 */
public class PerformanceTest extends BaseTest {
    private LoginPage loginPage;
    private ConfigManager configManager;
    private Map<String, Long> metrics;

    @BeforeMethod
    public void setupTest() {
        loginPage = new LoginPage(driver);
        configManager = ConfigManager.getInstance();
        metrics = new HashMap<>();
    }

    @Test(description = "Test login page load performance")
    public void testLoginPageLoadPerformance() {
        // Measure page load time
        long loadTime = TestUtils.measurePageLoadTime(driver, configManager.getProperty("baseUrl"));
        metrics.put("pageLoadTime", loadTime);
        
        // Log the load time
        System.out.println("Login page load time: " + loadTime + " ms");
        
        // Assert that load time is within acceptable threshold
        int threshold = configManager.getIntProperty("performanceThreshold", 3000);
        Assert.assertTrue(loadTime <= threshold, 
            "Login page load time " + loadTime + "ms exceeds threshold of " + threshold + "ms");
    }

    @Test(description = "Test login form interaction performance")
    public void testLoginFormInteractionPerformance() {
        // Measure time to enter credentials
        long startTime = System.currentTimeMillis();
        loginPage.enterUserId("test@example.com");
        loginPage.enterPassword("password123");
        long endTime = System.currentTimeMillis();
        long inputTime = endTime - startTime;
        metrics.put("credentialInputTime", inputTime);
        
        // Log the input time
        System.out.println("Time to enter credentials: " + inputTime + " ms");
        
        // Measure button click response time
        startTime = System.currentTimeMillis();
        loginPage.clickLoginButton();
        endTime = System.currentTimeMillis();
        long clickResponseTime = endTime - startTime;
        metrics.put("loginButtonClickTime", clickResponseTime);
        
        // Log the click response time
        System.out.println("Login button click response time: " + clickResponseTime + " ms");
        
        // Assert that interaction times are within acceptable thresholds
        Assert.assertTrue(inputTime <= 1000, 
            "Credential input time " + inputTime + "ms exceeds threshold of 1000ms");
        Assert.assertTrue(clickResponseTime <= 2000, 
            "Login button click response time " + clickResponseTime + "ms exceeds threshold of 2000ms");
    }

    @Test(description = "Test JavaScript performance metrics")
    public void testJavaScriptPerformanceMetrics() {
        // Use Navigation Timing API to get detailed performance metrics
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        // Refresh the page to get fresh timing data
        driver.navigate().refresh();
        TestUtils.waitForPageLoad(driver);
        
        // Get various timing metrics using Navigation Timing API
        Long navigationStart = (Long) js.executeScript("return window.performance.timing.navigationStart");
        Long responseStart = (Long) js.executeScript("return window.performance.timing.responseStart");
        Long responseEnd = (Long) js.executeScript("return window.performance.timing.responseEnd");
        Long domComplete = (Long) js.executeScript("return window.performance.timing.domComplete");
        Long loadEventEnd = (Long) js.executeScript("return window.performance.timing.loadEventEnd");
        
        // Calculate timing metrics
        long serverResponseTime = responseStart - navigationStart;
        long pageDownloadTime = responseEnd - responseStart;
        long domProcessingTime = domComplete - responseEnd;
        long totalPageLoadTime = loadEventEnd - navigationStart;
        
        // Store metrics
        metrics.put("serverResponseTime", serverResponseTime);
        metrics.put("pageDownloadTime", pageDownloadTime);
        metrics.put("domProcessingTime", domProcessingTime);
        metrics.put("totalPageLoadTime", totalPageLoadTime);
        
        // Log the metrics
        System.out.println("Server Response Time: " + serverResponseTime + " ms");
        System.out.println("Page Download Time: " + pageDownloadTime + " ms");
        System.out.println("DOM Processing Time: " + domProcessingTime + " ms");
        System.out.println("Total Page Load Time: " + totalPageLoadTime + " ms");
        
        // Assert that metrics are within acceptable thresholds
        Assert.assertTrue(serverResponseTime <= 1000, 
            "Server response time " + serverResponseTime + "ms exceeds threshold of 1000ms");
        Assert.assertTrue(pageDownloadTime <= 1000, 
            "Page download time " + pageDownloadTime + "ms exceeds threshold of 1000ms");
        Assert.assertTrue(domProcessingTime <= 1000, 
            "DOM processing time " + domProcessingTime + "ms exceeds threshold of 1000ms");
        Assert.assertTrue(totalPageLoadTime <= configManager.getIntProperty("performanceThreshold", 3000), 
            "Total page load time " + totalPageLoadTime + "ms exceeds threshold");
    }

    @Test(description = "Test resource loading performance")
    public void testResourceLoadingPerformance() {
        // Use Resource Timing API to analyze resource loading performance
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        // Get number of resources loaded
        Long resourceCount = (Long) js.executeScript(
            "return window.performance.getEntriesByType('resource').length");
        metrics.put("resourceCount", resourceCount);
        
        // Get total resource size and load time
        Object result = js.executeScript(
            "var resources = window.performance.getEntriesByType('resource');"
            + "var totalSize = 0;"
            + "var totalTime = 0;"
            + "for (var i = 0; i < resources.length; i++) {"
            + "  totalTime += resources[i].responseEnd - resources[i].startTime;"
            + "  // Note: Resource size is not directly available in all browsers"
            + "}"
            + "return {count: resources.length, totalTime: totalTime, avgTime: totalTime/resources.length};");
        
        // Extract metrics from result
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> resourceMetrics = (Map<String, Object>) result;
            Double totalTime = (Double) resourceMetrics.get("totalTime");
            Double avgTime = (Double) resourceMetrics.get("avgTime");
            
            metrics.put("resourceTotalLoadTime", totalTime.longValue());
            metrics.put("resourceAvgLoadTime", avgTime.longValue());
            
            // Log the metrics
            System.out.println("Resource Count: " + resourceCount);
            System.out.println("Total Resource Load Time: " + totalTime + " ms");
            System.out.println("Average Resource Load Time: " + avgTime + " ms");
            
            // Assert that metrics are within acceptable thresholds
            Assert.assertTrue(avgTime <= 500, 
                "Average resource load time " + avgTime + "ms exceeds threshold of 500ms");
        }
    }

    @AfterMethod
    public void tearDownTest(ITestResult result) {
        // Add performance metrics to report
        if (result.getStatus() == ITestResult.SUCCESS) {
            for (Map.Entry<String, Long> entry : metrics.entrySet()) {
                System.out.println("Performance Metric - " + entry.getKey() + ": " + entry.getValue() + " ms");
            }
        }
        
        // Add test result to report manager
        ReportManager.getInstance().addTestResult(result.getName(), result, driver);
    }
}