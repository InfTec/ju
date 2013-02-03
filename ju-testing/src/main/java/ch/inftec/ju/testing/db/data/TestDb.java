package ch.inftec.ju.testing.db.data;

import ch.inftec.ju.db.JuDbException;

/**
 * Instance of a Test database.
 * @author tgdmemae
 *
 */
public interface TestDb {
	/**
	 * Resets the Database so subsequent calls will yield the same results, e.g.
	 * sequences should be reset so new objects always get the same IDs.
	 * @throws JuDbException If the Database cannot be reset
	 */
	public void resetDatabase() throws JuDbException;
}