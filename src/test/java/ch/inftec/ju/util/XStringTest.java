package ch.inftec.ju.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Class containing XString related unit tests.
 * @author tgdmemae
 *
 */
public class XStringTest {
	@Test public void constructor() {
		assertEquals(new XString().toString(), "");
		assertEquals(new XString(null).toString(), "");
	}
	
	@Test
	public void indentation() {
		String lineBreak = "\n";
		String indentString = " ";
		
		String expectedString = "Line1" + lineBreak
			+ indentString + "Line2" + lineBreak
			+ indentString + indentString + indentString + "Line3" + lineBreak
			+ indentString + "Line4" + lineBreak
			+ "Line5";
			
		
		XString xs = new XString();
		xs.setLineBreak(lineBreak);
		xs.setIndentString(indentString);
		xs.addLine("Line1");
		xs.increaseIndent();
		xs.addLine("Line2");
		xs.increaseIndent(2);
		xs.addLine("Line3");
		xs.decreaseIndent();
		xs.addLine("Line4");
		xs.decreaseIndent(1);
		xs.addLine("Line5");
		
		assertEquals(expectedString, xs.toString());
		
		// Change indent string and check result
		indentString = "  ";
		xs.setIndentString(indentString);
		
		expectedString = "Line1" + lineBreak
			+ indentString + "Line2" + lineBreak
			+ indentString + indentString + indentString + "Line3" + lineBreak
			+ indentString + "Line4" + lineBreak
			+ "Line5";
		
		assertEquals(expectedString, xs.toString());
		
		// Change line break and check result
		lineBreak = "\r\n";
		xs.setLineBreak(lineBreak);
		
		expectedString = "Line1" + lineBreak
			+ indentString + "Line2" + lineBreak
			+ indentString + indentString + indentString + "Line3" + lineBreak
			+ indentString + "Line4" + lineBreak
			+ "Line5";
		
		assertEquals(expectedString, xs.toString());
	}	
	
	@Test
	public void addText() {
		XString xs = new XString();
		xs.setLineBreak("#");
		xs.addText("bla");
		xs.assertText("bla");
		xs.newLine();
		xs.addText("bli");
		assertEquals(xs.toString(), "bla#bli");
		
		XString xsLists = new XString();
		xsLists.setLineBreak("#");
		xsLists.addLine(7, 8, 9);
		xsLists.addLine(1);
		xsLists.addText(2, 3);
		assertEquals(xsLists.toString(), "789#123");
		
		// Check null value
		XString xsNull = new XString();
		xsNull.addText((Object)null);
		assertEquals(xsNull.toString(), "");
		
		// Make sure replacement value doesn't affect normal text adding
		xsNull.setNullReplacementValue("blabla");
		xsNull.addText((Object)null);
		assertEquals(xsNull.toString(), "");
	}
	
	@Test
	public void comparison() {
		XString xs = new XString("Test");
		assertEquals(xs, "Test");
		assertEquals(xs, new XString("Test"));
		
		assertEquals(new XString(), "");
		
		assertEquals(new XString("Test").hashCode(), new XString("Test").hashCode());
		assertEquals(new XString("Test").hashCode(), "Test".hashCode());
	}
	
