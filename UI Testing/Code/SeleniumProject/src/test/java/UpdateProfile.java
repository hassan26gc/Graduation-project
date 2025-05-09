import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import utils.ExtentReportManager;

import java.time.Duration;

public class UpdateProfile{
    WebDriver driver;
    ExtentReports extent = ExtentReportManager.getReportInstance();

    @Test
    public void registerAndUpdateProfile() throws InterruptedException {
        ExtentTest test = extent.createTest("TC01_registerAndUpdateProfile");

        // Setup ChromeDriver
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            test.log(Status.INFO, "Navigating to Registration Page");
            driver.get("https://parabank.parasoft.com/parabank/register.htm");

            test.log(Status.INFO, "Filling out registration form");
            driver.findElement(By.id("customer.firstName")).sendKeys("Mona");
            driver.findElement(By.id("customer.lastName")).sendKeys("Mamdouh");
            driver.findElement(By.id("customer.address.street")).sendKeys("Giza");
            driver.findElement(By.id("customer.address.city")).sendKeys("Giza");
            driver.findElement(By.id("customer.address.state")).sendKeys("Egypt");
            driver.findElement(By.id("customer.address.zipCode")).sendKeys("4444");
            driver.findElement(By.id("customer.phoneNumber")).sendKeys("01151266448");
            driver.findElement(By.id("customer.ssn")).sendKeys("123456789");
            driver.findElement(By.id("customer.username")).sendKeys("oio36");
            driver.findElement(By.id("customer.password")).sendKeys("hhh444");
            driver.findElement(By.id("repeatedPassword")).sendKeys("hhh444");

            test.log(Status.INFO, "Submitting registration form");
            Thread.sleep(1000);
            driver.findElement(By.xpath("//input[@value='Register']")).click();

            test.log(Status.INFO, "Waiting for welcome message");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Welcome')]")));

            test.log(Status.PASS, "Registration successful");

            test.log(Status.INFO, "Navigating to 'Update Contact Info'");
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Update Contact Info"))).click();

            test.log(Status.INFO, "Updating contact information");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.address.street"))).clear();
            driver.findElement(By.id("customer.address.street")).sendKeys("New Address");
            driver.findElement(By.id("customer.phoneNumber")).clear();
            driver.findElement(By.id("customer.phoneNumber")).sendKeys("0123456789");

            driver.findElement(By.id("customer.firstName")).clear();
            driver.findElement(By.id("customer.firstName")).sendKeys("saly");
            Thread.sleep(1000);

            driver.findElement(By.id("customer.lastName")).clear();
            driver.findElement(By.id("customer.lastName")).sendKeys("mohamed");
            Thread.sleep(1000);

            driver.findElement(By.id("customer.address.street")).clear();
            driver.findElement(By.id("customer.address.street")).sendKeys("cairo");
            Thread.sleep(1000);

            driver.findElement(By.xpath("//input[@value='Update Profile']")).click();
            test.log(Status.PASS, "Profile updated successfully");

        } catch (Exception e) {
            test.log(Status.FAIL, "Test failed: " + e.getMessage());
        } finally {
            Thread.sleep(2000);
            driver.quit();
            extent.flush();
        }
    }
}