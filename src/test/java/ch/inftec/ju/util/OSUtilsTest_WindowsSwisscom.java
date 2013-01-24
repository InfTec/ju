package ch.inftec.ju.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test case for the WinUtils class. Runs only in Swisscom
 * environment.
 * @author tgdmemae
 *
 */
public class OSUtilsTest_WindowsSwisscom {
	@Test
	public void getUserName() {
		Assert.assertEquals("tgdmemae", OSUtils.getUserName());
	}
	
	@Test
	public void getDomainName() {
		Assert.assertEquals("CORPROOT", OSUtils.getDomainName());
	}
}
