package ch.inftec.ju.db;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.testing.db.AbstractBaseDbTest;
import ch.inftec.ju.testing.db.data.repo.TestingEntityRepo;

/**
 * Tests for the JuDbUtils utility class.
 * @author Martin
 *
 */
public class JuDbUtilsTest extends AbstractBaseDbTest {
	/**
	 * Tests the lookup of a Spring JPA repository.
	 */
	@Test
	public void getJpaRepository() {
		this.loadDataSet(DefaultDataSet.SINGLE_TESTING_ENTITY);
		
		TestingEntityRepo testingEntityRepo = JuDbUtils.getJpaRepository(this.em, TestingEntityRepo.class);
		Assert.assertNotNull(testingEntityRepo);
		Assert.assertTrue(testingEntityRepo.exists(1L));		
		
		// There was a problem using dynamic queries where EclipseLink wouldn't create
		// the associated NamedQuery and set the transaction to rollback.
		Assert.assertEquals(1L, testingEntityRepo.getByName("Test1").getId().longValue());
		Assert.assertFalse(this.em.getTransaction().getRollbackOnly());
	}
}
