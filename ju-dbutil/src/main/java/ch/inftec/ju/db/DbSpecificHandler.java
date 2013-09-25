package ch.inftec.ju.db;

import java.util.List;

/**
 * Helper interface to provide handling of DB specific actions and problems.
 * @author Martin
 *
 */
interface DbSpecificHandler {
	/**
	 * Converts the casing of the specified tableName so the DB will understand it.
	 * @param tableName Table name
	 * @return Table name in casing the DB will understand
	 */
	String convertTableNameCasing(String tableName);
	
	/**
	 * Gets a list of all sequence names of the DB, as returned by the driver.
	 * @return List of sequence names
	 */
	List<String> getSequenceNames();
	
	/**
	 * Resets identity generation of all tables or sequences to allow for predictable
	 * and repeatable entity generation.
	 * @param val Value for the next primary key
	 */
	void resetIdentityGenerationOrSequences(int val);
}