	@Test
	public void placeHolders() {
		XString xs = new XString("This is a %test% with % %placeHolders% %but the last isn't");
		assertArrayEquals(xs.getPlaceHolders(), new String[] {"test", "placeHolders"});
		
		xs.addLine("Multi line XString... with a %second% placeholder");
		assertArrayEquals(xs.getPlaceHolders(), new String[] {"test", "placeHolders", "second"});
		
		XString xsExtreme = new XString("%Place holders %with%extreme%placement%%within%the string%");
		assertArrayEquals(xsExtreme.getPlaceHolders(), new String[] {"with", "placement", "within"});
		
		XString xsMultiple = new XString("%Multiple% placeholders %occurring% %Multiple% times %Multiple% %end%");
		assertArrayEquals(xsMultiple.getPlaceHolders(), new String[] {"Multiple", "occurring", "end"});
		
		// Check empty, null and strings without placeholders
		assertArrayEquals(new XString("").getPlaceHolders(), new String[0]);
		assertArrayEquals(new XString(null).getPlaceHolders(), new String[0]);
		assertArrayEquals(new XString("This String has no placeholders").getPlaceHolders(), new String[0]);
		assertArrayEquals(new XString("This String has no placeholders %either").getPlaceHolders(), new String[0]);
						
		// Change place holder character
		XString xsDelimiter = new XString("This String has a /different/ delimiter");
		assertArrayEquals(xsDelimiter.getPlaceHolders(), new String[0]);
		xsDelimiter.setPlaceHolderDelimiter("/");
		assertArrayEquals(xsDelimiter.getPlaceHolders(), new String[] {"different"});
		
		// Use multi character delimiter
		XString xsMultiDeli = new XString("This MULTIString has a MULTIdifferentMULTI delimiterMULTI");
		assertArrayEquals(xsMultiDeli.getPlaceHolders(), new String[0]);
		xsMultiDeli.setPlaceHolderDelimiter("MULTI");
		assertArrayEquals(xsMultiDeli.getPlaceHolders(), new String[] {"different"});
	}
	
	@Test
	public void setPlaceHolder() {
		XString xs = new XString("Hello %something% I'm %name% and I'm %age% years old. %name% I said %-(");
		assertEquals(xs.setPlaceholder("name", "Peter"), 2);
		assertEquals(xs.setPlaceholder("age", "22"), 1);
		assertEquals(xs.setPlaceholder("bla", "bli"), 0);
		
		assertEquals(xs.toString(), "Hello %something% I'm Peter and I'm 22 years old. Peter I said %-(");
		
		// Test null setting
		XString xsNull = new XString("This is a little %null% test");
		assertEquals(xsNull.setPlaceholder("null", null), 1);
		assertEquals(xsNull.toString(), "This is a little  test");
		
		XString xsNull2 = new XString("This is a little %null% test");
		xsNull2.setNullReplacementValue("{nullReplacement}");
		assertEquals(xsNull2.setPlaceholder("null", null), 1);
		assertEquals(xsNull2.toString(), "This is a little {nullReplacement} test");
	}
	
//	@Test
//	public void addEvaluatedText() {
//		XString xsNoEval = new XString();
//		xsNoEval.addText("Hello", null);
//		xsNoEval.addText("%Hello1%a", null);
//		xsNoEval.addText("%Hello2%b", new MultiValueHashtable());
//		assertEquals(xsNoEval.toString(), "Hello%Hello1%a%Hello2%b");
//		
//		MultiValueHashtable evaluator = new MultiValueHashtable();
//		evaluator.addValues("kSimple", "simple");
//		evaluator.addValues("kList", 1L, 2L, 3L);
//		evaluator.addValues("kNull", new Object[] {null});
//		
//		XString xsEval = new XString();
//		xsEval.setNullReplacementValue("{null}");
//		xsEval.addText("Simple %kSimple%", evaluator);
//		xsEval.addText("List %kList%", evaluator);
//		xsEval.addText("Null %kNull%", evaluator);
//		assertEquals(xsEval.toString(), "Simple simpleList 1, 2, 3Null {null}");
//	}
	
	@Test
	public void delimitedItems() {
		XString xs = new XString();
		xs.addText("This is a list: ");
		xs.addItems(", ", "one", "two", 3L);
		
		assertEquals(xs.toString(), "This is a list: one, two, 3");
		
		XString xs2 = new XString("Has line: ");
		xs2.addItems(".", "a", "b");

		assertEquals(xs2.toString(), "Has line: a.b");
		
		// Check null values
		XString xsNull = new XString();
		xsNull.addItems(".", "a", null, "b");
		assertEquals(xsNull.toString(), "a..b");
		
		XString xsNull2 = new XString();
		xsNull2.setNullReplacementValue("{null}");
		xsNull2.addItems(".", "a", null, "b");
		assertEquals(xsNull2.toString(), "a.{null}.b");
	}
	
