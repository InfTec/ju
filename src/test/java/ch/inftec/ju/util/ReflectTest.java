package ch.inftec.ju.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

/**
 * Class containing reflection related unit tests.
 * @author tgdmemae
 *
 */
public class ReflectTest {
	@Test
	public void getCallingClass() {
		new CalledClass().callMe();		
	}
	
	private class CalledClass {
		public void callMe() {
			assertEquals(ReflectUtils.getCallingClass(), ReflectTest.class);
		}
	}
	
	@Test
	public void getDeclaredMethod() throws Exception {
		Method m1 = ReflectUtils.getDeclaredMethod(ReflectTest.class, "getMethodTest", new Class<?>[] { Long.class, Long.class });
		int res1 = (Integer)m1.invoke(new ReflectTest(), null, null);
		assertEquals(res1, 2);
		
		Method m2 = ReflectUtils.getDeclaredMethod(ReflectTest.class, "getMethodTest", new Class<?>[] { String.class, Long.class });
		int res2 = (Integer)m2.invoke(new ReflectTest(), null, null);
		assertEquals(res2, 1);
		
	}
	
	/**
	 * Test retrieval of static field values.
	 * <P>
	 * Note that a public static field in a private static class cannot be accessed...
	 */
	@Test
	public void getStaticFieldValue() {
		// private field
		Assert.assertEquals("testValue", ReflectUtils.getStaticFieldValue(StaticFieldClass1.class, "test", null));
		// public field
		Assert.assertEquals("test2Value", ReflectUtils.getStaticFieldValue(StaticFieldClass2.class, "test2", null));
		
		// null
		Assert.assertEquals("def", ReflectUtils.getStaticFieldValue(StaticFieldClass2.class, null, "def"));
		Assert.assertNull(ReflectUtils.getStaticFieldValue(StaticFieldClass2.class, "test", null));
		Assert.assertEquals("def", ReflectUtils.getStaticFieldValue(StaticFieldClass2.class, "test3", "def"));
		
		try {
			ReflectUtils.getStaticFieldValue(StaticFieldClass1.class, "notAccessible", null);
			Assert.fail("Expected access to fail");			
		} catch (JuRuntimeException ex) {
			Assert.assertEquals(IllegalAccessException.class, ex.getCause().getClass());
		}
	}
	
	/**
	 * Tests the newInstance method on public and private classes.
	 */
	@Test
	public void newInstance() {
		Assert.assertEquals(StaticFieldClass1.class, ReflectUtils.newInstance(StaticFieldClass1.class, true).getClass());
		Assert.assertEquals(StaticFieldClass2.class, ReflectUtils.newInstance(StaticFieldClass2.class, false).getClass());
		
		// Without forcing
		try {
			ReflectUtils.newInstance(StaticFieldClass1.class, false);
			Assert.fail("Shouldn't be able to invoke private constructor");
		} catch (JuRuntimeException ex) {
			Assert.assertEquals(IllegalAccessException.class, ex.getCause().getClass());
		}
	}
	
	protected int getMethodTest(Object o1, Object o2) {
		return 1;
	}
	
	protected int getMethodTest(Long o1, Long o2) {
		return 2;
	}
	
	@SuppressWarnings("unused")
	private static class StaticFieldClass1 {
		private static String test = "testValue";
		public static String notAccessible;
	}
	
	public static class StaticFieldClass2 {
		public static String test2 = "test2Value";
		public static String test3 = null;
	}
}
