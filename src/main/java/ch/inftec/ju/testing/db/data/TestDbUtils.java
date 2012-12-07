package ch.inftec.ju.testing.db.data;

import java.net.URL;

import org.apache.log4j.Logger;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbConnectionFactory;
import ch.inftec.ju.db.DbConnectionFactoryLoader;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.testing.db.DbDataUtil;
import ch.inftec.ju.util.IOUtil;

/**
 * Helper class to handle TestDb instances.
 * @author tgdmemae
 *
 */
public final class TestDbUtils {
	private static final Logger _log = Logger.getLogger(TestDbUtils.class);
	
	private static TestDb derbyTestDb;
	private static TestDb oracleTestDb;
	
	public static final int ENTITY_TEAM_COUNT = 5;
	public static final int ENTITY_TEAM_PLAYER_COUNT = 5;
	
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
		private final String persistenceXmlFileName;
		private final String dbConnectionName;
		
		/**
		 * Creates a new TestDb instance using the specified DB connection.
		 * <p>
		 * Uses the default persistence.xml file.
		 * @param dbConnectionName Connection name
		 * @throws JuDbException If the tables cannot be created
		 */
		public AbstractTestDb(String dbConnectionName) throws JuDbException {
			this(dbConnectionName, "/META-INF/ju-testing_persistence.xml");
		}
		
		/**
		 * Creates a new TestDb instance using the specified DB connection and
		 * persistenceXml file.
		 * @param dbConnectionName Connection name
		 * @param persistenceXmlFileName Persistence.xml file name
		 * @throws JuDbException If the tables cannot be created
		 */
		public AbstractTestDb(String dbConnectionName, String persistenceXmlFileName) {
			this.persistenceXmlFileName = persistenceXmlFileName;
			this.dbConnectionName = dbConnectionName;
			
			this.createTables();			
		}

		@Override
		public final void close() throws JuDbException {		
			this.cleanup();
		}
		
		@Override
		public final DbConnection openDbConnection() {
			DbConnectionFactory factory = DbConnectionFactoryLoader.createInstance(persistenceXmlFileName);
			return factory.openDbConnection(dbConnectionName);
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
		public void clearData() throws JuDbException {
			try (DbConnection dbConn = this.openDbConnection()) {
				// Reset the data
				new DbDataUtil(dbConn).buildImport()
					.from(IOUtil.getResourceURL("/datasets/noData.xml"))
					.executeDeleteAll();				
			}
			
			this.resetPlatformSpecificData();
		}
		
		@Override
		public final void loadTestData(URL testDataFile) throws JuDbException {
			if (testDataFile == null) return;
			
			try (DbConnection dbConn = this.openDbConnection()) {
				_log.debug("Loading data from file: " + testDataFile);
					
				DbDataUtil du = new DbDataUtil(dbConn);
				du.buildImport()
					.from(testDataFile)
					.executeCleanInsert();
			}
		}
		
		/**
		 * Resets the platform specific data that cannot be set by global SQL statements.
		 * <p>
		 * Implementations of TestDb have to make sure that automatically generated
		 * IDs always start from 10.
		 * @throws JuDbException If the data cannot be set
		 */
		protected abstract void resetPlatformSpecificData() throws JuDbException;
	}
}
