package com.janitri.utils;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Utility class for managing test reports in HTML and CSV formats.
 */
public class ReportManager {
    private static final String DEFAULT_REPORT_DIR = "test-reports";
    private static final String DEFAULT_HTML_REPORT_FILE = "test-report.html";
    private static final String DEFAULT_CSV_REPORT_FILE = "test-report.csv";
    private static volatile ReportManager instance;
    private final List<TestResult> testResults;
    private final ConfigManager configManager;
    private final String reportDir;
    private final String htmlReportFile;
    private final String csvReportFile;

    /**
     * Private constructor for singleton pattern.
     */
    private ReportManager() {
        this.configManager = ConfigManager.getInstance();
        this.reportDir = configManager.getIntProperty("reportDir", DEFAULT_REPORT_DIR);
        this.htmlReportFile = configManager.getIntProperty("htmlReportFile", DEFAULT_HTML_REPORT_FILE);
        this.csvReportFile = configManager.getIntProperty("csvReportFile", DEFAULT_CSV_REPORT_FILE);
        this.testResults = new CopyOnWriteArrayList<>(); // Thread-safe list
        createReportDirectory();
    }

    /**
     * Get singleton instance of ReportManager using double-checked locking.
     *
     * @return ReportManager instance
     */
    public static ReportManager getInstance() {
        if (instance == null) {
            synchronized (ReportManager.class) {
                if (instance == null) {
                    instance = new ReportManager();
                }
            }
        }
        return instance;
    }

    /**
     * Create report directory if it doesn't exist.
     */
    private void createReportDirectory() {
        Path reportDirPath = Paths.get(reportDir);
        if (!Files.exists(reportDirPath)) {
            try {
                Files.createDirectories(reportDirPath);
            } catch (IOException e) {
                System.err.println("Failed to create report directory: " + e.getMessage());
            }
        }
    }

    /**
     * Add test result to the report.
     *
     * @param testName Name of the test
     * @param result   TestNG test result
     * @param driver   WebDriver instance for taking screenshots
     */
    public void addTestResult(String testName, ITestResult result, WebDriver driver) {
        String status = getTestStatus(result);
        String screenshotPath = "";

        // Take screenshot on failure if configured
        if (result.getStatus() == ITestResult.FAILURE &&
                configManager.getBooleanProperty("takeScreenshotOnFailure", true) &&
                driver != null) {
            screenshotPath = TestUtils.takeScreenshot(driver, testName);
        }

        // Calculate test duration
        long durationMs = result.getEndMillis() - result.getStartMillis();

        // Get error message if test failed
        String errorMessage = "";
        if (result.getStatus() == ITestResult.FAILURE && result.getThrowable() != null) {
            errorMessage = result.getThrowable().getMessage() != null ? result.getThrowable().getMessage() : "";
        }

        // Add test result to list
        testResults.add(new TestResult(
                testName,
                status,
                durationMs,
                LocalDateTime.now(),
                screenshotPath,
                errorMessage));
    }

    /**
     * Get test status as string.
     *
     * @param result TestNG test result
     * @return Test status as string
     */
    private String getTestStatus(ITestResult result) {
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                return "PASS";
            case ITestResult.FAILURE:
                return "FAIL";
            case ITestResult.SKIP:
                return "SKIP";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Initialize reports by clearing previous results.
     */
    public void initReports() {
        testResults.clear();
    }

    /**
     * Generate both HTML and CSV reports.
     */
    public void generateReports() {
        generateHtmlReport();
        generateCsvReport();
    }

    /**
     * Generate HTML report for UI tests.
     */
    public void generateHtmlReport() {
        Path reportPath = Paths.get(reportDir, htmlReportFile);
        try (FileWriter writer = new FileWriter(reportPath.toString())) {
            writeHtmlHeader(writer);
            writeReportSummary(writer);
            writeTestResultsTable(writer);
            writer.write("</body>\n</html>\n");
            System.out.println("HTML report generated at: " + reportPath);
        } catch (IOException e) {
            System.err.println("Failed to generate HTML report: " + e.getMessage());
        }
    }

    /**
     * Write HTML header for the report.
     *
     * @param writer FileWriter to write to
     * @throws IOException if writing fails
     */
    private void writeHtmlHeader(FileWriter writer) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        writer.write("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <title>Janitri Dashboard Test Report</title>\n" +
                "  <style>\n" +
                "    body { font-family: Arial, sans-serif; margin: 20px; }\n" +
                "    h1 { color: #333366; }\n" +
                "    table { border-collapse: collapse; width: 100%; }\n" +
                "    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n" +
                "    th { background-color: #f2f2f2; }\n" +
                "    .pass { color: green; }\n" +
                "    .fail { color: red; }\n" +
                "    .skip { color: orange; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <h1>Janitri Dashboard Test Report</h1>\n" +
                "  <p>Generated on: " + timestamp + "</p>\n");
    }

