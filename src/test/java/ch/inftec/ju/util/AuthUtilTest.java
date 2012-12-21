package ch.inftec.ju.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test cases for the AuthUtil class. We're using an in-memory Authentication Provider.
 * @author Martin
 *
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthUtilTest {
	@Autowired
	private AuthUtil authUtil;
	
	@Test
	public void authenticate() {
		this.authUtil.logout();
		
		// Try wrong authentication
		try {
			this.authUtil.authenticate("user1", "badPwd");
			Assert.fail("Expected exception");
		} catch (AuthenticationException ex) {
			// Expected
		}
		
		this.authUtil.authenticate("user1", "pwd1");		
	}
	
	@Test
	public void hasRoles() {
		this.authUtil.authenticate("user1", "pwd1");
		
		Assert.assertTrue(this.authUtil.hasAllRoles("ROLE_USER"));
		Assert.assertFalse(this.authUtil.hasAllRoles("ROLE_ADMIN"));
		Assert.assertFalse(this.authUtil.hasAllRoles("ROLE_USER", "ROLE_ADMIN"));
	}
}
