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
	
	private void assertAnnotations(List<Anno> annotations, String... values) {
		Assert.assertEquals(annotations.size(), values.length);
		
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
	}
	
	@Anno("ExtendingClass")
	public static class ExtendingClass1 extends BaseClass {
	}

	// No overriding of Annotation
	public static class ExtendingClass2 extends BaseClass {
	}
}
