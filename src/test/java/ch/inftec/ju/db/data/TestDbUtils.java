package ch.inftec.ju.db.data;

import javax.persistence.EntityManager;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbConnectionFactory;
import ch.inftec.ju.db.DbConnectionFactoryLoader;
import ch.inftec.ju.db.DbQueryRunner;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.db.data.entity.Player;
import ch.inftec.ju.db.data.entity.Team;
import ch.inftec.ju.util.ConversionUtils;

/**
 * Helper class to handle TestDb instances.
 * @author tgdmemae
 *
 */
public final class TestDbUtils {
	private static TestDb derbyTestDb;
	private static TestDb oracleTestDb;
	
	static final int ENTITY_TEAM_COUNT = 5;
	static final int ENTITY_TEAM_PLAYER_COUNT = 5;
	
	/**
	 * Gets an instance of a TestDB backed by a Derby DB. This returns
	 * always the same instance.
	 * <p>
	 * The Derby Test DB is an in memory DB and thus always available.
	 * @return Derby TestDb
	 */
	public static synchronized TestDb getDerbyInMemoryTestDb() {
		if (derbyTestDb == null) derbyTestDb = new DerbyTestDb();
		return derbyTestDb;
	}
	
	/**
	 * Gets an instance of a TestDb backed by an Oracle DB. This returns
	 * always the same instance.
	 * <p>
	 * This connects to the MyTTS database on Swisscom eswdev, so Swisscom
	 * connectivity is required for this DB to work.
	 * @return Oracle TestDb
	 */
	public static synchronized TestDb getOracleMyttsTestDb() {
		if (oracleTestDb == null) oracleTestDb = new OracleTestDb();
		return oracleTestDb;
	}
	
	
	/**
	 * Base class for test databases.
	 * @author tgdmemae
	 *
	 */
	abstract static class AbstractTestDb implements TestDb { 
		private final String dbConnectionName;
		private static DbConnectionFactory dcf = DbConnectionFactoryLoader.createInstance();
		
		/**
		 * Creates a new TestDb instance using the specified DB connection.
		 * @param dbConnectionName Connection name
		 * @throws JuDbException If the tables cannot be created
		 */
		public AbstractTestDb(String dbConnectionName) throws JuDbException {
			this.dbConnectionName = dbConnectionName;
			
			this.createTables();
		}

		@Override
		public final void close() throws JuDbException {		
			this.cleanup();
		}
		
		@Override
		public final DbConnection openDbConnection() {
			return dcf.openDbConnection(dbConnectionName);
		}
		
		/**
		 * Must create (and delete previously if necessary) the needed test tables.
		 * @throws JuDbException If the creation fails
		 */
		protected abstract void createTables() throws JuDbException;
		
		/**
		 * Cleans up any data created in createTables.
		 * @throws JuDbException If cleanup fails
		 */
		protected abstract void cleanup() throws JuDbException;
		
		@Override
		public final void resetData() throws JuDbException {
			try (DbConnection dbConn = this.openDbConnection()) {
				DbQueryRunner qr = dbConn.getQueryRunner();
				
				qr.update("DELETE FROM TEST_A");
				
				qr.update("INSERT INTO TEST_A (AID, TEXT, B_FK) VALUES (1, 'A1', 1)");
				qr.update("INSERT INTO TEST_A (AID, TEXT, B_FK) VALUES (2, 'A2', 2)");
				qr.update("INSERT INTO TEST_A (AID, TEXT, B_FK) VALUES (3, 'A3', 3)");
				
				qr.update("DELETE FROM TEST_B");
				
				qr.update("INSERT INTO TEST_B (BID, TEXT) VALUES (1, 'B1')");
				qr.update("INSERT INTO TEST_B (BID, TEXT) VALUES (2, 'B2')");
				qr.update("INSERT INTO TEST_B (BID, TEXT) VALUES (3, 'B3')");
				
				qr.update("DELETE FROM TEST_C");
				
				qr.update("INSERT INTO TEST_C (CID, TEXT, A_FK, D_FK) VALUES (1, 'A1D1', 1, 1)");
				qr.update("INSERT INTO TEST_C (CID, TEXT, A_FK, D_FK) VALUES (2, 'A1D2', 1, 2)");
				qr.update("INSERT INTO TEST_C (CID, TEXT, A_FK, D_FK) VALUES (3, 'A1D3', 1, 3)");
				qr.update("INSERT INTO TEST_C (CID, TEXT, A_FK, D_FK) VALUES (4, 'A2D1', 2, 1)");
				
				qr.update("DELETE FROM TEST_D");
				
				qr.update("INSERT INTO TEST_D (DID, TEXT) VALUES (1, 'D1')");
				qr.update("INSERT INTO TEST_D (DID, TEXT) VALUES (2, 'D2')");
				qr.update("INSERT INTO TEST_D (DID, TEXT) VALUES (3, 'D3')");
				
				this.resetPlatformSpecificData();
				this.resetEntityData();
			}
		}
		
		/**
		 * Resets the platform specific data that cannot be set by global SQL statements.
		 * @throws JuDbException If the data cannot be set
		 */
		protected abstract void resetPlatformSpecificData() throws JuDbException;
		
		private void resetEntityData() {
			// Create AllStar Player (player that is part of every team)
			Player allstar = new Player();
			allstar.setFirstName("All");
			allstar.setLastName("Star");			
			allstar.setBirthDate(ConversionUtils.newDate(1980, 3, 12));			
			
			try (DbConnection dbConn = this.openDbConnection()) {
				EntityManager em = dbConn.getEntityManager();
				
				em.createQuery("delete from Player").executeUpdate(); // Will automatically clear the relation table as well
				em.createQuery("delete from Team").executeUpdate();
				
				em.persist(allstar);
				
				for (int i = 1; i <= ENTITY_TEAM_COUNT; i++) {
					Team team = new Team();
					em.persist(team);
					
					team.setName("Team" + i);
					team.setRanking(i);
					team.setFoundingDate(ConversionUtils.newDate(2000, i, i));
					
					for (int j = 1; j <= ENTITY_TEAM_PLAYER_COUNT; j++) {
						Player player = new Player();
						em.persist(player);
						player.setFirstName("Player" + j);
						player.setLastName("Team" + i);
						player.setBirthDate(ConversionUtils.newDate(1980, i, j));
						team.getPlayers().add(player);
						player.getTeams().add(team);
					}
					
					team.getPlayers().add(allstar);
					allstar.getTeams().add(team);
				}
			}
		}
	}
}
