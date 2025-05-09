import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Random;

public class TransferFundsS1{
    WebDriver driver;
    WebDriverWait wait;
    static ExtentReports extent;
    ExtentTest test;

    private String generateRandomFourDigits() {
        return String.format("%04d", new Random().nextInt(10000));
    }
    @BeforeClass
    public void setup() {
        // Initialize ExtentReports
        ExtentSparkReporter spark = new ExtentSparkReporter("TransferFundsS1.html");
        extent = new ExtentReports();
        extent.attachReporter(spark);

        // Create screenshots directory
        new File(System.getProperty("user.dir") + "/screenshots").mkdirs();

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Test(priority = 1)
    public void registerTest() {
        test = extent.createTest("Register Test", "Test user registration on Parabank");
        try {
            test.log(Status.INFO, "Clicking on Register link");
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register"))).click();

            test.log(Status.INFO, "Filling registration form");
            driver.findElement(By.id("customer.firstName")).sendKeys("Toqa");
            driver.findElement(By.id("customer.lastName")).sendKeys("Hussen");
            driver.findElement(By.id("customer.address.street")).sendKeys("123 Main St");
            driver.findElement(By.id("customer.address.city")).sendKeys("Cairo");
            driver.findElement(By.id("customer.address.state")).sendKeys("Cairo");
            driver.findElement(By.id("customer.address.zipCode")).sendKeys("12345");
            driver.findElement(By.id("customer.phoneNumber")).sendKeys("0123456789");
            driver.findElement(By.id("customer.ssn")).sendKeys("123-45-6789");
            String username = "ToqaHussen" + generateRandomFourDigits();
            driver.findElement(By.id("customer.username")).sendKeys(username);
            test.log(Status.INFO, "Generated Username: " + username);
            driver.findElement(By.id("customer.password")).sendKeys("1234");
            driver.findElement(By.id("repeatedPassword")).sendKeys("1234");

            test.log(Status.INFO, "Submitting registration form");
            driver.findElement(By.xpath("//input[@value='Register']")).click();

            WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Welcome')]")));
            if (!welcomeMessage.getText().contains("Welcome")) {
                throw new RuntimeException("Registration failed");
            }
            test.log(Status.PASS, "Registration successful");
        } catch (Exception e) {
            test.log(Status.FAIL, "Registration failed: " + e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2, dependsOnMethods = "registerTest")
    public void openAccountTest() throws Exception {
        test = extent.createTest("Open Account Test", "Test opening a new savings account");
        try {
            test.log(Status.INFO, "Clicking on Open New Account link");
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account"))).click();

            test.log(Status.INFO, "Selecting account type as SAVINGS");
            new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.id("type")))).selectByVisibleText("SAVINGS");

            test.log(Status.INFO, "Selecting from account");
            WebElement fromAccountElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("fromAccountId")));
            Select fromAccount = new Select(fromAccountElement);
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//select[@id='fromAccountId']/option")));
            if (fromAccount.getOptions().isEmpty()) {
                throw new RuntimeException("No accounts in fromAccount dropdown");
            }
            fromAccount.selectByIndex(0);

            test.log(Status.INFO, "Submitting new account form");
            driver.findElement(By.xpath("//input[@value='Open New Account']")).click();

            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Account Opened')]")));
            WebElement accountNumber = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newAccountId")));
            if (!successMessage.getText().contains("Account Opened") || accountNumber.getText().isEmpty()) {
                throw new RuntimeException("Account opening failed");
            }
            test.log(Status.PASS, "Account opened: " + accountNumber.getText());

            test.log(Status.INFO, "Navigating to Accounts Overview");
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountTable")));
            test.log(Status.PASS, "Updated account list displayed");
            Thread.sleep(10000);
        } catch (Exception e) {
            test.log(Status.FAIL, "Account opening failed: " + e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3, dependsOnMethods = "openAccountTest")
    public void transferFundsTest() {
        test = extent.createTest("Transfer Funds Test", "Test transferring funds between accounts");
        try {
            test.log(Status.INFO, "Clicking on Transfer Funds link");
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds"))).click();
            test.log(Status.INFO, "Entering transfer amount");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount"))).sendKeys("100");

            test.log(Status.INFO, "Selecting from and to accounts");
            WebElement fromAccountElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("fromAccountId")));
            Select fromAccount = new Select(fromAccountElement);
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//select[@id='fromAccountId']/option")));
            WebElement toAccountElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("toAccountId")));
            Select toAccount = new Select(toAccountElement);
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//select[@id='toAccountId']/option")));

            if (fromAccount.getOptions().size() < 2 || toAccount.getOptions().size() < 2) {
                throw new RuntimeException("Not enough accounts for transfer");
            }
            test.log(Status.INFO, "From accounts: " + fromAccount.getOptions().size() + ", To accounts: " + toAccount.getOptions().size());

            fromAccount.selectByIndex(0);
            toAccount.selectByIndex(1);
            test.log(Status.INFO, "Submitting transfer request");
            driver.findElement(By.xpath("//input[@value='Transfer']")).click();

            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Transfer Complete')]")));
            if (!successMessage.getText().contains("Transfer Complete")) {
                throw new RuntimeException("Transfer failed");
            }
            test.log(Status.PASS, "Transfer submitted successfully");
        } catch (Exception e) {
            test.log(Status.FAIL, "Transfer failed: " + e.getMessage());
            throw e;
        }
    }

    @Test(priority = 4, dependsOnMethods = "transferFundsTest")
    public void verifyTransferTest() {
        test = extent.createTest("Verify Transfer Test", "Verify transfer completion");
        try {
            test.log(Status.INFO, "Verifying transfer completion");
            WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Transfer Complete')]")));
            if (!confirmation.getText().contains("Transfer Complete")) {
                throw new RuntimeException("Transfer verification failed");
            }
            test.log(Status.PASS, "Transfer verified successfully");
        } catch (Exception e) {
            test.log(Status.FAIL, "Transfer verification failed: " + e.getMessage());
            throw e;
        }
    }

    @Test(priority = 5, alwaysRun = true)
    public void logoutTest() {
        test = extent.createTest("Logout Test", "Test user logout");
        try {
            test.log(Status.INFO, "Clicking on Log Out link");
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
            test.log(Status.INFO, "Verifying login page is displayed");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            test.log(Status.PASS, "Logged out successfully");
        } catch (Exception e) {
            test.log(Status.FAIL, "Logout failed: " + e.getMessage());
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