package com.janitri.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for accessibility testing
 */
public class AccessibilityUtils {
    private static final ConfigManager configManager = ConfigManager.getInstance();

    /**
     * Check for common accessibility issues on the page
     * @param driver WebDriver instance
     * @return List of accessibility issues found
     */
    public static List<String> checkAccessibilityIssues(WebDriver driver) {
        List<String> issues = new ArrayList<>();
        
        if (!configManager.getBooleanProperty("enableAccessibilityTesting", true)) {
            return issues;
        }
        
        // Check for images without alt text
        checkImagesWithoutAltText(driver, issues);
        
        // Check for form fields without labels
        checkFormFieldsWithoutLabels(driver, issues);
        
        // Check for insufficient color contrast (basic check)
        checkColorContrast(driver, issues);
        
        // Check for missing document language
        checkDocumentLanguage(driver, issues);
        
        // Check for missing page title
        checkPageTitle(driver, issues);
        
        // Check for keyboard accessibility
        checkKeyboardAccessibility(driver, issues);
        
        return issues;
    }

    /**
     * Check for images without alt text
     * @param driver WebDriver instance
     * @param issues List to add issues to
     */
    private static void checkImagesWithoutAltText(WebDriver driver, List<String> issues) {
        List<WebElement> images = driver.findElements(By.tagName("img"));
        int count = 0;
        
        for (WebElement image : images) {
            String alt = image.getAttribute("alt");
            if (alt == null || alt.trim().isEmpty()) {
                count++;
                String src = image.getAttribute("src");
                if (src != null && !src.isEmpty()) {
                    issues.add("Image without alt text: " + src);
                } else {
                    issues.add("Image without alt text and src");
                }
            }
        }
        
        if (count > 0) {
            issues.add("Found " + count + " images without alt text");
        }
    }

    /**
     * Check for form fields without labels
     * @param driver WebDriver instance
     * @param issues List to add issues to
     */
    private static void checkFormFieldsWithoutLabels(WebDriver driver, List<String> issues) {
        List<WebElement> formFields = driver.findElements(
            By.cssSelector("input:not([type='hidden']), select, textarea"));
        int count = 0;
        
        for (WebElement field : formFields) {
            String id = field.getAttribute("id");
            if (id != null && !id.isEmpty()) {
                // Check if there's a label with a matching 'for' attribute
                List<WebElement> labels = driver.findElements(By.cssSelector("label[for='" + id + "']"));
                if (labels.isEmpty()) {
                    count++;
                    String name = field.getAttribute("name");
                    issues.add("Form field without label: " + (name != null ? name : id));
                }
            } else {
                // Check if the field is wrapped in a label
                WebElement parent = field.findElement(By.xpath("./parent::label"));
                if (parent == null) {
                    count++;
                    String name = field.getAttribute("name");
                    issues.add("Form field without label or id: " + (name != null ? name : "unknown"));
                }
            }
        }
        
        if (count > 0) {
            issues.add("Found " + count + " form fields without proper labels");
        }
    }

    /**
     * Basic check for color contrast issues
     * @param driver WebDriver instance
     * @param issues List to add issues to
     */
    private static void checkColorContrast(WebDriver driver, List<String> issues) {
        // This is a simplified check that looks for very light text on light backgrounds
        // or very dark text on dark backgrounds
        // A more comprehensive check would require a color contrast analysis library
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = ""
            + "var elements = document.querySelectorAll('*');"
            + "var issues = [];"
            + "for (var i = 0; i < elements.length; i++) {"
            + "  var el = elements[i];"
            + "  if (el.innerText && el.innerText.trim()) {"
            + "    var style = window.getComputedStyle(el);"
            + "    var bgColor = style.backgroundColor;"
            + "    var textColor = style.color;"
            + "    if ((bgColor.includes('255, 255, 255') || bgColor.includes('rgba(0, 0, 0, 0)')) "
            + "        && (textColor.includes('255, 255, 255') || textColor.includes('240, 240, 240'))) {"
            + "      issues.push('Light text on light background: ' + el.tagName + (el.className ? '.' + el.className.replace(/ /g, '.') : ''));"
            + "    }"
            + "    if ((bgColor.includes('0, 0, 0') || bgColor.includes('20, 20, 20')) "
            + "        && (textColor.includes('0, 0, 0') || textColor.includes('20, 20, 20'))) {"
            + "      issues.push('Dark text on dark background: ' + el.tagName + (el.className ? '.' + el.className.replace(/ /g, '.') : ''));"
            + "    }"
            + "  }"
            + "}"
            + "return issues;";
        
        @SuppressWarnings("unchecked")
        List<String> contrastIssues = (List<String>) js.executeScript(script);
        
        if (contrastIssues != null && !contrastIssues.isEmpty()) {
            issues.addAll(contrastIssues);
            issues.add("Found " + contrastIssues.size() + " potential color contrast issues");
        }
    }

