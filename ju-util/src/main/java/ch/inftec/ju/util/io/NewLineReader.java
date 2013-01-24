package ch.inftec.ju.util.io;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;

/**
 * Reader that is aware of new line characters, i.e. that will try to auto detect what kind of NewLine
 * the file uses (\r\n, \r or \n).
 * <p>
 * Initially, the newLine property will be set to null. If left to null, the Reader will set it to the
 * value of the NewLine string as soon as it encounters the first new line. After that, all following
 * lines are treated accordingly.
 * <p>
 * If set manually, the reader will treat the new lines accordingly to the value set. E.g. if the
 * newLine string ist set manually to '\n' and the Reader encounters a '\r\n', the '\r' will be
 * part of the string returned.
 * @author Martin
 *
 */
public class NewLineReader extends Reader {	
	public static String LF = "\n";
	public static String CRLF = "\r\n";
	
	/**
	 * Input new line String. 
	 */
	private String inputNewLine = null;
	
	/**
	 * Output new line String.
	 */
	private String outputNewLine = null;
	
	/**
	 * Buffer for characters that have been read from the underlying reader.
	 * This will already contain the converted output NewLines (if different
	 * from the input NewLines).
	 */
	private Queue<Character> queue = new LinkedList<Character>();
	
	/**
	 * Reader the NewLineReader works with.
	 */
	private Reader reader;
	
	/**
	 * Creates a new NewLineReader, using the same input and output NewLine
	 * Strings as used in the character stream.
	 * @param reader Reader to read character stream from
	 */
	public NewLineReader(Reader reader) {
		this(reader, null, null);
	}
	
	/**
	 * Creates a new NewLineReader using the specified input and output NewLine
	 * Strings.
	 * @param reader Reader to read character stream from
	 * @param inputNewLine Input NewLine String. If null, it is auto detected from the first
	 * occurrence of a newLine String in the stream. If not null, it must be either 1 or
	 * two characters.
	 * @param outputNewLine Output NewLine String. If null, the same NewLine String as
	 * the input NewLine String is used
	 */
	public NewLineReader(Reader reader, String inputNewLine, String outputNewLine) {
		this.reader = reader;
		
		if (StringUtils.length(inputNewLine) > 2 || StringUtils.length(outputNewLine) > 2) {
			throw new IllegalArgumentException("Input and Output NewLine characters can not be longer than 2 characters.");
		}
		if (inputNewLine != null && inputNewLine.length() == 0) {
			throw new IllegalArgumentException("Input NewLine may not be empty String");
		}
		
		this.inputNewLine = inputNewLine;
		this.outputNewLine = outputNewLine;
	}
	
	/**
	 * Gets the Input NewLine character used or detected by this Reader. If none
	 * has been specified and the Reader hasn't encountered any, null is returned.
	 * @return Input NewLine String or null if not known
	 */
	public String getInputNewLine() {
		return this.inputNewLine;
	}
	
	/**
	 * Gets the Output NewLine character used by this Reader. If none has been
	 * specified and the Reader hasn't encountered any, null is returned.
	 * @return Output NewLine String or null if not known
	 */
	public String getOutputNewLine() {
		return this.outputNewLine;
	}
	
	@Override
	public void close() throws IOException {
		this.reader.close();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		this.doRead(len);
		
		if (this.queue.size() == 0) return -1;
		
		int i = off;
		int lastIndex = off + len - 1;
		
		while (i <= lastIndex && this.queue.size() > 0) {
			cbuf[i] = this.queue.poll().charValue();
			i++;
		}
		
		return i-off;
	}
	
	/**
	 * Makes sure the queue holds the the specified amount of characters. If the stream
	 * doesn't provide enough characters, as much as possible are read.
	 * @param len Characters to be read into the queue. If the stream ends
	 * before, we'll read as many characters as possible.
	 * @throws IOException If the next character cannot be read
	 */
	private void doRead(int len) throws IOException {
		while (this.queue.size() < len) {
			if (!this.readFirstChar()) return;
		}
	}
	
	/**
	 * Reads a character from the underlying reader. If the character could be part
	 * of a two character new line sequence, another character will be read. The
	 * character(s) will be added to the reader's queue.
	 * @return False if the end of the reader was reached, true otherwise
	 * @throws IOException
	 */
	private boolean readFirstChar() throws IOException {
		int nextChInt = this.reader.read();
		if (nextChInt == -1) return false;
		
		char nextCh = (char)nextChInt;
		
		if (this.inputNewLine != null && this.inputNewLine.charAt(0) == nextCh) {
			if (this.inputNewLine.length() == 1) {
				this.addNewLine();
			} else {
				return this.readSecondChar(nextCh);
			}
		} else if (this.inputNewLine == null && nextCh == '\n') {
			this.inputNewLine = "\n";
			this.addNewLine();
		} else if (this.inputNewLine == null && nextCh == '\r') {
			return this.readSecondChar(nextCh);
		} else {
			this.queue.add(nextCh);
		}
		
		return true;
	}
	
	/**
	 * Reads the second character of a possible NewLine combination from the reader.
	 * The characters will be added to the reader's queue
	 * @param firstCh First character
	 * @return False if the end of the reader was reached, true otherwise
	 * @throws IOException If the character canot be read
	 */
	private boolean readSecondChar(char firstCh) throws IOException {
		// Try to read second character.
		int nextChInt = this.reader.read();
		if (nextChInt == -1) {
			if (this.inputNewLine == null && firstCh == '\r' || firstCh == '\n') {
				// First char IS NewLine
				this.inputNewLine = Character.toString(firstCh);
				this.addNewLine();
			} else {
				// No NewLine found
				this.queue.add(firstCh);
			}
			
			return false;
		}
		
		// Next character was not null
		char nextCh = (char)nextChInt;
		
		if (this.inputNewLine != null) {
			if (this.inputNewLine.charAt(1) == nextCh) {
				this.addNewLine();
			} else {
				this.queue.add(firstCh);
				this.queue.add(nextCh);
			}
		} else {
			// First character must have been '\r'
			if (nextCh == '\n') {
				this.inputNewLine = "\r\n";
				this.addNewLine();
			} else {
				this.inputNewLine = "\r";
				this.addNewLine();
				this.queue.add(nextCh);
			}
		}
		
		return true;
	}
	
	/**
	 * Adds a new (output) NewLine to the queue.
	 */
	private void addNewLine() {
		if (this.outputNewLine == null) this.outputNewLine = this.inputNewLine;
		
		for (int i = 0; i < this.outputNewLine.length(); i++) this.queue.add(this.outputNewLine.charAt(i));
	}
	
	@Override
	public void reset() throws IOException {
		this.reader.reset();
		this.queue.clear();
	}
}
