package ch.inftec.ju.util;

import java.io.StringReader;
import java.util.ArrayList;

import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

import ch.inftec.ju.util.io.NewLineReader;

/**
 * Helper class providing different String functionality like indented multi lines.
 * <br>
 * The default indent string is two blanks and the default line break is a simple line feed.
 * <p>
 * Note that an XString never returns null but an empty String when converted to a String.
 * @author tgdmemae
 *
 */
public class XString {
	private ArrayList<Line> lines = new ArrayList<Line>();
	
	private String s;
	
	private String lineBreak = "\n";
	private String indentString = "  ";
	private String placeHolderDelimiter = "%";
	private String nullReplacementValue = "";
	
	private int indentLevel = 0;
	private int lastIndentIncrease = 0;
	
	/**
	 * Creates an empty XString.
	 */
	public XString() {
	}
	
	/**
	 * Creates an XString with one line.
	 * @param s Initial line
	 */
	public XString(String s) {
		this.addLine(s);
	}
	
	/**
	 * Creates a new XString from the specified String, creating a new line after each
	 * line break
	 * <p>
	 * null and empty String both result in an XString with one empty line.
	 * @param s String to parse into a multi-line XString
	 * @return XString
	 */
	public static XString parseLines(String s) {
		if (s == null || s.isEmpty()) {
			return new XString("");
		} else {
			XString xs = new XString();
			try (NewLineReader r = new NewLineReader(new StringReader(s))) {
				LineIterator i = r.iterateLines();
				while (i.hasNext()) {
					xs.addLine(i.nextLine());
				}
				// Use the new line strategy of the input, provided we got some.
				// Otherwise we'll just leave the XString's default setting, which
				// is \n (NEW_LINE)
				if (r.getInputNewLine() != null) {
					xs.setLineBreak(r.getInputNewLine());
				}
				return xs;
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't parse String", ex);
			}
		}
	}
	
	/**
	 * Creates a new empty XString that has the same attributes as this XString,
	 * but doesn't contain any text.
	 * @return Empty XString instance containing the same attributes as this XString 
	 */
	public XString newEmptyInstance() {
		XString xs = new XString();
		xs.setLineBreak(this.getLineBreak());
		xs.setIndentString(this.getIndentString());
		xs.setPlaceHolderDelimiter(this.getPlaceHolderDelimiter());
		xs.setNullReplacementValue(this.getNullReplacementValue());
		
		
		return xs;
	}
	
	/**
	 * Creates a new XString instance with the same attributes as this instance and with
	 * the specified text.
	 * @param text Text
	 * @return New XString instance with the same attributes as this XString and the specified text
	 */
	public XString newInstance(String text) {
		XString xs = this.newEmptyInstance();
		xs.addText(text);
		return xs;
	}
	
	/**
	 * Creates a clone of the current XString. This deep copies all fields
	 * of the XString so they will be completely independent.
	 * @return Deep copy clone of the XString
	 */
	public XString newClonedInstance() {
		XString xs = this.newEmptyInstance();
		xs.s = this.s;
		xs.lines = new ArrayList<Line>();
		
		// We have to deep copy all lines as they might contain place holder information that
		// could get changed later and would affect both strings if we only created
		// a shallow copy.
		
		for (int i = 0; i < this.lines.size(); i++) {
			Line l = this.lines.get(i);
			xs.lines.add(new Line(l.getString(), l.getIndentation()));
		}
		
		return xs;
	}
	
	/**
	 * Adds a line to this indent string. If the string is null, an
	 * empty line is added.
	 * @param line Line
	 */
	public void addLine(String line) {
		this.lines.add(new Line(line == null ? "" : line, this.indentLevel));
		
		this.flagChanged();
	}
	
	/**
	 * Adds a line to this indent string using the String.format method.
	 * @param format Format string
	 * @param args arguments to String.format
	 */
	public void addLineFormatted(String format, Object... args) {
		this.addLine(String.format(format, args));
	}
	
