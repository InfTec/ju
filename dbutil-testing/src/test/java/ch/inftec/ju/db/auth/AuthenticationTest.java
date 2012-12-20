package ch.inftec.ju.db.auth;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.inftec.ju.db.JuDbUtils;
import ch.inftec.ju.db.auth.repo.AuthRoleRepo;
import ch.inftec.ju.db.auth.repo.AuthUserRepo;

/**
 * Contains tests for the Authentication functionality.
 * @author Martin
 *
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthenticationTest extends AbstractAuthBaseDbTest {
	@Autowired
	private JuUserDetailsService service;
	
	@Test
	public void juUserDetailsService() {
		this.loadDataSet("/datasets/auth/singleUser.xml");
		
		// Load existing user
		UserDetails userDetails1 = this.service.loadUserByUsername("user1");
		Assert.assertEquals("user1", userDetails1.getUsername());
		Assert.assertEquals(1, userDetails1.getAuthorities().size());
		Assert.assertEquals("role1", userDetails1.getAuthorities().iterator().next().getAuthority());
		
		// Load new user
		UserDetails userDetails2 = this.service.loadUserByUsername("user2");
		Assert.assertEquals(1, userDetails2.getAuthorities().size());
		Assert.assertEquals("NEW_ROLE", userDetails2.getAuthorities().iterator().next().getAuthority());
		
		// Check if the data has been stored to the DB
		this.reInitConnection(true);
		
		AuthUserRepo userRepo = JuDbUtils.getJpaRepository(this.em, AuthUserRepo.class);
		AuthRoleRepo roleRepo = JuDbUtils.getJpaRepository(this.em, AuthRoleRepo.class);
		Assert.assertNotNull(userRepo.getByName("user2"));
		Assert.assertNotNull(roleRepo.getByName("NEW_ROLE"));		
	}
}
