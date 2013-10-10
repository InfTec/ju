package ch.inftec.ju.ee.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.ee.test.ContainerTestRunnerRule.TestRunnerType;

/**
 * Base class for test cases that run as (remote) web tests, but need a running container
 * for data loading and web request handling.
 * @author Martin
 *
 */
public class WebContainerTest {
	protected static WebDriver driver;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Rule that performs test setup and verification on the server.
	 */
	@Rule
	public ContainerTestRunnerRule testRunnerRule = new ContainerTestRunnerRule(TestRunnerType.REMOTE_TEST);
	
	@BeforeClass
	public static void initDriver() {
		driver = new HtmlUnitDriver(true); // TODO: Enable to configure driver type
	}
	
	@AfterClass
	public static void closeDriver() {
		driver.quit();
	}
}
