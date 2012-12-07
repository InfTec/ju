package ch.inftec.ju.testing.db;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbQueryRunner;
import ch.inftec.ju.db.DbRow;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.testing.db.data.TestDb;
import ch.inftec.ju.testing.db.data.TestDbUtils;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuCollectionUtils;
import ch.inftec.ju.util.TestUtils;
import ch.inftec.ju.util.comparison.ValueComparator;

/**
 * Base class for tests that use test database data and should be performed for different
 * database implementations.
 * <p>
 * By default, a FULL DataSet profile is loaded into the DB automatically before
 * each test run. Use the protected constructor to provide another DefaultDataSet.
 * @author tgdmemae
 *
 */
public abstract class AbstractBaseDbTest {
	protected final Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Extending classes can use this DbConnection instance in their test methods. It will be
	 * automatically set at the beginning of each test case and closed after each test case.
	 */
	protected DbConnection dbConn;
	
	/**
	 * Extending classes can use this EntityManager instance in their test methods. It will be
	 * automatically set at the beginning of each test case and closed after each test case.
	 */
	protected EntityManager em;
	
	/**
	 * Extending classes can use this QueryRunner instance in their test methods. It will be
	 * automatically set at the beginning of each test case and closed after each test case.
	 */
	protected DbQueryRunner qr;
	
	private final URL dataSetFileUrl;
	
	protected AbstractBaseDbTest() {
		this(DefaultDataSet.FULL);
	}
	
	protected final void loadDataSet(DefaultDataSet dataSet) {
		this.getTestDb().loadTestData(dataSet.getUrl());
	}
	
	protected AbstractBaseDbTest(DefaultDataSet defaultDataSet) {
		dataSetFileUrl = defaultDataSet.getUrl();
	}
	
	@Before
	public final void initConnection() throws Exception {
		this.getTestDb().clearData();
		this.getTestDb().loadTestData(dataSetFileUrl);
		this.dbConn = this.openDbConnection();
		this.em = this.dbConn.getEntityManager();
		this.qr = this.dbConn.getQueryRunner();
	}
	
	@After
	public final void closeConnection() throws Exception {
		if (this.dbConn != null) this.dbConn.close();
	}
	
	/**
	 * Gets the TestDb instance to run the tests on.
	 * <p>
	 * Extending classes can override this method to provide a custom implementation. 
	 * This should always return the same instance.
	 * <p>
	 * The basic implementation returns a Derby In-Memory implementation
	 * @return TestDb instance to run the tests.
	 * @throws JuDbException If the connection to the TestDb cannot be established
	 */
	protected TestDb getTestDb() {
		return TestDbUtils.getDerbyInMemoryTestDb();
	}
	
	/**
	 * Opens a new DbConnection instance used to connect to the database.
	 * The caller is responsable for closing the connection.
	 * @return DbConnection instance
	 * @throws JuDbException If the DbConnection instance cannot be retrieved
	 */
	protected final DbConnection openDbConnection() throws JuDbException {
		return this.getTestDb().openDbConnection();
	}
	
	/**
	 * Asserts that the values of a row map those of the specified map. Uses
	 * a ValueComparator to compare the values, thus making sure that the tests
	 * succeed if for instance some database implementations return Long instances and some
	 * Integer.
	 * @param row DbRow instance
	 * @param expectedValues Expected values in a map, having the column names as keys
	 */
	protected final void assertRowEquals(DbRow row, Map<String, Object> expectedValues) {
		HashMap<String, Object> rowValues = new HashMap<String, Object>();
		
		for (int i = 0; i < row.getColumnCount(); i++) {
			rowValues.put(row.getColumnName(i), row.getValue(row.getColumnName(i)));
		}
		
		TestUtils.assertMapEquals(expectedValues, rowValues, ValueComparator.INSTANCE);
	}
	
	/**
	 * Asserts that the values of a row map those of specified keyValuePairs.
	 * @param row DbRow instance
	 * @param keyValuePairs KeyValue pairs
	 */
	protected final void assertRowEquals(DbRow row, Object... keyValuePairs) {
		this.assertRowEquals(row, JuCollectionUtils.stringMap(keyValuePairs));
	}
	
	/**
	 * Predefined DataSets that can be either set as default for a whole
	 * test class using the AbstractBaseDbTest constructor or be loaded
	 * as a clean insert using the method loadTestData.
	 * @author Martin
	 *
	 */
	protected static enum DefaultDataSet {
		NONE(null),
		SINGLE_TESTING_ENTITY("/datasets/singleTestingEntityData.xml"),
		FULL("/datasets/fullData.xml");
		
		private final String fileName;
		
		private DefaultDataSet(String fileName) {
			this.fileName = fileName;
		}
		
		private URL getUrl() {
			return IOUtil.getResourceURL(fileName);
		}
	}
}
