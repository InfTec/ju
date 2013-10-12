package ch.inftec.ju.util;

import org.junit.Assert;
import org.junit.Test;

public class SystemPropertyTempSetterTest {
	private static final String PROP = "ju-util.SystemPropertyTempSetterTest";
	
	@Test
	public void canHandleStrings() {
		System.setProperty(PROP, "v1");
		
		try (SystemPropertyTempSetter s = new SystemPropertyTempSetter()) {
			s.setProperty(PROP, "v1b");
			Assert.assertEquals("v1b", System.getProperty(PROP));
		}
		
		Assert.assertEquals("v1", System.getProperty(PROP));
	}
	
	@Test
	public void canHandleNull1() {
		System.clearProperty(PROP);
		
		try (SystemPropertyTempSetter s = new SystemPropertyTempSetter()) {
			s.setProperty(PROP, "v2");
			Assert.assertEquals("v2", System.getProperty(PROP));
		}
		
		Assert.assertNull(System.getProperty(PROP));
	}
	
	@Test
	public void canHandleNull2() {
		System.setProperty(PROP, "v2");
		
		try (SystemPropertyTempSetter s = new SystemPropertyTempSetter()) {
			s.setProperty(PROP, null);
			Assert.assertNull(System.getProperty(PROP));
		}
		
		Assert.assertEquals("v2", System.getProperty(PROP));
	}
}
