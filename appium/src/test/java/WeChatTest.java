import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;

import static org.junit.Assert.assertEquals;


public class WeChatTest {

    AppiumDriver driver;

    @Before
    public void setUp() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("appium-version", "1.0");
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("platformVersion", "5.0");
        capabilities.setCapability("deviceName", "192.168.58.101:5555");
        capabilities.setCapability("app", "/Users/syyan/Thoughtworks/QA/Appium-Workshop/weixin6322android821.apk");
        driver = new AndroidDriver(new URL("http://0.0.0.0:4723/wd/hub"), capabilities);
    }
    @After
    public void tearDown() throws Exception {
        driver.quit();
    }

    @Test
    public void Login() throws InterruptedException {

        WebElement loginButton = (new WebDriverWait(driver,60))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("com.tencent.mm:id/c32")));
        loginButton.click();

        WebElement LoginModeChangeButton = (new WebDriverWait(driver,60))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("com.tencent.mm:id/b58")));
        LoginModeChangeButton.click();

        WebElement AccountTextField = (new WebDriverWait(driver,60))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("com.tencent.mm:id/ew")));
        AccountTextField.sendKeys("18200289607");

        WebElement PasswordTextField;
        PasswordTextField = driver.findElement(By.xpath("//android.widget.FrameLayout[1]/android.view.View[1]/android.widget.FrameLayout[2]/android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[2]/android.widget.EditText[1]"));
        PasswordTextField.sendKeys("2739393");

        WebElement LoginButton;
        LoginButton = driver.findElement(By.id("com.tencent.mm:id/b4n"));
        LoginButton.click();

        WebElement NoteButton = (new WebDriverWait(driver,60))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("com.tencent.mm:id/bhd")));
        NoteButton.click();

        WebElement SelectGroup = (new WebDriverWait(driver,60))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//android.widget.FrameLayout[1]/android.view.View[1]/android.widget.FrameLayout[1]/android.widget.FrameLayout[1]/android.widget.LinearLayout[1]/android.support.v4.view.ViewPager[1]/android.widget.FrameLayout[1]/android.widget.FrameLayout[1]/android.widget.RelativeLayout[1]/android.widget.ListView[1]/android.widget.LinearLayout[1]")));
        SelectGroup.click();

        WebElement textArea = (new WebDriverWait(driver,60))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("com.tencent.mm:id/yv")));
        textArea.sendKeys("Appium Test!");

        WebElement sendButton = (new WebDriverWait(driver,60))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("com.tencent.mm:id/z1")));
        sendButton.click();

        WebElement textMessage = (new WebDriverWait(driver,60))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("com.tencent.mm:id/gw")));
        assertEquals("Appium Test!", textMessage.getText());
    }
}