package ch.inftec.ju.util.libs;

import junit.framework.Assert;

import org.junit.Assume;
import org.junit.Test;

public class JUnitTest {
	@Test
	public void failedAssume_shouldIgnoreTest() {
		Assume.assumeTrue(false);
		Assert.assertTrue(false);
	}
}
