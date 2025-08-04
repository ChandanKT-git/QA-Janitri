package com.janitri.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;

import java.time.Duration;

/**
 * Factory class for creating WebDriver instances
 */
public class DriverFactory {
    private static final ConfigManager configManager = ConfigManager.getInstance();

    /**
     * Create a WebDriver instance based on configuration
     * 
     * @param headless2
     * @param browser2
     * @return WebDriver instance
     */
    public static WebDriver createDriver(String browser2, boolean headless2) {
        String browser = configManager.getProperty("browser", "chrome").toLowerCase();
        boolean headless = configManager.getBooleanProperty("headless", false);
        int timeout = configManager.getIntProperty("timeout", 10);
        int pageLoadTimeout = configManager.getIntProperty("pageLoadTimeout", 30000);
        int scriptTimeout = configManager.getIntProperty("scriptTimeout", 30000);

        WebDriver driver;

        switch (browser) {
            case "firefox":
                driver = createFirefoxDriver(headless);
                break;
            case "edge":
                driver = createEdgeDriver(headless);
                break;
            case "safari":
                driver = createSafariDriver();
                break;
            case "chrome":
            default:
                driver = createChromeDriver(headless);
                break;
        }

        // Configure timeouts
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeout));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(pageLoadTimeout));
        driver.manage().timeouts().scriptTimeout(Duration.ofMillis(scriptTimeout));

        // Maximize window
        driver.manage().window().maximize();

        return driver;
    }

    /**
     * Create a Chrome WebDriver instance
     * 
     * @param headless Whether to run in headless mode
     * @return ChromeDriver instance
     */
    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        // Configure Chrome options
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-extensions");

        // Set headless mode if configured
        if (headless) {
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        }

        // Check if device emulation is enabled
        if (configManager.getBooleanProperty("emulateDevice", false)) {
            String deviceName = configManager.getProperty("deviceName", "iPhone X");
            options.addArguments(
                    "--user-agent=Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");
            options.addArguments("--window-size=375,812"); // iPhone X dimensions
        }

        return new ChromeDriver(options);
    }

    /**
     * Create a Firefox WebDriver instance
     * 
     * @param headless Whether to run in headless mode
     * @return FirefoxDriver instance
     */
    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();

        // Set headless mode if configured
        if (headless) {
            options.addArguments("-headless");
            options.addArguments("-width=1920");
            options.addArguments("-height=1080");
        }

        return new FirefoxDriver(options);
    }

    /**
     * Create an Edge WebDriver instance
     * 
     * @param headless Whether to run in headless mode
     * @return EdgeDriver instance
     */
    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();

        // Configure Edge options
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");

        // Set headless mode if configured
        if (headless) {
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        }

        return new EdgeDriver(options);
    }

    /**
     * Create a Safari WebDriver instance
     * 
     * @return SafariDriver instance
     */
    private static WebDriver createSafariDriver() {
        // Safari doesn't support headless mode
        return new SafariDriver();
    }
}