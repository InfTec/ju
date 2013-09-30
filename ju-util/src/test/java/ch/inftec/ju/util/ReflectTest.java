package ch.inftec.ju.util;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Class containing reflection related unit tests.
 * @author tgdmemae
 *
 */
public class ReflectTest {
	@Test
	public void getInnerClass_returnesClass_ifExists() {
		Class<?> innerClass = ReflectUtils.getInnerClass(ReflectTest.class, "InnerClass");
		Assert.assertEquals(InnerClass.class, innerClass);
	}
	
	@Test
	public void getInnerClass_returnesNull_ifNotExists() {
		Class<?> innerClass = ReflectUtils.getInnerClass(ReflectTest.class, "UnknownInnerClass");
		Assert.assertNull(innerClass);
	}
	
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
	
	/**
	 * Tests the getFieldsByAnnotation method.
	 */
	@Test
	public void getDeclaredFieldsByAnnotation() {
		// Try to get declared field that is only inherited
		List<Field> f1 = ReflectUtils.getDeclaredFieldsByAnnotation(AnnoClass.class, Anno1.class);
		Assert.assertEquals(0,  f1.size());
		
		// Get private fields
		List<Field> f2 = ReflectUtils.getDeclaredFieldsByAnnotation(AnnoClass.class, Anno2.class);
		Assert.assertEquals(2, f2.size());
		Assert.assertEquals("privateField1", f2.get(0).getName());
		Assert.assertEquals("privateField2", f2.get(1).getName());
	}
	
	/**
	 * Tests the getDeclaredFieldValueByAnnotation method.
	 */
	@Test
	public void getDeclaredFieldValueByAnnotation() {
		AnnoClass a1 = new AnnoClass();
		
		Assert.assertNull(ReflectUtils.getDeclaredFieldValueByAnnotation(a1, Anno1.class, true));
		Assert.assertEquals(11, ReflectUtils.getDeclaredFieldValueByAnnotation(a1, Anno2.class, true));
	}
	
	protected int getMethodTest(Object o1, Object o2) {
		return 1;
	}
	
	protected int getMethodTest(Long o1, Long o2) {
		return 2;
	}
	
	public static class InnerClass {
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
	
	@Retention(RetentionPolicy.RUNTIME)
	@interface Anno1 {		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@interface Anno2 {		
	}
	
	private static class AnnoBaseClass {
		@Anno1
		protected int inheritedField = 1;
	}
	
	private static class AnnoClass extends AnnoBaseClass {
		@Anno2
		private int privateField2 = 22;
		
		@Anno2
		private int privateField1 = 11;
	}
	
	
}
