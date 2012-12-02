package ch.inftec.ju.db;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;

import ch.inftec.ju.testing.db.data.TestDb;
import ch.inftec.ju.util.JuCollectionUtils;
import ch.inftec.ju.util.TestUtils;
import ch.inftec.ju.util.comparison.ValueComparator;

/**
 * Base class for tests that use test database data and should be performed for different
 * database implementations.
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
	
	@Before
	public final void initConnection() throws Exception {
		this.getTestDb().resetData();
		this.dbConn = this.openDbConnection();
		this.em = this.dbConn.getEntityManager();
		this.qr = this.dbConn.getQueryRunner();
	}
	
	@After
	public final void closeConnection() throws Exception {
		if (this.dbConn != null) this.dbConn.close();
	}
	
	/**
	 * Extending classes must return the TestDb to use for the unit tests. This should always return
	 * the same instance.
	 * @return TestDb instance to run the tests.
	 * @throws JuDbException If the connection to the TestDb cannot be established
	 */
	protected abstract TestDb getTestDb() throws JuDbException;	
	
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
}
