# API Testing Framework for Janitri

This document provides an overview of the API testing framework implemented for the Janitri project.

## Overview

The API testing framework is built using:
- **REST Assured**: For API request/response handling
- **TestNG**: For test execution and assertions
- **Jackson**: For JSON processing
- **JSON Schema Validator**: For response schema validation

## Test Structure

The API tests are organized into the following categories:

1. **Authentication Tests**
   - `LoginApiTest`: Tests for login functionality, token handling, and authentication errors
   - `RegistrationApiTest`: Tests for user registration, validation, and error handling

2. **Data Validation Tests**
   - `DataValidationApiTest`: Tests to ensure API responses contain correct data structures and values

3. **Integration Tests**
   - `IntegrationApiTest`: Tests that verify interactions between different API endpoints and complete user workflows

4. **Performance Tests**
   - `PerformanceApiTest`: Tests for API response times, throughput, and behavior under load

5. **Security Tests**
   - `SecurityApiTest`: Tests for common API security vulnerabilities like SQL injection, XSS, JWT tampering, etc.

## Base Classes

- `BaseApiTest`: Base class for all API tests, handles setup and teardown operations

## Utility Classes

- `ApiUtils`: Provides utility methods for API testing, including request sending, response validation, and data extraction
- `ConfigManager`: Manages test configuration properties
- `ReportManager`: Handles test reporting for both UI and API tests

## Configuration

API testing configuration is managed through the `default_config.properties` file, which includes:

```properties
# API Testing Configuration
apiBaseUrl=https://api.janitri.in
logApiRequests=true
logApiResponses=true
apiTimeout=30000
apiMaxResponseTime=5000

# Security Testing Configuration
enableSecurityTests=true
enableJwtVulnerabilityTests=true
```

## Running the Tests

### Prerequisites

- Java 8 or higher
- Maven

### Command Line Execution

To run all API tests:

```bash
mvn clean test -DsuiteXmlFile=src/test/resources/testng-api-suite.xml
```

To run a specific test class:

```bash
mvn clean test -Dtest=com.janitri.tests.api.LoginApiTest
```

### IDE Execution

1. Import the project into your IDE (Eclipse, IntelliJ IDEA, etc.)
2. Right-click on the `testng-api-suite.xml` file and select "Run As" > "TestNG Suite"

## Test Reports

After test execution, reports are generated in:

- HTML Report: `test-reports/api-test-report.html`
- CSV Report: `test-reports/api-test-report.csv`

## Adding New Tests

To add new API tests:

1. Create a new test class that extends `BaseApiTest`
2. Implement test methods with appropriate TestNG annotations
3. Use `ApiUtils` methods to send requests and validate responses
4. Add the new test class to the `testng-api-suite.xml` file

## Best Practices

1. **Isolation**: Each test should be independent and not rely on the state from other tests
2. **Clear Assertions**: Use descriptive assertion messages
3. **Proper Setup/Teardown**: Clean up any test data created during tests
4. **Error Handling**: Implement proper error handling and logging
5. **Parameterization**: Use TestNG data providers for parameterized tests
6. **Meaningful Test Names**: Use descriptive test method names

## Example Test

```java
@Test(description = "Test successful login with valid credentials")
public void testSuccessfulLogin() {
    // Create request body
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("email", "test@janitri.in");
    requestBody.put("password", "Test@123");
    
    // Send login request
    Response response = ApiUtils.sendPostRequest("/auth/login", null, requestBody);
    
    // Validate response
    ApiUtils.validateStatusCode(response, 200);
    ApiUtils.validateFieldExists(response, "token");
    ApiUtils.validateFieldExists(response, "user");
    ApiUtils.validateField(response, "user.email", "test@janitri.in");
}
```

## Troubleshooting

- **Connection Issues**: Verify the `apiBaseUrl` in the configuration file
- **Authentication Failures**: Check if the test credentials are valid
- **Schema Validation Errors**: Ensure the expected schema matches the actual API response
- **Performance Test Failures**: Adjust the thresholds in the performance tests based on the environment

## Maintenance

Regularly update the tests when:
- API endpoints change
- Response structures are modified
- New features are added to the API
- Security requirements change