	@Test
	public void lineCount() {
		XString xs = new XString();
		assertEquals(xs.getLineCount(), 1);
		xs.addText("Little Test");
		assertEquals(xs.getLineCount(), 1);
		xs.addLine("Huhu");
		assertEquals(xs.getLineCount(), 2);
		xs.newLine();
		assertEquals(xs.getLineCount(), 3);
	}
	
	@Test
	public void createEmptyAndClone() throws Exception {
		XString xs = new XString("FirstLine");
		xs.addLine("SecondLine");
		xs.increaseIndent();
		xs.addLine("ThirdLine@p@");
		xs.decreaseIndent();
		xs.addLine("4");
		xs.setIndentString("_");
		xs.setLineBreak("#");
		xs.setPlaceHolderDelimiter("@");
		
		XString xsEmpty = xs.newEmptyInstance();
		assertEquals(xsEmpty.getIndentString(), "_");
		assertEquals(xsEmpty.getLineBreak(), "#");
		assertEquals(xsEmpty.getPlaceHolderDelimiter(), "@");
		assertEquals(xsEmpty.toString(), "");
		
		XString xsClone = xs.newClonedInstance();
		assertEquals(xsClone.getIndentString(), "_");
		assertEquals(xsClone.getLineBreak(), "#");
		assertEquals(xsClone.getPlaceHolderDelimiter(), "@");
		String expectedString = "FirstLine#SecondLine#_ThirdLine";
		assertEquals(xsClone.toString(), expectedString + "@p@#4");
		
		XString xsClone2 = (XString)xs.newClonedInstance();
		assertEquals(xsClone.toString(), xsClone2.toString());
		assertArrayEquals(xsClone2.getPlaceHolders(), new String[] {"p"});
		
		xsClone.setPlaceholder("p", "P1");
		xsClone.addText("XX");
		xsClone.addLine("FourthLine");
		assertEquals(xsClone.toString(), expectedString + "P1#4XX#FourthLine");
		
		xsClone2.addText(""); // Make sure to reset cache
		assertEquals(xsClone2.toString(), expectedString + "@p@#4");
		xsClone2.setPlaceholder("p", "P2");
		xsClone2.addText("YY");
		xsClone2.addLine("FourthLineToo");
		xsClone.addText(""); // Make sure to reset cache
		assertEquals(xsClone.toString(), expectedString + "P1#4XX#FourthLine");
		assertEquals(xsClone2.toString(), expectedString + "P2#4YY#FourthLineToo");
	}
	
	@Test
	public void assertText() {
		XString xs = new XString();
		xs.assertText("Huhu");
		assertEquals(xs.toString(), "Huhu");
		xs.assertText("Huhu");
		assertEquals(xs.toString(), "Huhu");
		xs.addText(" bla");
		xs.assertText(" ");
		xs.addText("bli");
		assertEquals(xs.toString(), "Huhu bla bli");
		
		XString xs2 = new XString();
		xs2.assertEmptyOrText("Huhu");
		assertEquals(xs2.toString(), "");
		xs2.addText("Huhu");
		xs2.assertEmptyOrText("hu");
		xs2.assertEmptyOrText(" ");
		assertEquals(xs2.toString(), "Huhu ");
		
		XString xs3 = new XString("Test (");
		String vals[] = new String[] {"A", "B"};
		for (String val : vals) {
			xs3.assertText("(", ", ");
			xs3.addText(val);
		}
		xs3.addText(")");
		assertEquals(xs3.toString(), "Test (A, B)");		
	}
	
	@Test
	public void addFormatted() {
		XString xs = new XString();
		xs.addFormatted("Hello %s, I am %d years old.", "World", 100);
		
		assertEquals("Hello World, I am 100 years old.", xs.toString());
	}
}
