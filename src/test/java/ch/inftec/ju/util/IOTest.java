package ch.inftec.ju.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import junit.framework.Assert;
import junit.framework.ComparisonFailure;

import org.junit.Test;

public class IOTest {
	@Test
	public void getResourceURL() {
		// Non-existing resource
		assertNull(IOUtil.getResourceURL("blibla"));
		
		// Existing resource
		assertNotNull(IOUtil.getResourceURL("testResource.txt", IOTest.class));
		
		// Existing resource using calling-class lookup
		assertNotNull(IOUtil.getResourceURL("testResource.txt"));
	}
	
	@Test
	public void loadTextResource() throws Exception {
		String loadedString = new IOUtil("UTF-8").loadTextResource("testResource.txt");
		
		String expectedString = "First line\n";
		expectedString += "Second line\n";
		expectedString += "äöüéèãâ";
		
		assertEquals(loadedString, expectedString);
	}

	/**
	 * Tests the TestUtils.assertEqualsResource method.
	 */
	@Test
	public void assertEqualsResource() {
		String expectedString = "First line\n";
		expectedString += "Second line\n";
		expectedString += "äöüéèãâ";
		
		// Test assertEqualsResource method
		TestUtils.assertEqualsResource("testResource.txt", expectedString);
		
		// Test assertEqualsResource method with carriage return / line feed
		String expectedStringCrLf = "First line\r\n";
		expectedStringCrLf += "Second line\r\n";
		expectedStringCrLf += "äöüéèãâ";
		
		TestUtils.assertEqualsResource("testResource.txt", expectedStringCrLf);
	}
	
	@Test
	public void loadTextResourceReplacement() throws Exception {
		String loadedString = new IOUtil("UTF-8").loadTextResource("testResource_replacements.txt", IOTest.class, "key1", "###val1###");
		
		String expectedString = "First line ###val1###\n";
		expectedString += "Second %key2% line\n";
		expectedString += "äöüéèãâ";
		
		
		
		assertEquals(loadedString, expectedString);
		
		// Test assertEqualsResource method
		TestUtils.assertEqualsResource("testResource_replacements.txt", expectedString, "key1", "###val1###");
	}
	
	@Test(expected=ComparisonFailure.class)
	public void assertEqualsResource_fail() {
		TestUtils.assertEqualsResource("testResource.txt", "blibla");
	}
	
	@Test
	public void toNewLineUnix() {
		Assert.assertEquals("someString", IOUtil.toNewLineUnix("someString"));
		
		String targetString = "line1\nline2";
		Assert.assertEquals(targetString, IOUtil.toNewLineUnix(targetString));
		Assert.assertEquals(targetString, IOUtil.toNewLineUnix("line1\r\nline2"));
		Assert.assertEquals(targetString, IOUtil.toNewLineUnix("line1\rline2"));
	}
	
	@Test
	public void writeTextToFile() throws Exception {
		String text = "First line\n";
		text += "Second line\n";
		text += "äöüéèãâ";
		
		File tmpFile = IOUtil.getTemporaryFile();
		
		new IOUtil().writeTextToFile(text, tmpFile, true);
		
		String loadedText = new IOUtil().loadTextFromFile(tmpFile);
		
		TestUtils.assertEqualsResource("testResource.txt", loadedText);
	}
}
