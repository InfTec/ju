package ch.inftec.ee.webtest;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import ch.inftec.ju.ee.webtest.WebTest;
import ch.inftec.ju.util.SystemPropertyTempSetter;
import ch.inftec.ju.util.TestUtils;

/**
 * Test web test execution using WebTest base class.
 * <p>
 * We'll use a nested class that extends WebTest so we can simulate the
 * driver selection by overriding system properties.
 * @author Martin
 *
 */
public class WebTestTest {
//	@Test
////	@Ignore
//	public void canOpenGoogle_andSearchForCheese_usingFirefox() {
//		 // Create a new instance of the Firefox driver
//        // Notice that the remainder of the code relies on the interface, 
//        // not the implementation.
//        WebDriver driver = new FirefoxDriver();
//
//        this.googleForCheese(driver);
//	}
	
	@Test
	@Ignore
	public void canOpenGoogle_andSearchForCheese_usingChrome() {
		try (SystemPropertyTempSetter ts = new SystemPropertyTempSetter()) {
			ts.setProperty("ju-testing-ee.selenium.driver", "Chrome");
			
			TestUtils.runJUnitTests(GoogleSeleniumTest.class);
		}		
	}
	
	@Test
	public void canOpenGoogle_andSearchForCheese_usingHtmlUnit() {
		try (SystemPropertyTempSetter ts = new SystemPropertyTempSetter()) {
			ts.setProperty("ju-testing-ee.selenium.driver", "HtmlUnit");
			ts.setProperty("ju-testing-ee.selenium.htmlUnit.enableJavascript", "false");
			
			TestUtils.runJUnitTests(GoogleSeleniumTest.class);
		}		
	}
	
	public static class GoogleSeleniumTest extends WebTest {
		@Test
		public void canGoogle_forCheese() {
			// And now use this to visit Google
	        driver.get("http://www.google.com");
	        // Alternatively the same thing can be done like this
	        // driver.navigate().to("http://www.google.com");

	        // Find the text input element by its name
	        WebElement element = driver.findElement(By.name("q"));

	        // Enter something to search for
	        element.sendKeys("Cheese!"); // 

	        // Now submit the form. WebDriver will find the form for us from the element
	        element.submit();

	        // Check the title of the page
	        logger.info("Page title is: " + driver.getTitle());
	        
	        // Google's search is rendered dynamically with JavaScript.
	        // Wait for the page to load, timeout after 10 seconds
	        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
	            public Boolean apply(WebDriver d) {
	                return d.getTitle().toLowerCase().startsWith("cheese"); //Somehow, Chrome doesn't receive the '!'...
	            }
	        });

	        // Should see: "cheese! - Google Search"
	        logger.info("Page title is: " + driver.getTitle());
		}
	}
}
