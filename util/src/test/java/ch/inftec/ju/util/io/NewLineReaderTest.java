package ch.inftec.ju.util.io;

import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.io.LineIterator;
import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.util.IOUtil;

/**
 * Tests the NewLineReader class.
 * @author Martin
 *
 */
public class NewLineReaderTest {
	@Test
	public void autoDetect() throws Exception {
		String line1 = "I'm the first line";
		String line2 = "And I'm the second";
		
		// No newLine
		
		String inputString = line1;		

		NewLineReader r = new NewLineReader(new StringReader(inputString));
		LineIterator i = new LineIterator(r);
		
		Assert.assertTrue(i.hasNext());
		Assert.assertEquals(i.nextLine(), inputString);
		Assert.assertFalse(i.hasNext());
		Assert.assertNull(r.getInputNewLine());
		Assert.assertNull(r.getOutputNewLine());
		// Test the actual output
		r.reset();
		String out1 = IOUtil.toString(r);
		Assert.assertEquals(inputString, out1);
		
		// \n NewLine
		
		inputString = line1 + "\n" + line2;
		
		r = new NewLineReader(new StringReader(inputString)); 
		i = new LineIterator(r);
		Assert.assertEquals(i.next(), line1);
		Assert.assertEquals(r.getInputNewLine(), "\n");
		Assert.assertEquals(r.getOutputNewLine(), "\n");
		Assert.assertEquals(i.next(), line2);
		Assert.assertFalse(i.hasNext());
		// Test the actual output
		r.reset();
		String out2 = IOUtil.toString(r);
		Assert.assertEquals(inputString, out2);
		
		
		// \r\n NewLine
		
		inputString = line1 + "\r\n" + line2 + "\r\n";
		
		r = new NewLineReader(new StringReader(inputString)); 
		i = new LineIterator(r);
		Assert.assertEquals(i.next(), line1);
		Assert.assertEquals(r.getInputNewLine(), "\r\n");
		Assert.assertEquals(r.getOutputNewLine(), "\r\n");
		Assert.assertEquals(i.next(), line2);
		Assert.assertFalse(i.hasNext()); // Should not return another line (LineIterator behavior)
		// Test the actual output
		r.reset();
		String out3 = IOUtil.toString(r);
		Assert.assertEquals(inputString, out3);
	}
	
	@Test
	public void inputSet() throws IOException {
		String line1 = "I'm the \r first line";
		String line2 = "And I'm the second";
		
		// One character '\n'
		
		String inputString = line1 + "\n" + line2;		

		try (NewLineReader r = new NewLineReader(new StringReader(inputString), "\n", null)) {
			char buf[] = new char[line1.length() + line2.length() + 1];
			r.read(buf, 0, buf.length);
			Assert.assertEquals(line1, new String(buf, 0, line1.length()));
		}		
	}
	
	@Test
	public void outputSet() throws Exception {
		String line1 = "I'm the first line";
		String line2 = "And I'm the second";
		
		// One character '\n'
		
		String inputString = line1 + "\r\n" + line2;		

		NewLineReader r = new NewLineReader(new StringReader(inputString), null, "\n");
		LineIterator i = new LineIterator(r);
		
		Assert.assertEquals(i.nextLine(), line1);
		Assert.assertEquals(r.getInputNewLine(), "\r\n");
		Assert.assertEquals(r.getOutputNewLine(), "\n");
		Assert.assertEquals(i.nextLine(), line2);
		Assert.assertFalse(i.hasNext()); // Should not return another line (LineIterator behavior)
		
		// Test the actual output
		r.reset();
		String out = IOUtil.toString(r);
		Assert.assertEquals(line1 + "\n" + line2, out);
	}
}
