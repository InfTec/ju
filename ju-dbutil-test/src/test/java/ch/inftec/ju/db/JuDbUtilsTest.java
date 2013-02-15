package ch.inftec.ju.db;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.springtestdbunit.annotation.DatabaseSetup;

import ch.inftec.ju.testing.db.AbstractBaseDbTest;
import ch.inftec.ju.testing.db.DefaultContextAbstractBaseDbTest;
import ch.inftec.ju.testing.db.data.repo.TestingEntityRepo;

/**
 * Tests for the JuDbUtils utility class.
 * @author Martin
 *
 */
public class JuDbUtilsTest extends DefaultContextAbstractBaseDbTest {
	@Autowired
	private TestingEntityRepo testingEntityRepo;
	
	/**
	 * Tests the lookup of a Spring JPA repository.
	 */
	@DatabaseSetup("/datasets/singleTestingEntityData.xml")
	@Test
	public void getJpaRepository() {
		Assert.assertNotNull(this.testingEntityRepo);
		Assert.assertTrue(this.testingEntityRepo.exists(1L));		
		
		// There was a problem using dynamic queries where EclipseLink wouldn't create
		// the associated NamedQuery and set the transaction to rollback.
		Assert.assertEquals(1L, this.testingEntityRepo.getByName("Test1").getId().longValue());
	}
}
