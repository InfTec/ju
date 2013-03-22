package ch.inftec.ju.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Class containing String related utility methods.
 * @author tgdmemae
 *
 */
public final class JuStringUtils {
	/**
	 * Don't instantiate.
	 */
	private JuStringUtils() {
		throw new AssertionError("use only statically");
	}
	
	/**
	 * Newline character.
	 */
	public static final String NEW_LINE = "\n";
	
	/**
	 * Line feed character (equals NEW_LINE).
	 */
	public static final String LF = JuStringUtils.NEW_LINE;
	
	/**
	 * Carriage return / line feed combination.
	 */
	public static final String CRLF = "\r\n";
	
	/**
	 * Date of format dd.MM.yyyy.
	 */
	public final static SimpleDateFormat DATE_FORMAT_DAYS = new SimpleDateFormat("dd.MM.yyyy");
	
	/**
	 * Date of format dd.MM.yyyy HH:mm
	 */
	public final static SimpleDateFormat DATE_FORMAT_HOURS = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	
	/**
	 * Date of format dd.MM.yyyy HH:mm:ss
	 */
	public static final SimpleDateFormat DATE_FORMAT_SECONDS = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	/**
	 * Zulu date format, as used by XML: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
	 */
	private static final SimpleDateFormat DATE_FORMAT_ZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	/**
	 * Converts the specified String to a date using the SipleDateFormat provided.<br>
	 * Use the static date formats of the JuStringUtils class.
	 * @param s String
	 * @param dateFormat SimpleDateFormat to use
	 * @return Date
	 * @throws ParseException If the string cannot be parsed
	 */
	public static Date toDate(String s, SimpleDateFormat dateFormat) throws ParseException {
		return dateFormat.parse(s);
	}
	
	/**
	 * Converts the specified Date (in local TimeZone) to a Zulu time string as used
	 * in XML.
	 * @param date Date
	 * @return Zulu date string
	 */
	public static String toZuluDateString(Date date) {
		int offset = TimeZone.getDefault().getOffset(date.getTime());
		
		Date zuluDate = new Date(date.getTime() - offset);
		return JuStringUtils.DATE_FORMAT_ZULU.format(zuluDate);
	}
	
	/**
	 * Crops the String to the length specified. If the String
	 * is shorter, it is just returned.
	 * @param s String
	 * @param maxLength Maximum length of the String
	 * @return Cropped String
	 */
	public static String crop(String s, int maxLength) {
		if (s == null || s.length() <= maxLength) return s;
		return s.substring(0, Math.max(0, maxLength));
	}
	
	/**
	 * Repeats the String s n times and returns that newly built string.
	 * @param s String
	 * @param n Repetitions
	 * @return n times s
	 */
	public static String times(String s, int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) sb.append(s);
		
		return sb.toString();
	}
	
	/**
	 * Replaces all occurrences of %key% as specified in the replacements
	 * parameters (as key, value pairs) with the specified value.
	 * @param s String to be replaced
	 * @param replacements key, value replacement pairs
	 * @return String with replaced keys. If keys in the source string exist that
	 * were not specified in the replacements, they are left untouched
	 */
	public static String replaceAll(String s, String... replacements) {
		if (replacements.length % 2 != 0) {
			throw new IllegalArgumentException("replacements parameter must consist of 0-n key-value pairs");
		}
		
		if (replacements.length == 0 || s == null) return s;
		
		for (int i = 0; i < replacements.length; i += 2) {
			String key = replacements[i];
			String value = replacements[i + 1];
			
			s = s.replaceAll("%" + key + "%", value);
		}
		
		return s;
	}
	
	/**
	 * Counts how many times the substring occurrs in the specified String (without overlapping).
	 * If either string is null, 0 is returned.
	 * @param s String
	 * @param substring Substring
	 * @return Number of occurrences of the substring in the string
	 */
	public static int occurrancies(String s, String substring) {
		if (s == null || substring == null) return 0;
		
		int cnt = 0;
		int index = -1;
		while ((index = s.indexOf(substring, index + 1)) >= 0) {
			cnt++;
		}
		
		return cnt;
	}
	
	/**
	 * Checks if the specified String contains at least one whitespace character. Empty strings
	 * and null are not considered to contain whitespace.
	 * @param s String
	 * @return True if the String contains whitespace
	 */
	public static boolean containsWhitespace(String s) {
		return s != null && s.length() > 0 && new RegexUtil(RegexUtil.WHITESPACE).containsMatch(s);
	}
	
	/**
	 * Creates a toString String for the specified object and the list
	 * of key (String) value (Object) pairs. Uses the ToStringBuilder of the
	 * Apache commons library.
	 * @param obj Object to create toString for
	 * @return String representation of the object, containing key value infos
	 */
	public static String toString(Object obj, Object... keyValuePairs) {
		ToStringBuilder b = new ToStringBuilder(obj, ToStringStyle.SHORT_PREFIX_STYLE);
		
		for (int i = 1; i < keyValuePairs.length; i+= 2) {
			b.append(ObjectUtils.toString(keyValuePairs[i-1]), keyValuePairs[i]);
		}
		
		return b.toString();
	}
	
	/**
	 * Gets the stack trace of the specified Throwable as a String
	 * @param t Throwable
	 * @return Stacktrace
	 */
	public static String getStackTrace(Throwable t) {
		try (StringWriter w = new StringWriter()) {
			PrintWriter pw = new PrintWriter(w);
			t.printStackTrace(pw);
			return w.toString();
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't get stacktrace", ex);
		}
	}
}
