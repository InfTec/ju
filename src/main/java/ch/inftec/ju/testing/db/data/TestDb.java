package ch.inftec.ju.testing.db.data;

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
	 * Resets the test data in the test tables. 
	 * @throws SQLException If the test data cannot be reset
	 */
	public void resetData() throws JuDbException;
}