    /**
     * Check if document language is specified
     * @param driver WebDriver instance
     * @param issues List to add issues to
     */
    private static void checkDocumentLanguage(WebDriver driver, List<String> issues) {
        WebElement html = driver.findElement(By.tagName("html"));
        String lang = html.getAttribute("lang");
        
        if (lang == null || lang.trim().isEmpty()) {
            issues.add("Document language not specified (missing lang attribute on html element)");
        }
    }

    /**
     * Check if page has a title
     * @param driver WebDriver instance
     * @param issues List to add issues to
     */
    private static void checkPageTitle(WebDriver driver, List<String> issues) {
        String title = driver.getTitle();
        
        if (title == null || title.trim().isEmpty()) {
            issues.add("Page title is missing or empty");
        }
    }

    /**
     * Check for keyboard accessibility issues
     * @param driver WebDriver instance
     * @param issues List to add issues to
     */
    private static void checkKeyboardAccessibility(WebDriver driver, List<String> issues) {
        // Check for elements with click handlers but no keyboard event handlers
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = ""
            + "var elements = document.querySelectorAll('*');"
            + "var issues = [];"
            + "for (var i = 0; i < elements.length; i++) {"
            + "  var el = elements[i];"
            + "  if (el.onclick && !el.onkeydown && !el.onkeyup && !el.onkeypress) {"
            + "    if (el.tagName !== 'A' && el.tagName !== 'BUTTON' && el.tagName !== 'INPUT') {"
            + "      issues.push('Element with click handler but no keyboard handler: ' + el.tagName + (el.className ? '.' + el.className.replace(/ /g, '.') : ''));"
            + "    }"
            + "  }"
            + "}"
            + "return issues;";
        
        @SuppressWarnings("unchecked")
        List<String> keyboardIssues = (List<String>) js.executeScript(script);
        
        if (keyboardIssues != null && !keyboardIssues.isEmpty()) {
            issues.addAll(keyboardIssues);
            issues.add("Found " + keyboardIssues.size() + " potential keyboard accessibility issues");
        }
        
        // Check for tabindex values greater than 0 (which can disrupt natural tab order)
        List<WebElement> elementsWithTabIndex = driver.findElements(By.cssSelector("[tabindex]"));
        int count = 0;
        
        for (WebElement element : elementsWithTabIndex) {
            String tabIndex = element.getAttribute("tabindex");
            try {
                if (Integer.parseInt(tabIndex) > 0) {
                    count++;
                    issues.add("Element with tabindex > 0 (disrupts natural tab order): " + 
                              element.getTagName() + 
                              (element.getAttribute("class") != null ? "." + element.getAttribute("class").replace(" ", ".") : ""));
                }
            } catch (NumberFormatException e) {
                // Ignore invalid tabindex values
            }
        }
        
        if (count > 0) {
            issues.add("Found " + count + " elements with tabindex > 0 (disrupts natural tab order)");
        }
    }

    /**
     * Run a basic accessibility audit and return a summary
     * @param driver WebDriver instance
     * @return Map containing summary of accessibility issues
     */
    public static Map<String, Object> runAccessibilityAudit(WebDriver driver) {
        Map<String, Object> result = new HashMap<>();
        List<String> issues = checkAccessibilityIssues(driver);
        
        result.put("totalIssues", issues.size());
        result.put("issues", issues);
        result.put("passesThreshold", issues.size() <= configManager.getIntProperty("accessibilityViolationThreshold", 0));
        
        return result;
    }
}