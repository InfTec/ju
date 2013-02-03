package ch.inftec.ju.testing.db.data;

import java.net.URL;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import ch.inftec.ju.db.ConnectionInfo;
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
	
//	/**
//	 * Returns a new builder to build a generic TestDb instance.
//	 * @param connectionName Name of the connection in the persistene.xml file. The
//	 * persistence file can be changed using the persistenceFile method if necessary.
//	 * @return TestDbBuilder instance to build the TestDb instance
//	 */
//	public TestDbBuilder buildTestDb(String connectionName) {
//		return new TestDbBuilder(connectionName);
//	}
	
//	public static class TestDbBuilder {
//		private String noDataXmlImportFileName;
//		
//		/**
//		 * Sets a noDataXmlImportFile that will be used to clear data in the
//		 * TestDb at the beginning of each test case.
//		 * @param noDataXmlImportFileName Path to the noDataXmlImportFile as used by
//		 * DbUnit to clear data
//		 * @return This builder to allow for chaining
//		 */
//		public TestDbBuilder noDataXmlImportFile(String noDataXmlImportFileName) {
//			this.noDataXmlImportFileName = noDataXmlImportFileName;
//			return this;
//		}
//		
//		/**
//		 * Creates the TestDb instance as configured by the builder.
//		 * @return TestDb instance
//		 */
//		public TestDb createDerbyDb() {
//			// TODO: Add support for non-derby-DBs
//			DefaultDerbyTestDb testDb = new DefaultDerbyTestDb();
//			testDb.setNoDataXmlImportFile(noDataXmlImportFileName);
//			
//			return testDb;				
//		}
//	}
	
	private static class DefaultDerbyTestDb extends AbstractTestDb {
		@Override
		protected void resetPlatformSpecificData() throws JuDbException {
			// Reset sequence to guarantee predictable primary key values
			this.jdbcTemplate.update("UPDATE SEQUENCE SET SEQ_COUNT=? WHERE SEQ_NAME=?", 9, "SEQ_GEN");
		}
	}
	
	/**
	 * Base class for test databases.
	 * @author tgdmemae
	 *
	 */
	abstract static class AbstractTestDb implements TestDb {
		final Logger log = LoggerFactory.getLogger(AbstractTestDb.class);
		
		@PersistenceContext
		private EntityManager em;
		
		@Autowired
		protected JdbcTemplate jdbcTemplate;
		
		@Autowired
		private ConnectionInfo connectionInfo;
		
		@Autowired
		private DataSource dataSource;
		
		private String noDataXmlImportFile;

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
		public void resetDatabase() throws JuDbException {
			// Derby seems to rollback table creation as well
			// TODO: Refactor for Oracle...
			this.createTables();
			this.resetPlatformSpecificData();
			
//			if (noDataXmlImportFile != null) {
//				log.debug("Clearing data using file {} ", noDataXmlImportFile);
//					// Reset the data
//				new DbDataUtil(DataSourceUtils.getConnection(this.dataSource), this.connectionInfo).buildImport()
//					.from(IOUtil.getResourceURL(noDataXmlImportFile))
//					.executeDeleteAll();				
//				
//				this.resetPlatformSpecificData();
//			}
		}
		
//		@Override
//		public final void loadTestData(URL testDataFile) throws JuDbException {
//			if (testDataFile == null) return;
//			
//			log.debug("Loading data from file: " + testDataFile);
//				
//			DbDataUtil du = new DbDataUtil(DataSourceUtils.getConnection(this.dataSource), this.connectionInfo);
//			du.buildImport()
//				.from(testDataFile)
//				.executeCleanInsert();
//			
//			// Note: This will have inserted the data using plain JDBC, so we'll need
//			// to evict the EntityManager cache to avoid stale data
//			this.em.getEntityManagerFactory().getCache().evictAll();
//		}
		
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
