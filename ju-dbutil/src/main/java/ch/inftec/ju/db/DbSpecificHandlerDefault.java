package ch.inftec.ju.db;

/**
 * Default implementation of DbSpecificHandler. DB specific handlers can extend this class
 * and only have to override a method if the DB doesn't comply with the default handling.
 * @author Martin
 *
 */
public class DbSpecificHandlerDefault implements DbSpecificHandler {
	protected final JuEmUtil emUtil;
	
	protected DbSpecificHandlerDefault(JuEmUtil emUtil) {
		this.emUtil = emUtil;
	}
	
	@Override
	public String convertTableNameCasing(String tableName) {
		return tableName.toUpperCase();
	}
}
