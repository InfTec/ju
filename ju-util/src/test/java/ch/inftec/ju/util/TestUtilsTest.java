package ch.inftec.ju.util;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

public class TestUtilsTest {
	/**
	 * Maven will find the nested class and execute the test, so we need a flag
	 * to make sure it fails only when it should.
	 */
	private static boolean doTestFail = false;
	
	@Test
	public void canRunJUnitTests() {
		SuccessfulTest.run = false;
		TestUtils.runJUnitTests(SuccessfulTest.class);
		Assert.assertTrue(SuccessfulTest.run);
	}
	
	@Test(expected=JuRuntimeException.class)
	public void failingTests_fail() {
		try {
			doTestFail = true;
			TestUtils.runJUnitTests(FailingTest.class);
		} finally {
			doTestFail = false;
		}
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
			Assume.assumeTrue(doTestFail);
			throw new JuRuntimeException("failing");
		}
	}
}
