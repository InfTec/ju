package ch.inftec.ju.util;

import org.junit.Assert;
import org.junit.Test;

public class TestUtilsTest {
	@Test
	public void canRunJUnitTests() {
		SuccessfulTest.run = false;
		TestUtils.runJUnitTests(SuccessfulTest.class);
		Assert.assertTrue(SuccessfulTest.run);
	}
	
	@Test(expected=JuRuntimeException.class)
	public void failingTests_fail() {
		TestUtils.runJUnitTests(FailingTest.class);
	}
	
	public static class SuccessfulTest {
		public static boolean run = false;
		
		@Test
		public void setRun_toTrue() {
			run = true;
		}
	}
	
	public static class FailingTest {
		@Test
		public void throw_juRuntimeException() {
			throw new JuRuntimeException("failing");
		}
	}
}
