package ch.inftec.ju.util;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class to make assertions, much like the JUnit Assert class.
 * <p>
 * In contrast to the Assert class, the AssertUtil doesn't throw AssertionErrors
 * but JuRuntimeExceptions. This way, they will be caught by the common
 * catch-clauses.
 * @author tgdmemae
 *
 */
public class AssertUtil {
	public static void assertEquals(Object expected, Object actual) {
		AssertUtil.assertEquals(String.format("Objects not equal. Expected %s, got %s", expected, actual), actual);
	}
	
	public static void assertEquals(String message, Object expected, Object actual) {
		if (!ObjectUtils.equals(expected, actual)) {
			throw new JuRuntimeException(message);
		}
	}
	
	public static void assertNull(Object obj) {
		AssertUtil.assertNull("Object was not null", obj);
	}
	
	public static void assertNull(String message, Object obj) {
		if (obj != null) throw new JuRuntimeException(message);
	}
	
	public static void assertNotNull(Object obj) {
		AssertUtil.assertNotNull("Object was null", obj);
	}
	
	public static void assertNotNull(String message, Object obj) {
		if (obj == null) throw new JuRuntimeException(message);
	}
	
	public static void assertNotEmpty(String s) {
		AssertUtil.assertNotEmpty("String was empty", s);
	}
	
	public static void assertNotEmpty(String message, String s) {
		if (StringUtils.isEmpty(s)) throw new JuRuntimeException(message);
	}
	
	public static void fail(String message) {
		throw new JuRuntimeException(message);
	}
	
	public static void assertFalse(boolean bool) {
		AssertUtil.assertFalse("Expected false", bool);
	}
	
	public static void assertFalse(String message, boolean bool) {
		if (bool) throw new JuRuntimeException(message);
	}
	
	public static void assertTrue(boolean bool) {
		AssertUtil.assertTrue("Expected true", bool);
	}
	
	public static void assertTrue(String message, boolean bool) {
		if (!bool) throw new JuRuntimeException(message);
	}
	
	public static void assertCount(int expectedCount, int actualCount) {
		AssertUtil.assertCount("Object", expectedCount, actualCount);
	}
	
	public static void assertCount(String objectName, int expectedCount, int actualCount) {
		if (expectedCount != actualCount) {
			throw new JuRuntimeException(String.format("Expected exactly %d object%s '%s', but got %d",
					expectedCount,
					(expectedCount == 1 ? "" : "s"),
					objectName,
					actualCount));
		}
	}
}

