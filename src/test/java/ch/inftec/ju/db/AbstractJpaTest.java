package ch.inftec.ju.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.testing.db.data.entity.Player;
import ch.inftec.ju.testing.db.data.entity.Team;

/**
 * Test class for JPA related tests.
 * @author tgdmemae
 *
 */
public abstract class AbstractJpaTest extends AbstractBaseDbTest {
	/**
	 * Test if the EntityManager returns the same instance for the same object
	 */
	@Test
	public void objectReferenceTest() {
		Player allstar = this.getAllStar(this.em);
		Player allstar2 = this.getAllStar(this.em);
		Assert.assertSame(allstar, allstar2);
		
		try (DbConnection dbConn = this.openDbConnection()) {
			EntityManager otherEm = dbConn.getEntityManager();
			Assert.assertNotSame(otherEm, this.em);
			
			Player allstar3 = this.getAllStar(otherEm);
			Assert.assertNotSame(allstar, allstar3);
		}
	}
	
	/**
	 * Tests how entities behave if they are changed in another instance connected
	 * to another EntityManager (with a separate transaction).
	 */
	@Test
	public void concurrentChanges() {
		Player allstar = this.getAllStar(this.em);
		
		try (DbConnection dbConn = this.openDbConnection()) {
			EntityManager otherEm = dbConn.getEntityManager();
			Player allstar2 = this.getAllStar(otherEm);
			
			allstar2.setFirstName("None");
			otherEm.flush();
			
			Assert.assertEquals("All", allstar.getFirstName());
		}
		
		Assert.assertEquals("All", allstar.getFirstName());
		this.em.refresh(allstar);
		Assert.assertEquals("None", allstar.getFirstName());
	}
	
	/**
	 * Tests how versioning behaves. Team has versioning for optimistic locking
	 * enabled, Player hasn't.
	 */
	@Test
	public void versioning() {
		Player allstar = this.getAllStar(this.em);
		allstar.setFirstName("Change1");
		
		try (DbConnection dbConn = this.openDbConnection()) {
			EntityManager otherEm = dbConn.getEntityManager();
			Player allstar2 = this.getAllStar(otherEm);
			
			allstar2.setFirstName("Change2");
		} // Close will commit
		
		// Flush will work and overwrite changes made by first transaction
		em.flush();
		
		Team team = this.getTeam1(this.em);
		
		team.setName("Change1");
		
		try (DbConnection dbConn = this.openDbConnection()) {
			EntityManager otherEm = dbConn.getEntityManager();
			Team team2 = this.getTeam1(otherEm);
			
			team2.setName("Change2");
		} // Close will commit
		
		try {
			em.flush();
			Assert.fail("Excpected OptimisticLockException");
		} catch (OptimisticLockException ex) {
			// Update with version check failed
		}
	}
	
	/**
	 * Test the execution of Native SQL queries through the EntityManager.
	 */
	@Test
	public void nativeQueries() {
		Query q = this.em.createNativeQuery("select * from TEST_A");
		List<?> l = q.getResultList();
		
		// JPA returns list of Object[] arrays. No information about the column names
		// or types.
		Assert.assertEquals(3, l.size());
		Assert.assertEquals(Object[].class, l.get(0).getClass());
		
		// Test single result and parameter setting. Note: Named parameters are not supported for native queries.
		Query q2 = this.em.createNativeQuery("select aid, text, b_fk from TEST_A where aid=?1");
		q2.setParameter(1, 1);
		Object[] o = (Object[])q2.getSingleResult();
		Assert.assertEquals(o.length, 3);
		Assert.assertEquals(o[1], "A1");
		
		// Test update
		Query q3 = this.em.createNativeQuery("update TEST_A set text=?1 where aid=?2");
		q3.setParameter(1, "NewA1");
		q3.setParameter(2, 1);
		Assert.assertEquals(1, q3.executeUpdate());
		
		Assert.assertEquals("NewA1", this.em.createNativeQuery("select text from TEST_A where aid=1").getSingleResult().toString());
	}
	
	public void objectCopiesAndMerge() {
		Player somePlayer = new Player();
		Assert.assertFalse(this.em.contains(somePlayer));
		
		Player allstar = this.getAllStar(this.em);
		Assert.assertTrue(this.em.contains(allstar));
	}
	
	private Player getAllStar(EntityManager em) {
		return em.createQuery("select p from Player p where p.firstName='All' and p.lastName='Star'", Player.class).getSingleResult();
	}
	
	private Team getTeam1(EntityManager em) {
		return em.createQuery("select t from Team t where t.name='Team1'", Team.class).getSingleResult();
	}
}
