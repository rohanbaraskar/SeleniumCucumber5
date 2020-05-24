package com.automation.utilities;

import com.automation.helpers.KEYS;
import com.automation.helpers.ProjectConstants;
import com.automation.scripts.Command;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverUtils {

    private WebDriver driver;

    private ThisRun thisRun = ThisRun.getInstance();

    private static Logger logger = LogManager.getLogger(DriverUtils.class.getName());
    private String browser;
    private String platform;

   /* public DriverUtils(String browser) {
        this.browser = browser;
    }*/

    public DriverUtils(String platform, String browser) {
        this.platform = platform.toLowerCase();
        this.browser = browser.toLowerCase();
    }


    private WebDriver instantiateChromeDriver() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.get(thisRun.getAsString(KEYS.APP_URL.toString()));
        driver.manage().window().maximize();
        return driver;
    }

    private WebDriver instantiateAndroidDriver() throws MalformedURLException {
        // appium --allow-insecure chromedriver_autodownload
        driver = new AndroidDriver<>(new URL("http://0.0.0.0:4723/wd/hub"), setCapabilitiesForAndroid());
        driver.get(thisRun.getAsString(KEYS.APP_URL.toString()));
        return driver;
    }

    private DesiredCapabilities setCapabilitiesForAndroid() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.UDID, thisRun.getAsString(KEYS.DEVICE_ID.toString())); //"emulator-5554"
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android Emulator"); //"emulator-5554"
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, "9.0");
        //  capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "uiautomator2");
        capabilities.setCapability(MobileCapabilityType.BROWSER_NAME, "Chrome");
        capabilities.setCapability("nativeWebScreenshot", true);
        // capabilities.setCapability("chromedriverUseSystemExecutable", true);
        // capabilities.setCapability("autoGrantPermissions", true);

        return capabilities;
    }

    private WebDriver instantiateIE11Driver() {
        String cmd = "REG ADD \"HKEY_CURRENT_USER\\Software\\Microsoft\\Internet Explorer\\New Windows\" /F /V \"PopupMgr\" /T REG_SZ /D \"no\"";
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            System.out.println("Error ocured!");
        }
        WebDriverManager.iedriver().setup();
        driver = new InternetExplorerDriver();
        driver.get(thisRun.getAsString(KEYS.APP_URL.toString()));
        driver.manage().window().maximize();
        return driver;
    }

    private WebDriver instantiateFireFoxDriver() {
        WebDriverManager.firefoxdriver().setup();
        driver = new FirefoxDriver();
        driver.get(thisRun.getAsString(KEYS.APP_URL.toString()));
        driver.manage().window().maximize();
        return driver;
    }

    private WebDriver instantiateRemoteWebDriver() {
        try {
            Command.execCommand("test");
            driver = new RemoteWebDriver(getBrowserCapabilities(getBrowserNamesForRemoteWebdriver()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        driver.get(thisRun.getAsString(KEYS.APP_URL.toString()));
        driver.manage().window().fullscreen();
        return driver;
    }

    private String getBrowserNamesForRemoteWebdriver() {
        return thisRun.getAsString(KEYS.REMOTEWEBDRIVER_BROWSERS.toString());
    }

    private DesiredCapabilities getBrowserCapabilities(String browserType) {
        switch (browserType) {
            case "firefox":
                logger.info("Opening firefox driver in Node");
                return DesiredCapabilities.firefox();
            case "chrome":
                logger.info("Opening chrome driver in Node");
                return DesiredCapabilities.chrome();
            case "IE":
                logger.info("Opening IE driver in Node");
                return DesiredCapabilities.internetExplorer();
            default:
                throw new InvalidArgumentException("browser : " + browserType + " is invalid.");
        }
    }

    public WebDriver instantiateMobileEmulatorDriver() {

        String driverToBeLoaded = thisRun.getAsString(KEYS.OS_NAME.toString()).contains("Windows") ? "chromedriver_win.exe" : "chromedriver_mac";
        System.setProperty("webdriver.chrome.driver", thisRun.getAsString(KEYS.TEST_RESOURCES.toString()) + "/" + driverToBeLoaded);

        driver = new ChromeDriver(setChromeOptions());
        driver.get(thisRun.getAsString(KEYS.APP_URL.toString()));
        return driver;
    }

    private List<String> addChromeArguments() {
        List<String> chromeArguments = new ArrayList<>();
        chromeArguments.add("--test-type");
        chromeArguments.add("--browser-test");
        chromeArguments.add("--disable-popup-blocking");
        chromeArguments.add("--disable-extensions");
        chromeArguments.add("--disable-infobars");
        chromeArguments.add("--disable-notifications");
        chromeArguments.add("--no-default-browser-check");
        chromeArguments.add("--allow-file-access");
        chromeArguments.add("--allow-file-access-from-files");
        chromeArguments.add("--allow-nacl-file-handle-api[2]");
        chromeArguments.add("--use-file-for-fake-audio-capture");
        chromeArguments.add("--allow-external-pages");
        chromeArguments.add("--enable-local-file-accesses");
        chromeArguments.add("--allow-external-pages");
        chromeArguments.add("--ash-enable-touch-view-testing");
        chromeArguments.add("--enable-touch-drag-drop");
        chromeArguments.add("--enable-touchview[7]");
        chromeArguments.add("--disable-extensions-file-access-check");
        return chromeArguments;
    }

    private ChromeOptions setChromeOptions() {
        Map<String, String> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", "Pixel 2");
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("mobileEmulation", mobileEmulation);
        options.addArguments(addChromeArguments());
        options.setAcceptInsecureCerts(true);
        options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.ACCEPT);
        return options;
    }


    public WebDriver getDriver() throws MalformedURLException {
        switch (platform) {
            case ProjectConstants.PLATFORM_DESKTOP:
                if (ProjectConstants.CHROME.equals(browser)) {
                    return instantiateChromeDriver();
                } else if (ProjectConstants.FIREFOX.equals(browser)) {
                    return instantiateFireFoxDriver();
                } else if (ProjectConstants.IE11.equals(browser)) {
                    return instantiateIE11Driver();
                } else {
                    throw new InvalidArgumentException("Invalid browser name ");
                }
            case ProjectConstants.PLATFORM_MOBILITY:
                if (ProjectConstants.PLATFORM_ANDROID.equals(thisRun.getAsString(KEYS.SUB_PLATFORM.name())))
                    return instantiateAndroidDriver();
            case "mobileemulator":
                return instantiateMobileEmulatorDriver();
            case "remotewebdriver":
                return instantiateRemoteWebDriver();
            default:
                throw new InvalidArgumentException("Invalid browser type");
        }
    }


    public void quit(WebDriver driver) {
        driver.quit();
    }
}