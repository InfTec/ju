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
	/**
	 * Tests the user and domain name lookup.
	 */
	@Test
	public void userAndDomainName() {
		Assert.assertEquals("tgdmemae", WinUtils.getUserName());
		Assert.assertEquals("CORPROOT", WinUtils.getDomainName());
	}
}
