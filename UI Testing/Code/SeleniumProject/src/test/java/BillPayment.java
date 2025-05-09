import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
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

public class BillPayment {
    WebDriver driver;
    WebDriverWait wait;
    ExtentReports extent;
    ExtentTest test;
    ExtentTest node;

    String username;
    final String password = "Test@123";

    public String getUniqueUsername() {
        return "user" + System.currentTimeMillis();
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
    }

    @BeforeClass
    public void setupAndRegister() {
        extent = ExtentReportManager.getReportInstance();
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
        driver.get("https://parabank.parasoft.com");

        registerNewUser();
    }

    @BeforeMethod
    public void beforeMethod(Method method) {
        test = extent.createTest(method.getName());
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            test.log(Status.FAIL, result.getThrowable());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            test.log(Status.PASS, "Test passed");
        } else {
            test.log(Status.SKIP, "Test skipped");
        }
        extent.flush();
    }

    public void registerNewUser() {
        username = getUniqueUsername();

        driver.findElement(By.linkText("Register")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName"))).sendKeys("Habiba");
        driver.findElement(By.id("customer.lastName")).sendKeys("Waleed");
        driver.findElement(By.id("customer.address.street")).sendKeys("123 Main St");
        driver.findElement(By.id("customer.address.city")).sendKeys("Cairo");
        driver.findElement(By.id("customer.address.state")).sendKeys("EG");
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("12345");
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("01000000000");
        driver.findElement(By.id("customer.ssn")).sendKeys("123-45-6789");
        driver.findElement(By.id("customer.username")).sendKeys(username);
        driver.findElement(By.id("customer.password")).sendKeys(password);
        driver.findElement(By.id("repeatedPassword")).sendKeys(password);
        driver.findElement(By.xpath("//input[@value='Register']")).click();

        WebElement successMsg = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Welcome " + username + "']"))
        );
        Assert.assertTrue(successMsg.isDisplayed(), "تم تسجيل الحساب بنجاح.");
    }

    @Test(priority = 4)
    public void TC18_navigateToBillPay() {
        node = test.createNode("Navigate To Bill Pay Page");
        
        logStep("↓", "Opened ParaBank home page.");
        logStep("↓", "Clicked on Bill Pay link.");
        
        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();
        
        String currentUrl = driver.getCurrentUrl();
        boolean urlCheck = currentUrl.contains("billpay.htm");
        
        logStep(urlCheck ? "✔" : "✘", "Current URL contains billpay.htm: " + currentUrl);
        
        Assert.assertTrue(urlCheck, "تم الانتقال إلى صفحة Bill Pay بنجاح.");
    }

    @Test(priority = 5)
    public void TC19_validBillPaymentSubmission() {
        node = test.createNode("Valid Bill Payment Submission");
        
        logStep("↓", "Opened Bill Pay page.");
        driver.findElement(By.linkText("Bill Pay")).click();

        logStep("↓", "Filled payee information");
        driver.findElement(By.name("payee.name")).sendKeys("Electric Company");
        driver.findElement(By.name("payee.address.street")).sendKeys("123 Power St");
        driver.findElement(By.name("payee.address.city")).sendKeys("Cairo");
        driver.findElement(By.name("payee.address.state")).sendKeys("EG");
        driver.findElement(By.name("payee.address.zipCode")).sendKeys("12345");
        driver.findElement(By.name("payee.phoneNumber")).sendKeys("01111111111");
        driver.findElement(By.name("payee.accountNumber")).sendKeys("12345678");
        driver.findElement(By.name("verifyAccount")).sendKeys("12345678");
        driver.findElement(By.name("amount")).sendKeys("150");

        logStep("↓", "Selected from account");
        new Select(driver.findElement(By.name("fromAccountId"))).selectByIndex(0);

        logStep("↓", "Clicked Send Payment button");
        driver.findElement(By.xpath("//input[@value='Send Payment']")).click();

        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h1[text()='Bill Payment Complete']")
        ));
        
        boolean isConfirmed = confirmation.isDisplayed();
        logStep(isConfirmed ? "✔" : "✘", "Bill Payment Complete message displayed");
        
        Assert.assertTrue(isConfirmed, "تمت عملية الدفع بنجاح.");
    }

    @Test(priority = 6)
    public void TC20_submitBillPayWithEmptyFields() {
        node = test.createNode("Submit Bill Pay With Empty Fields");
        
        logStep("↓", "Opened Bill Pay page");
        driver.findElement(By.linkText("Bill Pay")).click();
        
        String initialUrl = driver.getCurrentUrl();
        logStep("↓", "Initial URL: " + initialUrl);
        
        logStep("↓", "Clicked Send Payment without filling fields");
        driver.findElement(By.xpath("//input[@value='Send Payment']")).click();
        
        String currentUrl = driver.getCurrentUrl();
        boolean stillOnForm = currentUrl.equals(initialUrl); 
        
        boolean errorsDisplayed = driver.findElements(By.cssSelector(".error")).size() > 0;
        
        logStep(stillOnForm ? "✔" : "✘", "Still on payment form after submission");
        logStep(errorsDisplayed ? "✔" : "✘", "Error messages displayed");
        
        Assert.assertTrue(stillOnForm, "لم يبقَ في صفحة الدفع بعد الإرسال الفارغ");
        Assert.assertTrue(errorsDisplayed, "لم تظهر رسائل الخطأ المطلوبة");
    }

    private void logStep(String status, String details) {
        String timestamp = getCurrentTime();
        String message = String.format("| %s | %s | %s |", status, timestamp, details);
        
        if (status.equals("✔")) {
            node.log(Status.PASS, message);
        } else if (status.equals("✘")) {
            node.log(Status.FAIL, message);
        } else {
            node.log(Status.INFO, message);
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