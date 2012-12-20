package ch.inftec.ju.db;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.db.auth.AbstractAuthBaseDbTest;
import ch.inftec.ju.db.auth.repo.AuthRoleRepo;
import ch.inftec.ju.db.auth.repo.AuthUserRepo;

/**
 * Tests for the JuDbUtils utility class.
 * @author Martin
 *
 */
public class JuDbUtilsTest extends AbstractAuthBaseDbTest {
	/**
	 * Tests the lookup of a Spring JPA repository.
	 */
	@Test
	public void getJpaRepository() {
		this.loadDataSet("/datasets/auth/singleUser.xml");
		
		AuthUserRepo userRepo = JuDbUtils.getJpaRepository(this.em, AuthUserRepo.class);
		Assert.assertNotNull(userRepo);
		Assert.assertTrue(userRepo.exists(1L));
		Assert.assertFalse(this.em.getTransaction().getRollbackOnly());
		
		AuthRoleRepo roleRepo = JuDbUtils.getJpaRepository(this.em, AuthRoleRepo.class);
		Assert.assertEquals(1L, roleRepo.getByNameAndUsersId("role1", 1L).getId().longValue());
		Assert.assertNull(roleRepo.getByNameAndUsersId("unassignedRole", 1L));
	}
}
