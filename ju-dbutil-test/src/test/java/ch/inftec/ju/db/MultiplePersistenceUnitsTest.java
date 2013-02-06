package ch.inftec.ju.db;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;

import org.eclipse.persistence.sessions.server.ServerSession;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ch.inftec.ju.testing.db.data.entity.Team;
import ch.inftec.ju.testing.db.data.repo.TeamRepo;
import ch.inftec.ju.testing.db.data.repo.TestingEntityRepo;
import ch.inftec.ju.util.JuCollectionUtils;

/**
 * Test class to see how multiple persistenceUnits and dataSources (including
 * the corresponding transactions) are handled.
 * <p>
 * Note: Even though one can have multiple PersistenceUnits defined, one need to explicitly
 * define which one to inject EVERY TIME a PersistenceContext is used.
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
		
		public boolean exists(Long id) {
			return this.teamRepo.exists(id);
		}
		
		public boolean exists(String name) {
			return this.teamRepo.getByName(name) != null;
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
		
		@Transactional
		public void tryToSwitchConnectionInfoBetweenTx(ConnectionInfo connectionInfo) {
			this.insertTeam("switchTest1", false);
			ConnectionInfoContextHolder.setConnectionInfo(connectionInfo);
			this.insertTeam("switchTest2", false);
		}
		
		@Transactional
		public void tryToSwitchConnectionInfoBeforeTx(ConnectionInfo connectionInfo) {
			ConnectionInfoContextHolder.setConnectionInfo(connectionInfo);
			this.insertTeam("switchTest3", false);
		}
		
		
		public boolean transactionStatusTest() {
			return TransactionSynchronizationManager.isActualTransactionActive();
//			// Should throw an Exception if we have not transaction
//			TransactionInterceptor.currentTransactionStatus();
		}
		
		@Transactional
		public boolean transactionStatusTestTx() {
			return TransactionSynchronizationManager.isActualTransactionActive();
//			TransactionStatus status = TransactionInterceptor.currentTransactionStatus();
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
		
		@Autowired
		private InheritingEntityManagerTest inheritingEmTest;
		
		public int testingEntityCount() {
			return (int) this.testingEntityRepo.count();
		}
		
		public int inheritedTestingEntityCount() {
			return this.inheritingEmTest.testingEntityCount();
		}
	}
	
	public static class InheritingEntityManagerTest {
		@PersistenceContext(unitName="TestingEntity PU") // PersistenceContext is not "inherited"...
		private EntityManager entityManager;
		
		public int testingEntityCount() {
			return (int) this.entityManager.createQuery("select t from TestingEntity t").getResultList().size();
		} 
	}
	
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
		
		// Test EntityManager inheritance
		Assert.assertEquals(0, emTeTest.inheritedTestingEntityCount());
		
		// Test DataSource switching
		List<ConnectionInfo> availableConnections = JuCollectionUtils.asList(ConnectionInfoContextHolder.getAvailableConnectionInfos());
		ConnectionInfo ci1 = availableConnections.get(0);
		ConnectionInfo ci2 = availableConnections.get(1);
		
		Assert.assertEquals("TeamPlayer DB1", ci1.getName());
		Assert.assertEquals("TeamPlayer DB2", ci2.getName());
		
		// Insert Entity in DB1
		ConnectionInfoContextHolder.setConnectionInfo(ci1);
		Long etDb1a = entityManagerTest.insertTeam("DB1 a", false);
		Assert.assertEquals("DB1 a", entityManagerTest.teamName(etDb1a));
		
		// Switch to DB2 and make sure it isn't there
		ConnectionInfoContextHolder.setConnectionInfo(ci2);
		entityManagerTest.createDb();
		Assert.assertFalse(entityManagerTest.exists(etDb1a));
		
		// Insert Object in DB2
		Long etDb2a = entityManagerTest.insertTeam("DB2 a", false);
		Assert.assertEquals("DB2 a", entityManagerTest.teamName(etDb2a));
		
		// Try to switch DB in a transaction after objects have been inserted...
		entityManagerTest.tryToSwitchConnectionInfoBetweenTx(ci1);
		// As we run the switch within the transaction, the Connection will not be
		// reopened, thus the switch is performed AFTER the transaction ends. All
		// objects are inserted in the ci2
		Assert.assertFalse(entityManagerTest.exists("switchTest2"));
		
		// Check if the objects have been created in ci2
		ConnectionInfoContextHolder.setConnectionInfo(ci2);
		Assert.assertTrue(entityManagerTest.exists("switchTest1"));
		Assert.assertTrue(entityManagerTest.exists("switchTest2"));

		entityManagerTest.tryToSwitchConnectionInfoBeforeTx(ci1);
		// The transaction is started when the method is accessed, so we cannot
		// change the DataSource within a transactional method...
		Assert.assertFalse(entityManagerTest.exists("switchTest3"));
		// Check if the object was created in ci2
		ConnectionInfoContextHolder.setConnectionInfo(ci2);
		Assert.assertTrue(entityManagerTest.exists("switchTest3"));
		
		// Check transaction status
		Assert.assertFalse(entityManagerTest.transactionStatusTest());
		Assert.assertTrue(entityManagerTest.transactionStatusTestTx());
		
		// Try to switch DB in a transaction BEFORE an object has been inserted
		context.close();
	}
	
}