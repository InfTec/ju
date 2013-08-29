package ch.inftec.ju.testing.db;

/**
 * Provider that get information about a testing DB.
 * <p>
 * Can be used with AbstractDbTest to specify test DBs at runtime:<br>
 * Supply the name of the TestDbProvider implementation in a file called
 * META-INF/testDbProvider.impl
 * @author Martin
 *
 */
public interface TestDbProvider {
	/**
	 * Gets the TestDbInfo for the specified persistenceUnitName.
	 * @param persistenceUnitName PersistenceUnitName
	 * @return TestDbInfo
	 */
	TestDbInfo getTestDbInfo(String persistenceUnitName);
	
	/**
	 * Helper object containing information on how to connect to and how to handle the test DB
	 * @author Martin
	 *
	 */
	public class TestDbInfo {
		private final String connectionUrl;
		
		public TestDbInfo(String connectionUrl) {
			this.connectionUrl = connectionUrl;
		}
		
		/**
		 * Gets the connection URL used to connect to the test DB
		 * @return jdbc connection URL
		 */
		public String getConnectionUrl() {
			return this.connectionUrl;
		}
	}
}
