package ch.inftec.ju.testing.db.data;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbConnectionFactory;
import ch.inftec.ju.db.DbConnectionFactoryLoader;
import ch.inftec.ju.db.DbQueryRunner;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.testing.db.DbDataUtil;
import ch.inftec.ju.util.IOUtil;

/**
 * Helper class to handle TestDb instances.
 * @author tgdmemae
 *
 */
public final class TestDbUtils {
	final Logger _log = LoggerFactory.getLogger(TestDbUtils.class);
	
	private static TestDb derbyTestDb;
	private static TestDb oracleTestDb;
	
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
	 * Returns a new builder to build a generic TestDb instance.
	 * @param connectionName Name of the connection in the persistene.xml file. The
	 * persistence file can be changed using the persistenceFile method if necessary.
	 * @return TestDbBuilder instance to build the TestDb instance
	 */
	public TestDbBuilder buildTestDb(String connectionName) {
		return new TestDbBuilder(connectionName);
	}
	
	public static class TestDbBuilder {
		private final String connectionName;
		private String persistenceXmlFileName = "/META-INF/ju-testing_persistence.xml";
		private String noDataXmlImportFileName;
		
		private TestDbBuilder(String connectionName) {
			this.connectionName = connectionName;
		}
		
		/**
		 * Uses the specified persistence XML file to load the connection.
		 * <p>
		 * If not set explicitly, the default persistence.xml file will be used
		 * @param persistenceXmlFileName Path to the persistence file name
		 * @return This builder to allow for chaining
		 */
		public TestDbBuilder persistenceFile(String persistenceXmlFileName) {
			this.persistenceXmlFileName = persistenceXmlFileName;
			return this;
		}
		
		/**
		 * Sets a noDataXmlImportFile that will be used to clear data in the
		 * TestDb at the beginning of each test case.
		 * @param noDataXmlImportFileName Path to the noDataXmlImportFile as used by
		 * DbUnit to clear data
		 * @return This builder to allow for chaining
		 */
		public TestDbBuilder noDataXmlImportFile(String noDataXmlImportFileName) {
			this.noDataXmlImportFileName = noDataXmlImportFileName;
			return this;
		}
		
		/**
		 * Creates the TestDb instance as configured by the builder.
		 * @return TestDb instance
		 */
		public TestDb createDerbyDb() {
			// TODO: Add support for non-derby-DBs
			DefaultDerbyTestDb testDb = new DefaultDerbyTestDb(this.connectionName, this.persistenceXmlFileName);
			testDb.setNoDataXmlImportFile(noDataXmlImportFileName);
			
			return testDb;				
		}
	}
	
	private static class DefaultDerbyTestDb extends AbstractTestDb {
		public DefaultDerbyTestDb(String dbConnectionName, String persistenceXmlFileName) {
			super(dbConnectionName, persistenceXmlFileName);
		} 
				
		@Override
		protected void resetPlatformSpecificData() throws JuDbException {
			try (DbConnection dbConn = this.openDbConnection()) {
				DbQueryRunner qr = dbConn.getQueryRunner();

				// Reset sequence to guarantee predictable primary key values
				qr.update("UPDATE SEQUENCE SET SEQ_COUNT=? WHERE SEQ_NAME=?", 9, "SEQ_GEN");
			}
		}
	}
	
	/**
	 * Base class for test databases.
	 * @author tgdmemae
	 *
	 */
	abstract static class AbstractTestDb implements TestDb {
		final Logger log = LoggerFactory.getLogger(AbstractTestDb.class);
		
		private final String persistenceXmlFileName;
		private final String dbConnectionName;
		
		private String noDataXmlImportFile;
		
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

		/**
		 * Extending classes can use this method to set a noDataXmlImportFile.
		 * <p>
		 * If specified, this data will be automatically be imported as deleteAll
		 * at the beginning of the clearData invocation.
		 * <p>
		 * If not set, this implementation will also not call resetPlatformSpecificData.
		 * @param noDataXmlImportFile Path to the noDataXmlImportFile or null if none should be used
		 */
		protected final void setNoDataXmlImportFile(String noDataXmlImportFile) {
			this.noDataXmlImportFile = noDataXmlImportFile;
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
		 * <p>
		 * The default implementation does nothing
		 * @throws JuDbException If the creation fails
		 */
		protected void createTables() throws JuDbException {			
		}
		
		/**
		 * Cleans up any data created in createTables.
		 * <p>
		 * The default implementation does nothing.
		 * @throws JuDbException If cleanup fails
		 */
		protected void cleanup() throws JuDbException {
		}
		
		@Override
		public void clearData() throws JuDbException {
			if (noDataXmlImportFile != null) {
				log.debug("Clearing data using file {} ", noDataXmlImportFile);
				try (DbConnection dbConn = this.openDbConnection()) {
					// Reset the data
					new DbDataUtil(dbConn).buildImport()
						.from(IOUtil.getResourceURL(noDataXmlImportFile))
						.executeDeleteAll();				
				}
				
				this.resetPlatformSpecificData();
			}
		}
		
		@Override
		public final void loadTestData(URL testDataFile) throws JuDbException {
			if (testDataFile == null) return;
			
			try (DbConnection dbConn = this.openDbConnection()) {
				log.debug("Loading data from file: " + testDataFile);
					
				DbDataUtil du = new DbDataUtil(dbConn);
				du.buildImport()
					.from(testDataFile)
					.executeCleanInsert();
				
				// Note: This will have inserted the data using plain JDBC, so we'll need
				// to evict the EntityManager cache to avoid stale data
				dbConn.getEntityManager().getEntityManagerFactory().getCache().evictAll();
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
