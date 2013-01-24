package ch.inftec.ju.util.libs;

import java.io.StringReader;

import junit.framework.Assert;

import org.apache.commons.io.LineIterator;
import org.junit.Test;

/**
 * Test class for the commons-io library.
 * @author Martin
 *
 */
public class IOLibTest {
	@Test
	public void lineIterator() {
		LineIterator i = new LineIterator(new StringReader("Line1\n"));
		
		Assert.assertTrue(i.hasNext());
		Assert.assertEquals(i.next(), "Line1");
		Assert.assertFalse(i.hasNext()); // Doesn't return an empty line
	}
}
