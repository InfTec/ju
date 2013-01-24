package ch.inftec.ju.util;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Util class containing conversion functions.
 * @author tgdmemae
 *
 */
public final class ConversionUtils {
	/**
	 * Don't instantiate.
	 */
	private ConversionUtils() {
		throw new AssertionError("use only statically");
	}
	
	/**
	 * Converts an Object to a BigDecimal (if possible) or returns null if this
	 * is not possible.
	 * @param o Object to convert to BigDecimal.
	 * @return BigDecimal or null if value cannot be converted
	 */
	public static BigDecimal toBigDecimal(Object o) {
		if (o == null) return null;
		
		if (o instanceof Integer) return new BigDecimal((Integer)o);
		if (o instanceof Long) return new BigDecimal((Long)o);
		if (o instanceof Float) return new BigDecimal((Float)o);
		if (o instanceof Double) return new BigDecimal((Double)o);
		if (o instanceof BigDecimal) return (BigDecimal)o;
		
		return null;
	}
	
	/**
	 * Creates a new Date with the specified year, month and day. Uses
	 * a GregorianCalendar to create the Date instance.
	 * @param year Year
	 * @param month Month
	 * @param day Day
	 * @return Date instance
	 */
	public static Date newDate(int year, int month, int day) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(year, month, day);
		
		return cal.getTime();
	}
}
