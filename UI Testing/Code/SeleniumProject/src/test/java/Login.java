import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import utils.ExtentReportManager;

public class Login{
    WebDriver driver;
    WebDriverWait wait;
    ExtentReports extent;
    ExtentTest test;
    ExtentTest node;
    String registeredUsername = "";
    String registeredPassword = "P@ssw0rd123";
    String baseUrl = "https://parabank.parasoft.com/parabank/index.htm";

    @BeforeClass
    public void setup() {
        extent = ExtentReportManager.getReportInstance();
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        registerNewUser();
    }

    private void registerNewUser() {
        driver.get(baseUrl);
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        registerLink.click();

        String timestamp = String.valueOf(System.currentTimeMillis());
        registeredUsername = "user" + timestamp.substring(6);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customerForm")));
        
        driver.findElement(By.id("customer.firstName")).sendKeys("Test");
        driver.findElement(By.id("customer.lastName")).sendKeys("User");
        driver.findElement(By.id("customer.address.street")).sendKeys("123 Test St");
        driver.findElement(By.id("customer.address.city")).sendKeys("Testville");
        driver.findElement(By.id("customer.address.state")).sendKeys("TS");
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("12345");
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("1234567890");
        driver.findElement(By.id("customer.ssn")).sendKeys("123-45-6789");
        driver.findElement(By.id("customer.username")).sendKeys(registeredUsername);
        driver.findElement(By.id("customer.password")).sendKeys(registeredPassword);
        driver.findElement(By.id("repeatedPassword")).sendKeys(registeredPassword);
        
        driver.findElement(By.xpath("//input[@value='Register']")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[contains(text(),'Welcome')]")));
        
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();
    }

    @BeforeMethod
    public void beforeMethod(Method method) {
        test = extent.createTest(method.getName());
        driver.get(baseUrl);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        switch (result.getStatus()) {
            case ITestResult.FAILURE -> test.log(Status.FAIL, result.getThrowable());
            case ITestResult.SUCCESS -> test.log(Status.PASS, "Test passed");
            default -> test.log(Status.SKIP, "Test skipped");
        }
        extent.flush();
        
        if (!driver.getCurrentUrl().equals(baseUrl)) {
            driver.get(baseUrl);
        }
    }

    // Test Case 01: Verify valid login with correct username/password
    @Test(priority = 1)
    public void TC01_verifyValidLogin() {
        node = test.createNode("Verify valid login with registered credentials");
        
        logStep("↓", "Using registered username: " + registeredUsername);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        usernameField.sendKeys(registeredUsername);
        
        logStep("↓", "Using registered password: " + registeredPassword.replaceAll("\\.", "*"));
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys(registeredPassword);
        
        logStep("↓", "Clicking Login button");
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));
        loginButton.click();
        
        WebElement welcomeMessage = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Accounts Overview')]"))
        );
        
        boolean isLoggedIn = welcomeMessage.isDisplayed();
        logStep(isLoggedIn ? "✔" : "✘", "User successfully logged in");
        
        Assert.assertTrue(isLoggedIn, "Login should be successful with registered credentials");
        
        logStep("↓", "Clicking Logout link");
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();
    }

    // Test Case 02: Verify login fails with incorrect credentials
    @Test(priority = 2)
public void TC02_verifyLoginFailsWithIncorrectCredentials() {
    node = test.createNode("Verify login fails with incorrect credentials");
    
    logStep("↓", "Entering incorrect username: wronguser");
    WebElement usernameField = driver.findElement(By.name("username"));
    usernameField.clear();
    usernameField.sendKeys("wronguser");
    
    logStep("↓", "Entering incorrect password: wrongpass");
    WebElement passwordField = driver.findElement(By.name("password"));
    passwordField.clear();
    passwordField.sendKeys("wrongpass");
    
    logStep("↓", "Clicking Login button");
    WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));
    loginButton.click();
    
    try {
        WebElement errorMessage = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//p[contains(text(),'Error') or contains(text(),'error') or contains(@class,'error')]"))
        );
        
        boolean loginFailed = errorMessage.isDisplayed();
        logStep(loginFailed ? "✔" : "✘", "Login failed as expected");
        
        Assert.assertTrue(loginFailed, "Login should fail with incorrect credentials");
    } catch (TimeoutException e) {
        logStep("✘", "Timeout while waiting for error message");
        Assert.fail("Timeout while waiting for error message: " + e.getMessage());
    }
}

    // Test Case 03: Verify login fails with blank username/password
    @Test(priority = 3)
    public void TC03_verifyLoginFailsWithBlankCredentials() {
        node = test.createNode("Verify login fails with blank credentials");
        
        logStep("↓", "Leaving username field blank");
        WebElement usernameField = driver.findElement(By.name("username"));
        usernameField.clear();
        
        logStep("↓", "Leaving password field blank");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.clear();
        
        logStep("↓", "Clicking Login button");
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));
        loginButton.click();
        
        // Verify error message appears
        boolean errorDisplayed = !driver.findElements(By.xpath("//p[contains(@class,'error')]")).isEmpty();
        logStep(errorDisplayed ? "✔" : "✘", "Error message displayed for blank fields");
        
        Assert.assertTrue(errorDisplayed, "Error message should appear for blank credentials");
    }

    // Test Case 04: Validate error message on failed login
    @Test(priority = 4)
    public void TC04_validateErrorMessageOnFailedLogin() {
        node = test.createNode("Validate error message on failed login");
        
        logStep("↓", "Entering incorrect username: testuser");
        WebElement usernameField = driver.findElement(By.name("username"));
        usernameField.sendKeys("testuser");
        
        logStep("↓", "Entering incorrect password: wrongpass");
        WebElement passwordField = driver.findElement(By.name("password"));
        passwordField.sendKeys("wrongpass");
        
        logStep("↓", "Clicking Login button");
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));
        loginButton.click();
        
        WebElement errorMessage = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(@class,'error')]"))
        );
        
        String actualError = errorMessage.getText();
        String expectedError = "The username and password could not be verified.";
        
        logStep("↓", "Actual error message: " + actualError);
        boolean errorCorrect = actualError.contains(expectedError);
        logStep(errorCorrect ? "✔" : "✘", "Error message validation");
        
        Assert.assertTrue(errorCorrect, "Error message should contain: " + expectedError);
    }

    private void logStep(String status, String details) {
        String message = String.format("<b>%s</b>    %s    %s", 
                                     status, getCurrentTime(), details);
        
        switch (status) {
            case "✔" -> node.log(Status.PASS, message);
            case "✘" -> node.log(Status.FAIL, message);
            default -> node.log(Status.INFO, message);
        }
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        extent.flush();
    }
}