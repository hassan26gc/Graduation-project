package com.mycompany.seleniumproject;
import java.io.File;
import java.io.IOException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.io.FileHandler;

public class screen {
    public static void take_screen(WebDriver driver,String path)throws IOException
    {
        TakesScreenshot srcShot=((TakesScreenshot)driver);
        File SrcFile=srcShot.getScreenshotAs(OutputType.FILE);
        FileHandler.copy(SrcFile,new File(path));
    }
}
