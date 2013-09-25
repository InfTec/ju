package ch.inftec.ju.db;

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
	public String convertTableNameCasing(String tableName);
}
