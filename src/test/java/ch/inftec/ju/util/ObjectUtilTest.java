package ch.inftec.ju.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for ObjectUtil methods.
 * @author TGDMEMAE
 *
 */
public class ObjectUtilTest {
	@Test
	public void getIdentityString() {
		Assert.assertEquals(JuObjectUtils.getIdentityString(null), "null");
		
		Object o1 = new Object();
		Object o2 = o1;
		Assert.assertTrue(JuObjectUtils.getIdentityString(o1).length() > 0);
		Assert.assertEquals(JuObjectUtils.getIdentityString(o1), JuObjectUtils.getIdentityString(o2));
		
		Object o3 = new Object();
		Assert.assertFalse(JuObjectUtils.getIdentityString(o1).equals(JuObjectUtils.getIdentityString(o3)));
	}
}
