package ch.inftec.ju.db.auth;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ImportResource;
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
@ContextConfiguration(classes={AuthenticationTest.Configuration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthenticationTest extends AbstractAuthBaseDbTest {
	/*
	 * NOTE: We need to load the XML configuration file in a class Configuration. Loading it
	 * directly with the @ContextConfiguration annotation is not working.
	 */
	@org.springframework.context.annotation.Configuration
	@ImportResource("classpath:ch/inftec/ju/db/auth/AuthenticationTest-context.xml")
	static class Configuration {		
	}
	
	@Autowired
	private JuUserDetailsService service;
	
	@Test
	public void authRepositoryTest() {
		this.loadDataSet("/datasets/auth/singleUser.xml");
		
		AuthUserRepo userRepo = JuDbUtils.getJpaRepository(this.em, AuthUserRepo.class);
		Assert.assertNotNull(userRepo);
		Assert.assertTrue(userRepo.exists(1L));
		
		AuthRoleRepo roleRepo = JuDbUtils.getJpaRepository(this.em, AuthRoleRepo.class);
		Assert.assertEquals(1L, roleRepo.getByNameAndUsersId("role1", 1L).getId().longValue());
		Assert.assertNull(roleRepo.getByNameAndUsersId("unassignedRole", 1L));
	}
	
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
