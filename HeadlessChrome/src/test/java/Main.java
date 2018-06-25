import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Main {

    @Test
    public void Test() {
        System.setProperty("webdriver.chrome.driver","/Users/xxx/HeadlessChrome/env/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.setBinary("/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary");
        options.addArguments("headless");
        options.addArguments("disable-gpu");
        WebDriver driver = new ChromeDriver(options);
        driver.navigate().to("https://super.suncorp.com.au/ssp/public/memberAcquire/super/main.html#/personalDetails");
        Assert.assertEquals(driver.findElement(By.id("su")).getAttribute("Value"),"百度一下");
        driver.quit();

    }

}
