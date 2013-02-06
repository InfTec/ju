package ch.inftec.ju.db;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.eclipse.persistence.sessions.server.ServerSession;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import ch.inftec.ju.testing.db.data.entity.Team;
import ch.inftec.ju.testing.db.data.repo.TeamRepo;
import ch.inftec.ju.testing.db.data.repo.TestingEntityRepo;

/**
 * Test class to see how multiple persistenceUnits and dataSources (including
 * the corresponding transactions) are handled.
 * @author Martin
 *
 */
public class MultiplePersistenceUnitsTest {
	public static class EntityManagerTest {
		@PersistenceContext(unitName="TeamPlayer PU")
		private EntityManager entityManager;
		
		@Autowired
		private TeamRepo teamRepo;
		
		@Transactional // Must be transactional to unwrap session
		public void createDb() {
			ServerSession s = this.entityManager.unwrap(ServerSession.class);
			SchemaManager sm = new SchemaManager(s);
			sm.createDefaultTables(true);
		}
		
		public int teamCount() {
			return (int) this.teamRepo.count();
		}
		
		public String teamName(Long id) {
			return this.teamRepo.findOne(id).getName();
		}
		
		public Long insertTeam(String name, boolean throwException) {
			Long id = this.doInsertTeam(name);
			
			if (throwException) {
				throw new RuntimeException("No Rollback");
			}
			
			return id;
		}
		
		@Transactional
		public Long insertTeamTx(String name, boolean throwException) {
			Long id = this.doInsertTeam(name);
			
			if (throwException) {
				throw new RuntimeException("Rollback");
			}
			
			return id;
		}
		
		private Long doInsertTeam(String name) {
			Team t = new Team();
			t.setName(name);
			this.teamRepo.save(t);
			return t.getId();
		}
	}
	
	public static class EntityManagerTestingEntityTest {
		@PersistenceContext(unitName="TestingEntity PU")
		private EntityManager entityManager;
		
		@Autowired
		private TestingEntityRepo testingEntityRepo;
		
		public int testingEntityCount() {
			return (int) this.testingEntityRepo.count();
		}
	}
	
//	public static class InheritingEntityManagerTest {
//		@PersistenceContext
//		private EntityManager entityManager;
//		
//		public 
//	}
	
	/**
	 * JdbcTemplate tests, using raw SQL
	 * @author Martin
	 *
	 */
	public static class JdbcTemplateTest {
		@PersistenceContext(unitName="TeamPlayer PU")
		private EntityManager entityManager;
		
		@Autowired
		private JdbcTemplate jdbcTemplate;
		
//		@Autowired
//		private JuDbUtils juDbUtils;
		
		public int teamCount() {
			return jdbcTemplate.queryForInt("select count(*) from Team");
		}
		
		public String teamName(Long id) {
			return jdbcTemplate.queryForObject("select name from Team where id=?", String.class, id);
		}
		
		public void insertTeam(Long id, String name, boolean throwException) {
			this.doInsertTeam(id, name);
			
			if (throwException) {
				throw new RuntimeException("No Rollback");
			}
		}
		
		@Transactional
		public void insertTeamTx(Long id, String name, boolean throwException) {
			this.doInsertTeam(id, name);
			
			if (throwException) {
				throw new RuntimeException("Rollback");
			}
		}
		
		private void doInsertTeam(Long id, String name) {
			this.jdbcTemplate.update("insert into Team (id, name) values (?, ?)", id, name);
		}
	}
	
	@Test
	public void multiplePersistenceUnits() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:/ch/inftec/ju/db/MultiplePersistenceUnitsTest-context.xml");
		
		EntityManagerTest entityManagerTest = context.getBean(EntityManagerTest.class);
		entityManagerTest.createDb();
		
		// JDBC tests
		
		JdbcTemplateTest jdbcTemplateTest = context.getBean(JdbcTemplateTest.class);
		
		// Access Team DB (without transaction)
		Assert.assertEquals(0, jdbcTemplateTest.teamCount());
		jdbcTemplateTest.insertTeam(-1L, "Team-1", false);
		Assert.assertEquals(1, jdbcTemplateTest.teamCount());
		
		// Access Team DB (with transaction, with and without rollback)
		try {
			jdbcTemplateTest.insertTeamTx(-2L, "Team-2-rb", true);
		} catch (RuntimeException ex) {
			Assert.assertEquals("Rollback", ex.getMessage());			
		}
		Assert.assertEquals(1, jdbcTemplateTest.teamCount());
		jdbcTemplateTest.insertTeamTx(-2L, "Team-2", false);
		Assert.assertEquals("Team-2", jdbcTemplateTest.teamName(-2L));
		
		// Try access with exception and no transaction
		try {
			jdbcTemplateTest.insertTeam(-3L, "Team-3-noRb", true);
		} catch (RuntimeException ex) {
			Assert.assertEquals("No Rollback", ex.getMessage());
		}
		Assert.assertEquals("Team-3-noRb", jdbcTemplateTest.teamName(-3L));
		
		// EntityManager tests
		Assert.assertEquals("Team-1", entityManagerTest.teamName(-1L));
		Assert.assertEquals(3, entityManagerTest.teamCount());
		
		// Access Team DB (with transaction, with and without rollback)
		try {
			entityManagerTest.insertTeamTx("Team-2-rb", true);
		} catch (RuntimeException ex) {
			Assert.assertEquals("Rollback", ex.getMessage());			
		}
		Assert.assertEquals(3, entityManagerTest.teamCount());
		Long et2 = entityManagerTest.insertTeamTx("Team-2", false);
		Assert.assertEquals("Team-2", jdbcTemplateTest.teamName(et2));
		Assert.assertEquals(4, jdbcTemplateTest.teamCount());
		
		// Try access with exception and no transaction
		try {
			entityManagerTest.insertTeam("Team-3-noRb", true);
		} catch (RuntimeException ex) {
			Assert.assertEquals("No Rollback", ex.getMessage());
		}
		Assert.assertEquals(5, jdbcTemplateTest.teamCount());
		
		// Test different PersistenceUnit
		EntityManagerTestingEntityTest emTeTest = context.getBean(EntityManagerTestingEntityTest.class);
		Assert.assertEquals(0, emTeTest.testingEntityCount());
		
		context.close();
	}
	
}
