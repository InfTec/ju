package ch.inftec.ju.db;

import java.util.List;

import javax.persistence.EntityManager;

/**
 * DbSpecificHandler implementations for MySQL.
 * @author Martin
 *
 */
public class DbSpecificHandlerMySql extends DbSpecificHandlerDefault {
	public DbSpecificHandlerMySql(JuEmUtil emUtil, EntityManager em) {
		super(emUtil, em);
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
	
	@Override
	public void resetIdentityGenerationOrSequences(int val) {
		List<?> res = this.em.createNativeQuery(
				"select c.TABLE_NAME, c.COLUMN_NAME " +
				"from information_schema.columns c " +
				"where c.EXTRA='auto_increment'" //c.TABLE_NAME='Player'" + 
				).getResultList();
			
		for (Object row : res) {
			Object[] aRow = (Object[]) row;
			String tableName = aRow[0].toString();
			String columnName = aRow[1].toString();
			
			logger.debug(String.format("Restarting ID column %s.%s with %d", tableName, columnName, val));
			
			this.em.createNativeQuery(String.format("alter table %s auto_increment = %d"
					, tableName
					, val)).executeUpdate();
		}
	}
}
