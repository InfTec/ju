package ch.inftec.ju.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test class for JuBeanUtils.
 * @author tgdmemae
 *
 */
public class JuBeanUtilsTest {
	@Test
	public void checkFieldsNotNull() {
		TestClass tc = new TestClass();
		
		try {
			JuBeanUtils.checkFieldsNotNull(tc, "s1", "s2");
		} catch (IllegalStateException ex) {
			Assert.assertTrue(ex.getMessage().contains("s1"));
			Assert.assertTrue(ex.getMessage().contains("s2"));
		}
		
		tc.s1 = "bli";
		tc.s2 = "bla";
		JuBeanUtils.checkFieldsNotNull(tc, "s1", "s2");

		try {
			JuBeanUtils.checkFieldsNotNull(tc, "s1", "s2", "sList");
		} catch (IllegalStateException ex) {
			Assert.assertTrue(ex.getMessage().contains("sList"));
		}
		
		tc.sList = new ArrayList<>();
		
		JuBeanUtils.checkFieldsNotNull(tc, "s1", "s2", "sList");
	}
	
	public static class TestClass {
		private String s1;
		protected String s2;
		public List<String> sList;
		
		public String getS1() {return s1;};
	}
}