    /**
     * Write report summary to HTML.
     *
     * @param writer FileWriter to write to
     * @throws IOException if writing fails
     */
    private void writeReportSummary(FileWriter writer) throws IOException {
        int passCount = 0, failCount = 0, skipCount = 0;
        for (TestResult result : testResults) {
            switch (result.getStatus()) {
                case "PASS":
                    passCount++;
                    break;
                case "FAIL":
                    failCount++;
                    break;
                case "SKIP":
                    skipCount++;
                    break;
            }
        }

        writer.write("<h2>Summary</h2>\n" +
                "<p>Total Tests: " + testResults.size() + "</p>\n" +
                "<p>Passed: " + passCount + "</p>\n" +
                "<p>Failed: " + failCount + "</p>\n" +
                "<p>Skipped: " + skipCount + "</p>\n");
    }

    /**
     * Write test results table to HTML.
     *
     * @param writer FileWriter to write to
     * @throws IOException if writing fails
     */
    private void writeTestResultsTable(FileWriter writer) throws IOException {
        writer.write("<h2>Test Results</h2>\n" +
                "<table>\n" +
                "  <tr>\n" +
                "    <th>Test Name</th>\n" +
                "    <th>Status</th>\n" +
                "    <th>Duration (ms)</th>\n" +
                "    <th>Timestamp</th>\n" +
                "    <th>Screenshot</th>\n" +
                "    <th>Error Message</th>\n" +
                "  </tr>\n");

        for (TestResult result : testResults) {
            String screenshotLink = result.getScreenshotPath() != null && !result.getScreenshotPath().isEmpty()
                    ? "<a href=\"" + result.getScreenshotPath() + "\">Screenshot</a>"
                    : "N/A";
            String errorMessage = result.getErrorMessage() != null && !result.getErrorMessage().isEmpty()
                    ? result.getErrorMessage()
                    : "N/A";
            writer.write("<tr>\n" +
                    "  <td>" + result.getTestName() + "</td>\n" +
                    "  <td class=\"" + result.getStatus().toLowerCase() + "\">" + result.getStatus() + "</td>\n" +
                    "  <td>" + result.getDurationMs() + "</td>\n" +
                    "  <td>" + result.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    + "</td>\n" +
                    "  <td>" + screenshotLink + "</td>\n" +
                    "  <td>" + errorMessage + "</td>\n" +
                    "</tr>\n");
        }

        writer.write("</table>\n");
    }

    /**
     * Generate CSV report for UI tests.
     */
    public void generateCsvReport() {
        Path reportPath = Paths.get(reportDir, csvReportFile);
        try (FileWriter writer = new FileWriter(reportPath.toString())) {
            writer.write("Test Name,Status,Duration (ms),Timestamp,Screenshot,Error Message\n");
            for (TestResult result : testResults) {
                String screenshotPath = result.getScreenshotPath() != null ? result.getScreenshotPath() : "";
                String errorMessage = result.getErrorMessage() != null ? result.getErrorMessage().replace("\"", "\"\"")
                        : "";
                writer.write(String.format("%s,%s,%d,%s,%s,\"%s\"\n",
                        result.getTestName(),
                        result.getStatus(),
                        result.getDurationMs(),
                        result.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        screenshotPath,
                        errorMessage));
            }
            System.out.println("CSV report generated at: " + reportPath);
        } catch (IOException e) {
            System.err.println("Failed to generate CSV report: " + e.getMessage());
        }
    }

    /**
     * Inner class to represent a UI test result.
     */
    private static class TestResult {
        private final String testName;
        private final String status;
        private final long durationMs;
        private final LocalDateTime timestamp;
        private final String screenshotPath;
        private final String errorMessage;

        public TestResult(String testName, String status, long durationMs,
                LocalDateTime timestamp, String screenshotPath, String errorMessage) {
            this.testName = testName;
            this.status = status;
            this.durationMs = durationMs;
            this.timestamp = timestamp;
            this.screenshotPath = screenshotPath;
            this.errorMessage = errorMessage;
        }

        public String getTestName() {
            return testName;
        }

        public String getStatus() {
            return status;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getScreenshotPath() {
            return screenshotPath;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}