	/**
	 * Adds the object(s) to the indent string. If the object is null, an empty
	 * string is added. Otherwise the String returned by the object's toString
	 * method is added. The objects are concatenated without any character between
	 * them.
	 * @param objs Object(s)
	 */
	public void addLine(Object... objs) {
		this.addLine((String)null);
		for (Object obj : objs) {
			this.addText(obj);
		}
	}
	
	/**
	 * Adds an empty line to the string.
	 */
	public void newLine() {
		this.addLine((String)null);
	}
	
	/**
	 * Gets the line at the specified position as a String.
	 * @param index Line index
	 * @return Line as String
	 */
	public String getLine(int index) {
		if (this.lines.size() <= index) {
			throw new IndexOutOfBoundsException(String.format(
					"Line has only %d lines, cannot access line %d", this.lines.size(), index));
		}
		
		return this.lines.get(index).toString();
	}
	
	/**
	 * Gets the last line of the String. If no line exists, a new one is created.
	 * @return Last line of the String
	 */
	protected Line getLastLine() {
		if (this.lines.size() == 0) this.addLine((String)null);
		return this.lines.get(this.lines.size() - 1);
	}
	
	/**
	 * Adds some text to the last line. If there are no lines, a new one is created.
	 * If the text is null, nothing is added.
	 * @param text
	 */
	public void addText(String text) {
		this.getLastLine().addText(text);
		this.flagChanged();
	}
	
	/**
	 * Converts the specified Object(s) to a String and adds it to the XString.
	 * If a specified Object is null, an empty string is used.
	 * <br>
	 * The object strings are concatenated without a character in between.
	 * @param objs Object(s) to be added to the text
	 */
	public void addText(Object... objs) {
		for (Object obj : objs) {
			this.addText(obj == null ? "" : obj.toString());
		}
	}
	
	/**
	 * Adds text to the String, using the String.format method.
	 * @param format Format string
	 * @param args Argument for the String.format method
	 */
	public void addFormatted(String format, Object... args) {
		this.addText(String.format(format, args));
	}
	
//	/**
//	 * Adds some text to the last line, using the specified evaluator to replace any
//	 * placeholders in the text with the value(s) specified by the evaluator. If a placeholder
//	 * cannot be evaluated through the Evaluator, is is left untouched. 
//	 * @param text Text to be added
//	 * @param evaluator Evaluator used to evaluate any placeholders in the text
//	 */
//	public void addText(String text, Evaluator evaluator) {
//		XString xs = this.createNew(text);
//		
//		if (evaluator != null) {
//			for (String placeholder : xs.getPlaceHolders()) {
//				Object res[] = evaluator.evaluate(placeholder, null);
//				
//				if (res.length > 0) {
//					XString xsRes = this.createEmpty();
//					xsRes.addItems(", ", res);			
//					xs.setPlaceholder(placeholder, xsRes.toString());
//				}
//			}
//		}
//		
//		this.addText(xs.toString());
//	}
	
	/**
	 * Makes sure that the current (last) line ends with the specified text. The method supports an
	 * array of Strings. If will loop through the array and check if the line ends with any of the
	 * Strings. If none matches, the last of the Strings is added to the line.
	 * @param texts Array of text the line must end with. If it ends with none, the last is added
	 */
	public void assertText(String... texts) {
		if (ArrayUtils.isEmpty(texts)) return;
		
		Line l = this.getLastLine();
		
		for (String text : texts) {
			if (l.getString().endsWith(text)) return;
		}
		
		this.addText(texts[texts.length-1]);
	}
	
	/**
	 * Makes sure that the current (last) line is either empty or contains the specified text.
	 * If not, the text is added to the current line.
	 * @param text Text the line must end with if it's not empty
	 */
	public void assertEmptyOrText(String text) {
		Line l = this.getLastLine();
		if (l.getString().length() > 0) this.assertText(text);
	}
	
	/**
	 * Adds the specified items, separated by the specified delimiter.
	 * @param delimiter Delimiter to separate items
	 * @param items Items to be added. If an item is null, the NullReplacementValue
	 * is added.
	 */
	public void addItems(String delimiter, Object... items) {
		boolean firstItem = true;
		for (Object item : items) {
			if (firstItem) firstItem = false;
			else this.addText(delimiter);
			
			this.addText(this.toString(item));
		}
	}
	
