import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import utils.ExtentReportManager;
import java.time.Duration;

public class Register {
    WebDriver driver;
    WebDriverWait wait;
    ExtentReports report;
    ExtentTest tests;
    String baseUrl = "https://parabank.parasoft.com/parabank";
    String username;
    String password = "TestPass123";

    @BeforeMethod
    public void setup() {
        report = ExtentReportManager.getReportInstance();
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
   
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    
        username = getUniqueUsername();
    }

    @Test
public void testNavigateToRegistrationPage() {
    tests = report.createTest("TC05_Navigate To Registration Page", "Verify that the user can navigate to the registration page.");

    driver.get(baseUrl + "/index.htm");
    tests.info("Opened ParaBank home page.");

    driver.findElement(By.linkText("Register")).click();
    tests.info("Clicked on 'Register' link.");

    Assert.assertTrue(driver.getCurrentUrl().contains("register.htm"));
    tests.pass("Current URL contains 'register.htm'.");

    Assert.assertTrue(driver.getTitle().contains("ParaBank | Register"));
    tests.pass("Page title contains 'ParaBank | Register'.");
}

@Test
public void testRegisterWithValidDetails() {
    tests = report.createTest("TC06_Register With Valid Details", "Verify that a user can register successfully with valid details.");

    driver.get(baseUrl + "/register.htm");
    tests.info("Opened registration page.");

    registerUser(username, password);
    tests.info("Filled form with valid data and submitted.");

    WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
    Assert.assertEquals(successMsg.getText(), "Welcome " + username);
    tests.pass("Successfully registered. Found welcome message: 'Welcome " + username + "'");
}

@Test
public void testRegisterWithMissingFields() {
    tests = report.createTest("TC07_Register With Missing Fields", "Verify that the registration fails when required fields are missing.");

    driver.get(baseUrl + "/register.htm");
    tests.info("Opened registration page.");

    driver.findElement(By.id("customer.firstName")).sendKeys("Ali");
    tests.info("Entered only first name.");

    driver.findElement(By.xpath("//input[@value='Register']")).click();
    tests.info("Clicked Register with missing fields.");

    Assert.assertTrue(driver.getCurrentUrl().contains("register.htm"));
    tests.pass("Still on registration page as expected.");

    Assert.assertFalse(driver.getPageSource().contains("Welcome"));
    tests.pass("No 'Welcome' message found, registration correctly failed.");
}

@Test
public void testRegisterWithTakenUsername() {
    tests = report.createTest("TC08_Register With Taken Username", "Verify that registration fails if the username is already taken.");

    driver.get(baseUrl + "/register.htm");
    tests.info("Opened registration page.");

    registerUser(username, password);
    tests.info("Registered first time with username: " + username);

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
    tests.info("First registration successful.");

    driver.get(baseUrl + "/register.htm");
    tests.info("Navigated back to registration page to try same username again.");

    registerUser(username, password);
    tests.info("Tried to register again with same username.");

    WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
    Assert.assertTrue(error.getText().contains("This username already exists"));
    tests.pass("Proper error message displayed for duplicate username.");
}


    public void registerUser(String username, String password) {
        fillForm("Ali", "Hassan", "123 Nile St", "Cairo", "Cairo", "12345", "01122334455", "123-45-6789", username, password, password);
        submitForm();
    }

    public void fillForm(String fname, String lname, String address, String city, String state,
                        String zip, String phone, String ssn, String username, String pass, String confirm) {
        driver.findElement(By.id("customer.firstName")).sendKeys(fname);
        driver.findElement(By.id("customer.lastName")).sendKeys(lname);
        driver.findElement(By.id("customer.address.street")).sendKeys(address);
        driver.findElement(By.id("customer.address.city")).sendKeys(city);
        driver.findElement(By.id("customer.address.state")).sendKeys(state);
        driver.findElement(By.id("customer.address.zipCode")).sendKeys(zip);
        driver.findElement(By.id("customer.phoneNumber")).sendKeys(phone);
        driver.findElement(By.id("customer.ssn")).sendKeys(ssn);
        driver.findElement(By.id("customer.username")).sendKeys(username);
        driver.findElement(By.id("customer.password")).sendKeys(pass);
        driver.findElement(By.id("repeatedPassword")).sendKeys(confirm);
    }

    public void submitForm() {
        driver.findElement(By.xpath("//input[@value='Register']")).click();
    }

    public String getUniqueUsername() {
        return "user" + System.currentTimeMillis();
    }

    @AfterMethod
    
    public void tearDown() {
        if (driver != null) {
            System.out.println("Page Title: " + driver.getTitle());
            System.out.println("Current URL: " + driver.getCurrentUrl());
            driver.quit();
        }
    }
    
    @AfterClass
    public void closeReport() {
            report.flush();
        }
    }