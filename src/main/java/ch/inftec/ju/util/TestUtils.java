package ch.inftec.ju.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import junit.framework.Assert;

import org.w3c.dom.Document;

import ch.inftec.ju.util.comparison.EqualityTester;
import ch.inftec.ju.util.xml.XmlUtils;

/**
 * Class containing test related utility methods.
 * @author tgdmemae
 *
 */
public final class TestUtils {
	/**
	 * Don't instantiate.
	 */
	private TestUtils() {
		throw new AssertionError("use only statically");
	}
	
	/**
	 * Runs internal tests as defined in a static (recommendedly protected) class method 'internalTests'.
	 * If the class doesn't have a static method internalTests, an exception is thrown.
	 * @param c Class
	 * @param params Array of 0-n parameters to pass to the internalTests method. If a parameter is
	 * null, it is supposed to be of type Object.
	 */
	public static void runInternalTests(Class<?> c, Object... params) {
		Method m = null;
		
		try {
			Class<?>[] paramTypes = ReflectUtils.getTypes(params);
			m = ReflectUtils.getDeclaredMethod(c, "internalTests", paramTypes);
			m.setAccessible(true);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Couldn't get static method 'internalTests' for class " + c.getName(), ex);
		}
		
		try {
			m.invoke(null, params);
		} catch (Exception ex) {
			throw new RuntimeException("internalTests raised exception for class " + c.getName(), ex);
		}
	}
	
	/**
	 * Asserts that the specified String equals the string defined in the text resource. Expects the resource
	 * to be in UTF-8 encoding.
	 * <p>
	 * This method will convert both the resource and the string to using line endings \n, so
	 * different line endings will be ignored.
	 * @param s String to be compared
	 * @param resourceName Name of the resource that has to be in the same package as the calling class
	 * @param replacements key, value replacement pairs. Key in the file has to be surrounded by percentage signs, e.g. %key%
	 */
	public static void assertEqualsResource(String resourceName, String s, String... replacements) {
		try {
			String resString = new IOUtil("UTF-8").loadTextResource(resourceName, ReflectUtils.getCallingClass(), replacements);
			String sUnix = IOUtil.toNewLineUnix(s);
			
			Assert.assertEquals(resString, sUnix);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Resource not found: " + resourceName, ex);
		}
	}
	
	/**
	 * Asserts that two maps are equal. If they are not, Assert.fail is called.
	 * Uses the specified EqualityTester instance to check for equality of the
	 * values.
	 * @param mExpected Expected Map
	 * @param mActual Actual Map
	 * @param equalityTester EqualityTester instance to be used to test for equality. If null,
	 * a default tester will be used that uses the Object.equals method
	 */
	public static <K, V> void assertMapEquals(Map<K, V> mExpected, Map<K, V> mActual, EqualityTester<V> equalityTester) {
		if (!JuCollectionUtils.mapEquals(mExpected, mActual, equalityTester)) {
			Assert.fail("Maps are not equal. Expected: " + mExpected + "; Actual: " + mActual);
		}
	}
	
	/**
	 * Asserts that two maps are equal. If they are not, Assert.fail is called.
	 * @param mExpected Expected Map
	 * @param mActual Actual Map
	 */
	public static <K, V> void assertMapEquals(Map<K, V> mExpected, Map<K, V> mActual) {
		TestUtils.assertMapEquals(mExpected, mActual, null);
	}
	
	/**
	 * Asserts that two arrays are equal. If they are not, Assert.fail is called.
	 * Uses CollectionUtil.arrayEquals to perform the test.
	 * @param aExpected Expected Array
	 * @param aActual Actual Array
	 */
	public static void assertArrayEquals(Object[] aExpected, Object[] aActual) {
		if (!JuCollectionUtils.arrayEquals(aExpected, aActual)) {
			Assert.fail("Arrays are not equal. Expected: " + aExpected + "; Actual: " + aActual);
		}
	}
	
	/**
	 * Asserts that two collections are equal. If they are not, Assert.fail is called.
	 * Uses CollectionUtil.collectionEquals to perform the test.
	 * @param cExpected Expected Collection
	 * @param cActual Actual Collection
	 */
	public static <T> void assertCollectionEquals(Collection<T> cExpected, Collection<T> cActual) {
		if (!JuCollectionUtils.collectionEquals(cExpected, cActual)) {
			Assert.fail("Collections are not equal. Expected: " + cExpected + "; Actual: " + cActual);
		}
	}
	
	/**
	 * Convenience method to assert collection using variable parameter list.
	 * @param cActual Actual collection
	 * @param expectedObjects List of expected elements
	 */
	@SafeVarargs
	public static <T> void assertCollectionEquals(Collection<T> cActual, T... expectedObjects) {
		TestUtils.assertCollectionEquals(Arrays.asList(expectedObjects), cActual);
	}
	
	/**
	 * Asserts that a String matches an expected pattern. If it doesn't, Assert.fail is called.
	 * @param expectedPattern Expected regular expression pattern
	 * @param actualString Actual String
	 */
	public static void assertRegexEquals(String expectedPattern, String actualString) {
		if (!new RegexUtil(expectedPattern).matches(actualString)) {
			Assert.fail("String doesn't match pattern. Expected: " + expectedPattern + "; Actual: " + actualString);
		}
	}
	
	/**
	 * Asserts that two XMLs are equal, ignoring whitespace and formatting.
	 * @param expectedDocument Expected document
	 * @param actualDocument Actual document
	 */
	public static void assertEqualsXml(Document expectedDocument, Document actualDocument) {
		String expectedXml = XmlUtils.toString(expectedDocument, false, false);
		String actualXml = XmlUtils.toString(actualDocument, false, false);
		
		Assert.assertEquals(expectedXml, actualXml);
	}
}