	/**
	 * Increases the indent level by 1.
	 */
	public void increaseIndent() {
		this.increaseIndent(1);
	}
	
	/**
	 * Increases the indent level by the specified levels.
	 * @param levels Levels
	 */
	public void increaseIndent(int levels) {
		this.indentLevel += levels;
		this.lastIndentIncrease = levels;
	}
	
	/**
	 * Decreases the indent level by the amount of the last
	 * increase.
	 */
	public void decreaseIndent() {
		this.indentLevel -= this.lastIndentIncrease;
	}
	
	/**
	 * Decreases the indent level by the specified levels.
	 * @param levels Levels
	 */
	public void decreaseIndent(int levels) {
		this.indentLevel -= levels;
		if (this.indentLevel < 0) throw new IllegalStateException("Indent level may not fall below 0");
	}
	
	/**
	 * Gets the line break String used.
	 * The line break string is added before a new line to separate it from the following.
	 * @return Line break string
	 */
	public String getLineBreak() {
		return this.lineBreak;
	}
	
	/**
	 * Sets the line break String to be used.
	 * The line break string is added before a new line to separate it from the following.
	 * @param lineBreak Line break
	 */
	public void setLineBreak(String lineBreak) {
		this.lineBreak = lineBreak;
		this.flagChanged();
	}
	
	/**
	 * Gets the indent string used. The indent string is multiplied
	 * by the indent level of the line and added before each line.
	 * @return Indent string
	 */
	public String getIndentString() {
		return this.indentString;
	}
	
	/**
	 * Sets the indent string to be used. The indent string is multiplied
	 * by the indent level of the line and added before each line.
	 * @param indentString Indent string
	 */
	public void setIndentString(String indentString) {
		this.indentString = indentString;
		this.flagChanged();
	}
	
	/**
	 * Gets the place holder delimiter. The place holder delimiter is used by the place holder
	 * methods to determine place holders within the string. A place holder name is
	 * identified by a string starting and ending with the place holder delimiter and
	 * containing no whitespace in between, e.g. %placeHolder% for the delimiter '%'
	 * @return Place holder delimiter used by this XString
	 */
	public String getPlaceHolderDelimiter() {
		return this.placeHolderDelimiter;
	}
		
	/**
	 * Sets the place holder delimiter to be used by the place holder related methods.
	 * @param s Place holder delimiter
	 */
	public void setPlaceHolderDelimiter(String s) {
		this.placeHolderDelimiter = s;
	}
	
	/**
	 * Gets the null replacement value, i.e. the value that is used in the replacement methods
	 * when the specified value is null.
	 * @return Null replacement value
	 */
	public String getNullReplacementValue() {
		return this.nullReplacementValue;
	}
	
	/**
	 * Sets the null replacement value, i.e. the value that is used in the replacement methods
	 * when the specified value is null.
	 * @param nullReplacementValue Null replacement value
	 */
	public void setNullReplacementValue(String nullReplacementValue) {
		this.nullReplacementValue = nullReplacementValue;
	}
	
	/**
	 * Gets all placeholders within the string. If the same placeholder occurrs multiple times, it
	 * is only returned once (at the order of first occurrence).
	 * @return Placeholders within the string
	 */
	public String[] getPlaceHolders() {
		ArrayList<String> placeHolders = new ArrayList<String>();
		String s = this.toString();
		int plLength = this.getPlaceHolderDelimiter().length();
		
		int startIndex = s.indexOf(this.getPlaceHolderDelimiter());
		
		while (startIndex >= 0 && startIndex < s.length() - plLength) {
			int endIndex = s.indexOf(this.getPlaceHolderDelimiter(), startIndex + plLength);
			
			if (endIndex > startIndex + plLength) {
				String placeHolder = s.substring(startIndex + plLength, endIndex);
				if (JuStringUtils.containsWhitespace(placeHolder)) {
					startIndex = endIndex;
				} else {
					// Add the placeholder (if it hasn't occurred yet)
					if (!placeHolders.contains(placeHolder)) placeHolders.add(placeHolder);
					startIndex = s.indexOf(this.getPlaceHolderDelimiter(), endIndex + plLength);
				}
			} else if (endIndex > 0) {
				startIndex = endIndex + plLength;
			} else {
				break;
			}
		}

		return (String[])placeHolders.toArray(new String[0]);
	}
	
