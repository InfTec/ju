package ch.inftec.ju.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test case for the WinUtils class. Runs only in Swisscom
 * environment.
 * @author tgdmemae
 *
 */
public class WinUtilsTest_Swisscom {
	@Test
	public void getUserName() {
		Assert.assertEquals("tgdmemae", WinUtils.getUserName());
	}
	
	@Test
	public void getDomainName() {
		Assert.assertEquals("CORPROOT", WinUtils.getDomainName());
	}
}
