package ch.inftec.ju.util.libs;


import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class JUnitTest {
	@Test
	public void failedAssume_shouldIgnoreTest() {
		Assume.assumeTrue(false);
		Assert.assertTrue(false);
	}
	
	@Test(expected=RuntimeException.class)
	public void expectedException_shouldPassTest() {
		throw new RuntimeException("Test");
	}
}
