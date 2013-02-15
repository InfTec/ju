package ch.inftec.ju.util.libs;

import junit.framework.Assert;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.junit.Test;

/**
 * Tests for the Yoda Time framework.
 * @author Martin
 *
 */
public class YodaTimeTest {
	@Test
	public void core() {
		LocalDate lt1 = new LocalDate(2000, 1, 1);
		String s1 = lt1.toString();
		Assert.assertEquals("2000-01-01", s1);
		
		DateTimeFormatter fmt = new DateTimeFormatterBuilder()
			.appendLiteral("Year: ")
			.appendYear(4, 4)
			.toFormatter();// minDigits, maxDigits
		Assert.assertEquals("Year: 2000", lt1.toString(fmt));
	}
}
