package ch.inftec.ju.testing.db;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
 * Base class for tests that use test database data.
 * <p>
 * Different DB implementations can be testing by extending the implementing class
 * and overriding the getTestDb method.
 * <p>
 * By default, no test data is loaded. Override the loadDefaultTestData method to
 * load test data in each test setup.
 * <p>
 * Note that test data is commited after loading to avoid transactional problems when
 * using multiple connections.
 * 
 * @author tgdmemae
 *
 */
@ContextConfiguration(classes={AbstractBaseDbTest.Configuration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class AbstractBaseDbTest {
	/**
	 * Spring configuration.
	 * @author tgdmemae
	 *
	 */
	static class Configuration {
		@Bean
		@Scope("prototype")
		private TestDb testDb() {
			return TestDbUtils.getDerbyInMemoryTestDb();
		}
	}
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
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
	
	@Autowired
	private TestDb testDb;
	
	@Autowired(required=false)
	@Qualifier("dataSet")
	private List<URL> initialDataSetUrls;
	
	@Autowired(required=false)
	private List<DefaultDataSet> initialDataSets;
	
	/**
	 * Helper method to load the data of the provided DefaultDataSet.
	 * <p>
	 * This will perform a clean insert of the data contained in the set, but
	 * leave tables not included in the set unaffected.
	 * @param dataSet DataSet to load data of
	 */
	protected final void loadDataSet(DefaultDataSet dataSet) {
		this.loadDataSet(dataSet.getUrl());
	}
	
	/**
	 * Helper method to load the data of the provided import file.
	 * <p>
	 * This will perform a clean insert of the data contained in the set, but
	 * leave tables not included in the set unaffected.
	 * @param testDataFile URL to a test data file
	 */
	protected final void loadDataSet(URL testDataFile) {
		this.testDb.loadTestData(testDataFile);
	}
	
	/**
	 * Helper method to load the data from the provided import file.
	 * @param testDataFile Path to a test data file on the classpath
	 */
	protected final void loadDataSet(String testDataFile) {
		this.loadDataSet(IOUtil.getResourceURL(testDataFile));
	}
	
	@Before
	public final void initConnection() throws Exception {
		this.testDb.clearData();
		this.loadDefaultTestData();
		this.dbConn = this.openDbConnection();
		this.em = this.dbConn.getEntityManager();
		this.qr = this.dbConn.getQueryRunner();
	}
	
	@After
	public final void closeConnection() throws Exception {
		if (this.dbConn != null) this.dbConn.close();
	}
	
	/**
	 * Reinitializes the connection (i.e. dbConn, em and qr) of the test case.
	 * <p>
	 * This will implicitly commit all transactions and can be done to make sure changed
	 * data can be seen by other transactions.
	 * @param evictCache If true, the EntityManager cache will be evicted. Use this if data
	 * has been modified outside the EntityManager.
	 */
	protected final void reInitConnection(boolean evictCache) {
		try {
			this.closeConnection();
			this.dbConn = this.openDbConnection();
			this.em = this.dbConn.getEntityManager();
			this.qr = this.dbConn.getQueryRunner();
			
			if (evictCache) this.em.getEntityManagerFactory().getCache().evictAll();
		} catch (Exception ex) {
			throw new JuDbException("Couldn't reinit connection", ex);
		}
	}
	
	/**
	 * Loads the specified default data sets at the beginning of a test.
	 */
	private void loadDefaultTestData() {
		for (DefaultDataSet dataSet : JuCollectionUtils.emptyForNull(this.initialDataSets)) {
			this.loadDataSet(dataSet);
		}
		
		for (URL dataSetUrl : JuCollectionUtils.emptyForNull(this.initialDataSetUrls)) {
			this.loadDataSet(dataSetUrl);
		}
	}
	
	/**
	 * Opens a new DbConnection instance used to connect to the database.
	 * The caller is responsable for closing the connection.
	 * @return DbConnection instance
	 * @throws JuDbException If the DbConnection instance cannot be retrieved
	 */
	protected final DbConnection openDbConnection() throws JuDbException {
		return this.testDb.openDbConnection();
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
	public static enum DefaultDataSet {
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
