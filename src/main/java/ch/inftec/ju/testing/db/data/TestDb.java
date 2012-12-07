package ch.inftec.ju.testing.db.data;

import java.net.URL;
import java.sql.SQLException;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.JuDbException;

/**
 * Instance of a Test database.
 * @author tgdmemae
 *
 */
public interface TestDb {
	/**
	 * Closes the DB, i.e. cleans up any resources created.
	 * @throws JuDbException If the cleanup fails
	 */
	public void close() throws JuDbException;

	/**
	 * Opens a new DbConnection that can be used to connect to the DB. The
	 * caller has to close the DbConnection itself.
	 * @return DbConnection
	 */
	public DbConnection openDbConnection();

	/**
	 * Clears all data in the Test DB.
	 * @throws JuDbException If the data cannot be cleared
	 */
	public void clearData() throws JuDbException;
	
	/**
	 * Loads test data from an XML import data set into the test DB.
	 * <p>
	 * This will perform a clean insert in the related tables, but will leave
	 * tables that are not part of the XML unaffected.
	 * @param testDataFile URL to a testDataFile to load values from 
	 * @throws SQLException If the test data cannot be loaded
	 */
	public void loadTestData(URL testDataFile) throws JuDbException;
}