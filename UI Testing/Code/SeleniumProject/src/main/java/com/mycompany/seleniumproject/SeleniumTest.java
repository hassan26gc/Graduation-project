package com.mycompany.seleniumproject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class SeleniumTest {
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        WebDriver driver = new ChromeDriver();
        driver.get("https://parabank.parasoft.com/");
        System.out.println("Title: " + driver.getTitle());
        try {
            Thread.sleep(10000); 
        } catch (InterruptedException e) {
        }
        driver.quit();}
}
