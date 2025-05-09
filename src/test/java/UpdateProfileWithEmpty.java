import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import utils.ExtentReportManager;

import java.time.Duration;

public class UpdateProfileWithEmpty {
    ExtentReports extent = ExtentReportManager.getReportInstance();
    ExtentTest test = extent.createTest("TC20_submitBillPayWithEmptyFields");

    @Test
    public void updateProfileWithEmptyFields_ShouldShowValidation() throws InterruptedException {
        // Setup WebDriver
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();

        try {
            test.log(Status.INFO, "Opened browser and initialized WebDriver");

            // Log in to an existing account
            driver.get("https://parabank.parasoft.com/parabank/index.htm");
            test.log(Status.INFO, "Navigated to ParaBank login page");

            driver.findElement(By.name("username")).sendKeys("oio");
            driver.findElement(By.name("password")).sendKeys("hhh444");
            driver.findElement(By.xpath("//input[@value='Log In']")).click();
            test.log(Status.INFO, "Entered credentials and clicked Login");

            // Wait for login to complete
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Update Contact Info")));
            test.pass("Login successful");

            // Navigate to Update Contact Info
            driver.findElement(By.linkText("Update Contact Info")).click();
            test.log(Status.INFO, "Navigated to 'Update Contact Info' page");

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName")));

            // Clear all fields
            driver.findElement(By.id("customer.firstName")).clear();
            driver.findElement(By.id("customer.lastName")).clear();
            driver.findElement(By.id("customer.address.street")).clear();
            driver.findElement(By.id("customer.address.city")).clear();
            driver.findElement(By.id("customer.address.state")).clear();
            driver.findElement(By.id("customer.address.zipCode")).clear();
            driver.findElement(By.id("customer.phoneNumber")).clear();
            test.log(Status.INFO, "Cleared all fields");

            Thread.sleep(1000);
            driver.findElement(By.xpath("//input[@value='Update Profile']")).click();
            test.log(Status.INFO, "Clicked 'Update Profile' with empty fields");

            // Validation check
            Thread.sleep(2000);
            String currentUrl = driver.getCurrentUrl();
            boolean stillOnPage = currentUrl.contains("updateprofile") || currentUrl.contains("update");

            if (stillOnPage) {
                test.pass("Validation worked: User remained on the update page");
            } else {
                test.fail("Validation failed: User was redirected, submission likely went through");
            }

        } catch (Exception e) {
            test.fail("Test encountered an exception: " + e.getMessage());
        } finally {
            driver.quit();
            extent.flush();
            test.info("Browser closed and report flushed");
        }
    }
}