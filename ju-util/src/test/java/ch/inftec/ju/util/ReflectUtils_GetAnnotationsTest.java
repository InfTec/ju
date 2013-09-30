package ch.inftec.ju.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ReflectUtils_GetAnnotationsTest {
	@Test
	public void canGetAnnotation_forClass() {
		List<Anno> annos = ReflectUtils.getAnnotations(BaseClass.class, Anno.class, false);
		this.assertAnnotations(annos, "BaseClass");
	}
	
	@Test
	public void canGetAnnotation_forExtendingClass_withoutSuperClassAnnotation() {
		List<Anno> annos = ReflectUtils.getAnnotations(ExtendingClass1.class, Anno.class, false);
		this.assertAnnotations(annos, "ExtendingClass");
	}
	
	@Test
	public void canGetAnnotation_forExtendingClass_withSuperClassAnnotation() {
		List<Anno> annos = ReflectUtils.getAnnotations(ExtendingClass1.class, Anno.class, true);
		this.assertAnnotations(annos, "ExtendingClass", "BaseClass");
	}
	
	@Test
	public void extendingClassWithoutAnnotation_returnsNull_withoutBaseClass() {
		List<Anno> annos = ReflectUtils.getAnnotations(ExtendingClass2.class, Anno.class, false);
		this.assertAnnotations(annos);
	}
	
	@Test
	public void extendingClassWithoutAnnotation_returnsBaseAnnotation_withBaseClass() {
		List<Anno> annos = ReflectUtils.getAnnotations(ExtendingClass2.class, Anno.class, true);
		this.assertAnnotations(annos, "BaseClass");
	}
	
	@Test
	public void canGetAnnotation_forMethod() throws Exception {
		List<Anno> annos = ReflectUtils.getAnnotations(BaseClass.class.getMethod("m1", (Class<?>[])null), Anno.class, false, false, false);
		this.assertAnnotations(annos, "BaseClass.m1");
	}
	
	@Test
	public void canGetAnnotation_forMethod_withoutOverriddenMethod() throws Exception {
		List<Anno> annos = ReflectUtils.getAnnotations(ExtendingClass1.class.getMethod("m1", (Class<?>[])null), Anno.class, false, false, false);
		this.assertAnnotations(annos, "ExtendingClass1.m1");
	}
	
	@Test
	public void canGetAnnotation_forMethod_withOverriddenMethod() throws Exception {
		List<Anno> annos = ReflectUtils.getAnnotations(ExtendingClass1.class.getMethod("m1", (Class<?>[])null), Anno.class, true, false, false);
		this.assertAnnotations(annos, "ExtendingClass1.m1", "BaseClass.m1");
	}
	
	@Test
	public void canGetAnnotation_forMethod_withOverriddenMethod_thatIsNotOverriding() throws Exception {
		List<Anno> annos = ReflectUtils.getAnnotations(ExtendingClass2.class.getMethod("m2", (Class<?>[])null), Anno.class, true, false, true);
		this.assertAnnotations(annos, "ExtendingClass2.m2");
	}
	
	@Test
	public void canGetAnnotation_forMethod_includingOverriddenMethods_andClass() throws Exception {
		List<Anno> annos = ReflectUtils.getAnnotations(ExtendingClass1.class.getMethod("m1", (Class<?>[])null), Anno.class, true, true, false);
		this.assertAnnotations(annos, "ExtendingClass1.m1", "BaseClass.m1", "ExtendingClass");
	}
	
	@Test
	public void canGetAnnotation_forMethod_includingOverriddenMethods_andClasses() throws Exception {
		List<Anno> annos = ReflectUtils.getAnnotations(ExtendingClass1.class.getMethod("m1", (Class<?>[])null), Anno.class, true, true, true);
		this.assertAnnotations(annos, "ExtendingClass1.m1", "BaseClass.m1", "ExtendingClass", "BaseClass");
	}
	
	private void assertAnnotations(List<Anno> annotations, String... values) {
		Assert.assertEquals(values.length, annotations.size());
		
		for (int i = 0; i < values.length; i++) {
			Assert.assertEquals(values[i], annotations.get(i).value());
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@interface Anno {
		String value();
	}
	
	@Anno("BaseClass")
	public static class BaseClass {
		@Anno("BaseClass.m1")
		public void m1() {
		}
	}
	
	@Anno("ExtendingClass")
	public static class ExtendingClass1 extends BaseClass {
		@Anno("ExtendingClass1.m1")
		public void m1() {
		}
	}

	// No overriding of Annotation
	public static class ExtendingClass2 extends BaseClass {
		@Override
		public void m1() {
		}
		
		@Anno("ExtendingClass2.m2")
		public void m2() {
		}
	}
}
