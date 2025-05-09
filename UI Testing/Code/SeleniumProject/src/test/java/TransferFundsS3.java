import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

public class TransferFundsS3 {
    WebDriver driver;
    WebDriverWait wait;
    String username;
    final String password = "Test@123";
    ExtentReports extent;
    ExtentTest test;
    private String generateRandomFourDigits() {
        return String.format("%04d", new Random().nextInt(10000));
    }
    @BeforeClass
    public void setup() {
        extent = new ExtentReports();
        extent.attachReporter(new ExtentSparkReporter("TransferFundsS3.html"));
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test(priority = 1)
    public void registerNewUser() {
        test = extent.createTest("Register New User", "Test user registration on Parabank");
        try {
            test.log(Status.INFO, "Clicking on Register link");
            driver.findElement(By.linkText("Register")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName")));
            test.log(Status.INFO, "Filling registration form");
            driver.findElement(By.id("customer.firstName")).sendKeys("Toqa");
            driver.findElement(By.id("customer.lastName")).sendKeys("Hussein");
            driver.findElement(By.id("customer.address.street")).sendKeys("123 Street");
            driver.findElement(By.id("customer.address.city")).sendKeys("Cairo");
            driver.findElement(By.id("customer.address.state")).sendKeys("State");
            driver.findElement(By.id("customer.address.zipCode")).sendKeys("12345");
            driver.findElement(By.id("customer.phoneNumber")).sendKeys("01012345678");
            driver.findElement(By.id("customer.ssn")).sendKeys("123-45-6789");
            String username = "ToqaHussen" + generateRandomFourDigits();
            driver.findElement(By.id("customer.username")).sendKeys(username);
            test.log(Status.INFO, "Generated Username: " + username);
            driver.findElement(By.id("customer.password")).sendKeys(password);
            driver.findElement(By.id("repeatedPassword")).sendKeys(password);
            test.log(Status.INFO, "Submitting registration form");
            driver.findElement(By.xpath("//input[@value='Register']")).click();
            test.log(Status.INFO, "Verifying registration success");
            WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Your account was created successfully')]")));
            Assert.assertTrue(successMsg.isDisplayed());
            test.log(Status.PASS, "User registered successfully");
        } catch (Exception e) {
            test.log(Status.FAIL, "Registration failed: " + e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2)
    public void transferWithoutDataWhileLoggedIn() {
        test = extent.createTest("Transfer Without Data While Logged In", "Test transfer without entering data");
        try {
            test.log(Status.INFO, "Clicking on Transfer Funds link");
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds"))).click();
            test.log(Status.INFO, "Clicking Transfer button without data");
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@value='Transfer']"))).click();
            test.log(Status.INFO, "Verifying error message");
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='showError']/p")));
            Assert.assertTrue(error.isDisplayed(), "Error message not displayed");
            test.log(Status.PASS, "Error message displayed as expected");
        } catch (Exception e) {
            test.log(Status.FAIL, "Error message not displayed: " + e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3)
    public void logoutAndAccessProtectedPage() {
        test = extent.createTest("Logout And Access Protected Page", "Test logout and access protected page");
        try {
            test.log(Status.INFO, "Clicking on Log Out link");
            driver.findElement(By.linkText("Log Out")).click();
            test.log(Status.INFO, "Verifying login page is displayed");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            test.log(Status.INFO, "Accessing protected page");
            driver.get("https://parabank.parasoft.com/parabank/overview.htm");
            test.log(Status.INFO, "Verifying login page after accessing protected page");
            WebElement loginBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            Assert.assertTrue(loginBox.isDisplayed());
            test.log(Status.PASS, "Successfully logged out and accessed protected page");
        } catch (Exception e) {
            test.log(Status.FAIL, "Logout or protected page access failed: " + e.getMessage());
            throw e;
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        extent.flush();
    }
}