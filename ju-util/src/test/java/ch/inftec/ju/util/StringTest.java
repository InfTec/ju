package ch.inftec.ju.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Class containing string related unit tests.
 * @author tgdmemae
 *
 */
public class StringTest {
	@Test
	public void cropString() {
		assertEquals(JuStringUtils.crop("1234567890", 5), "12345");
		assertEquals(JuStringUtils.crop("1234567890", 10), "1234567890");
		assertEquals(JuStringUtils.crop("1234567890", 11), "1234567890");
		assertEquals(JuStringUtils.crop("1234567890", 0), "");
		assertEquals(JuStringUtils.crop("1234567890", -1), "");
	}
	
	@Test
	public void replaceAll() {
		String testString = "bli %key1%, bla %key2%, blu %key1%";
		
		assertNull(JuStringUtils.replaceAll(null));
		assertEquals(JuStringUtils.replaceAll(testString), testString);
		assertEquals(JuStringUtils.replaceAll(testString, "blablaKey", "bli"), testString);
		assertEquals(JuStringUtils.replaceAll(testString, "key1", "val1"), "bli val1, bla %key2%, blu val1");
		assertEquals(JuStringUtils.replaceAll(testString, "key1", "val1", "key2", "val2"), "bli val1, bla val2, blu val1");
	}
	
	@Test
	public void occurrancies() {
		String testString = "abbcccddddblabla";
		
		assertEquals(JuStringUtils.occurrancies(testString, "bb"), 1);
		assertEquals(JuStringUtils.occurrancies(testString, "a"), 3);
		assertEquals(JuStringUtils.occurrancies(testString, "bla"), 2);
		
		assertEquals(JuStringUtils.occurrancies(null, "bla"), 0);
		assertEquals(JuStringUtils.occurrancies("bla", null), 0);
		assertEquals(JuStringUtils.occurrancies(null, null), 0);		
	}
	
	@Test
	public void containsWhitespace() {
		assertFalse(JuStringUtils.containsWhitespace("ThisHasNoWhiteSpace"));
		assertFalse(JuStringUtils.containsWhitespace(""));
		assertFalse(JuStringUtils.containsWhitespace(null));
		assertTrue(JuStringUtils.containsWhitespace(" "));
		assertTrue(JuStringUtils.containsWhitespace("some whitespace"));
		assertTrue(JuStringUtils.containsWhitespace("LineBreak\nShouldBeWhitespace"));
	}
	
	@Test
	public void toStringTest() {
		String str = JuStringUtils.toString(this, "one", 1, "two", 2);
		assertEquals(this.getClass().getSimpleName() + "[one=1,two=2]", str);
	}
	
	/**
	 * Test the conversion to Zulu time (as used in XMLs)
	 */
	@Test
	public void zuluTime() throws Exception {
		Calendar cal = new GregorianCalendar();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.set(1980, Calendar.DECEMBER, 03, 12, 01, 02);
		cal.set(Calendar.MILLISECOND, 0);
		
		String zuluTime = JuStringUtils.toZuluDateString(cal.getTime());
		Assert.assertEquals("1980-12-03T12:01:02.000Z", zuluTime);
	}
	
	@Test
	public void loremIpsumParagraphs() {
		TestUtils.assertEqualsResource("StringTest_loremIpsumParagraphs.txt", JuStringUtils.createLoremIpsum().getParagraphs());
	}
	
	@Test
	public void loremIpsumWords() {
		TestUtils.assertEqualsResource("StringTest_loremIpsumWords.txt", JuStringUtils.createLoremIpsum().getWords(15));
	}
}
