package ch.inftec.ju.ee.webtest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.JuUtils;
import ch.inftec.ju.util.PropertyChain;

/**
 * Base class for test cases that run selenium web tests.
 * <p>
 * See ju-testing-ee_default.properties on how to configure tests.
 * @author Martin
 *
 */
public class WebTest {
	protected static WebDriver driver;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String PROP_DRIVER = "ju-testing-ee.selenium.driver";
	
	@BeforeClass
	public static void initDriver() {
		AssertUtil.assertNull("Driver hasn't been closed", driver);
		
		PropertyChain pc = JuUtils.getJuPropertyChain();
		
		String driverType = pc.get(PROP_DRIVER);
		
		LoggerFactory.getLogger(WebTest.class).debug("Creating driver: " + driverType);
		if ("HtmlUnit".equals(driverType)) {
			driver = new HtmlUnitDriver(true);
		} else if ("Chrome".equals(driverType)) {
			System.setProperty("webdriver.chrome.driver", pc.get("ju-testing-ee.selenium.chrome.driver"));
	        driver = new ChromeDriver();
		} else {
			throw new JuRuntimeException(String.format("Unsupported selenium driver type: %s. Check value of property %s"
					, driverType
					, PROP_DRIVER));
		}
	}
	
	@AfterClass
	public static void closeDriver() {
		driver.quit();
		driver = null;
	}
}