	/**
	 * Replaces the place holder with the specified value and returns the number of
	 * replaced occurrences.
	 * @param placeHolder Place holder to be replaced
	 * @param value Value to replace with
	 * @return Count of replacements (0 if none)
	 */
	public int setPlaceholder(String placeHolder, String value) {
		if (placeHolder == null || placeHolder.length() < 1 || JuStringUtils.containsWhitespace(placeHolder)) {
			throw new IllegalArgumentException("Placeholder must contain at least one character and must not contain whitespace: " + placeHolder);
		}
		
		String replacementString = this.getPlaceHolderDelimiter() + placeHolder + this.getPlaceHolderDelimiter();
		
		int cnt = 0;
		for (Line line : this.lines) {
			String s = line.getString();
			int lCnt = JuStringUtils.occurrancies(s, replacementString);
			if (lCnt > 0) {
				cnt += lCnt;
				line.setString(s.replaceAll(replacementString, (this.toString(value))));
				this.flagChanged();
			}
		}
		
		return cnt;
	}
	
	/**
	 * Flags the string as changed, i.e. it will be rebuilt the next time it is requested.
	 */
	private void flagChanged() {
		this.s = null;
	}
	
	@Override
	public String toString() {
		if (this.s == null) {
			StringBuilder sb = new StringBuilder();
			boolean firstLine = true;
			for (Line line : this.lines) {
				if (!firstLine) sb.append(this.lineBreak);
				
				sb.append(JuStringUtils.times(this.indentString, line.getIndentation()));
				sb.append(line.getString());
				
				firstLine = false;
			}
			
			this.s = sb.toString();
		}
		
		return this.s;
	}
	
	/**
	 * Gets the number of lines in the XString.
	 * @return Number of lines in the XString
	 */
	public int getLineCount() {
		return Math.max(1, this.lines.size());
	}
	
	/**
	 * Gets the length of the longest line of this XString.
	 * @return Length in characters
	 */
	public int getLongestLineLength() {
		int maxSize = 0;
		for (Line l : this.lines) {
			maxSize = Math.max(maxSize, l.getString().length());
		}
		return maxSize;
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		
		String s = null;
		if (obj instanceof XString) {
			s = ((XString)obj).toString();
		} else if (obj instanceof String) {
			s = (String)obj;
		} else {
			return false;
		}
		
		return ObjectUtils.equals(this.toString(), s);
	}
	
	/**
	 * Converts the specified Object to a String. If the object is null,
	 * the XString's NullReplacementValue is returned, otherwise the
	 * object's toString method is used to convert it to a String.
	 * @param obj Object to convert to a String
	 * @return Object.toString or XString.nullReplacementValue if the object is null
	 */
	private String toString(Object obj) {
		return obj == null ? this.getNullReplacementValue() : obj.toString();
	}
	
	/**
	 * Helper class that represents a line (with indentation).
	 * @author tgdmemae
	 *
	 */
	private class Line {
		/**
		 * StringBuilder used to improve performance when using the addText functionality. In this case, we will
		 * initialize a new StringBuilder with the string of the line and set the string to null.
		 */
		StringBuilder sb;
		String s;
		private int indentation;
		
		public Line(String s, int indentation) {
			this.s = s;
			this.indentation = indentation;
		}
		
		public String getString() {
			return this.s == null ? this.sb.toString() : this.s;
		}
		
		public void setString(String s) {
			this.s = s;
			this.sb = null;
		}
		
		public void addText(String s) {
			if (this.sb == null) {
				this.sb = new StringBuilder(this.s);
				this.s = null;
			}

			this.sb.append(s == null ? "" : s);
		}
		
		public int getIndentation() {
			return this.indentation;
		}
		
		@Override
		public String toString() {
			return this.getString();
		}
	}
}
