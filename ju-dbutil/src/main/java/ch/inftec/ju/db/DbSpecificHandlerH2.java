package ch.inftec.ju.db;

import java.util.List;

import javax.persistence.EntityManager;

/**
 * DbSpecificHandler implementations for H2.
 * @author Martin
 *
 */
public class DbSpecificHandlerH2 extends DbSpecificHandlerDefault {
	public DbSpecificHandlerH2(JuEmUtil emUtil, EntityManager em) {
		super(emUtil, em);
	}
	
	@Override
	public List<String> getSequenceNames() {
		@SuppressWarnings("unchecked")
		List<String> results = (List<String>) this.em.createNativeQuery(
				"select SEQUENCE_NAME name from INFORMATION_SCHEMA.SEQUENCES").getResultList();
		
		return results;
	}
	
	@Override
	public void resetIdentityGenerationOrSequences(int val) {
		List<?> res = this.em.createNativeQuery(
				"select c.TABLE_NAME, c.COLUMN_NAME " +
				"from information_schema.columns c " +
				"where c.SEQUENCE_NAME is not null").getResultList();
			
		for (Object row : res) {
			Object[] aRow = (Object[]) row;
			String tableName = aRow[0].toString();
			String columnName = aRow[1].toString();
			
			logger.debug(String.format("Restarting ID column %s.%s with %d", tableName, columnName, val));
			
			this.em.createNativeQuery(String.format("alter table %s alter column %s restart with %d"
					, tableName
					, columnName
					, val)).executeUpdate();
		}
	}
}
