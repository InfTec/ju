package ch.inftec.ju.db;

public class DbSpecificHandlerMySql extends DbSpecificHandlerDefault {
	public DbSpecificHandlerMySql(JuEmUtil emUtil) {
		super(emUtil);
	}
	
	@Override
	public String convertTableNameCasing(String tableName) {
		// MySQL is case sensitive, so look for the exact casing in the table list
		for (String actualTableName : this.emUtil.getTableNames()) {
			if (actualTableName.equalsIgnoreCase(tableName)) return actualTableName;
		}
		
		// If we were unlucky, just return the same tableName
		return tableName;
	}
}
