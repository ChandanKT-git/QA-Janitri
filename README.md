# Janitri Dashboard Login Page Automation  

This project automates the testing of the Janitri Dashboard login page UI using Java, Selenium WebDriver, and the Page Object Model design pattern.

## Project Structure

```
QA-Janitri/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── janitri/
│   │               ├── pages/
│   │               │   └── LoginPage.java
│   │               └── utils/
│   │                   └── TestUtils.java
│   └── test/
│       └── java/
│           └── com/
│               └── janitri/
│                   └── tests/
│                       ├── BaseTest.java
│                       └── LoginPageTest.java
├── pom.xml
├── testng.xml
└── README.md
```

## Technologies Used

- Java
- Selenium WebDriver
- TestNG
- Maven
- WebDriverManager

## Test Scenarios

The following test scenarios are automated:

1. Verify login button is disabled when fields are empty
2. Verify password masking/unmasking functionality
3. Verify invalid login shows error message
4. Verify presence of page elements (title, input fields, eye icon)

## How to Run the Tests

### Prerequisites

- Java JDK 11 or higher
- Maven
- Chrome browser

### Steps to Run

1. Clone the repository
2. Navigate to the project directory
3. Run the following command:

```bash
mvn clean test
```

## Page Object Model (POM) Implementation

This project follows the Page Object Model design pattern, which provides the following benefits:

- Separation of page-specific code from test code
- Reusable page methods
- Improved test maintenance
- Reduced code duplication

## Components

### BaseTest.java

Handles browser setup and teardown for all test classes.

### LoginPage.java

Contains all the page elements and methods for interacting with the login page.

### LoginPageTest.java

Contains all the test methods for testing the login page functionality.

### TestUtils.java

Contains utility methods for common operations like waiting for elements, taking screenshots, etc.