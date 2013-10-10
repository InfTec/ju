package ch.inftec.ju.db;

import java.util.List;

import javax.persistence.EntityManager;

import ch.inftec.ju.util.ConversionUtils;

/**
 * DbSpecificHandler implementations for Derby.
 * @author Martin
 *
 */
public class DbSpecificHandlerDerby extends DbSpecificHandlerDefault {
	public DbSpecificHandlerDerby(JuEmUtil emUtil, EntityManager em) {
		super(emUtil, em);
	}
	
	@Override
	public List<String> getSequenceNames() {
		@SuppressWarnings("unchecked")
		List<String> results = (List<String>) this.em.createNativeQuery(
				"select SEQUENCENAME name from SYS.SYSSEQUENCES").getResultList();
		
		return results;
	}
	
	@Override
	public void resetIdentityGenerationOrSequences(int val) {
		// Reset autoincrement values
		List<?> res = this.em.createNativeQuery(
				"select t.TABLENAME, c.COLUMNNAME " +
				"from sys.SYSCOLUMNS c " +
				"  inner join sys.SYSTABLES t on t.TABLEID = c.REFERENCEID " +
				"where c.AUTOINCREMENTVALUE is not null").getResultList();
			
		for (Object row : res) {
			Object[] aRow = (Object[]) row;
			String tableName = aRow[0].toString();
			String columnName = aRow[1].toString();
			
			logger.debug(String.format("Restarting ID column %s.%s with %d", tableName, columnName, val));
			
			this.em.createNativeQuery(String.format("alter table %s alter %s restart with %d"
					, tableName
					, columnName
					, val)).executeUpdate();
		}
		
		// Reset sequences
		// Derby doesn't seem to support altering sequences, so we'll use drop/create
		for (String sequenceName : this.getSequenceNames()) {
			logger.debug(String.format("Restarting (crop/create) sequence %s with %d", sequenceName, val));
			
			this.em.createNativeQuery(String.format("drop sequence %s restrict", sequenceName)).executeUpdate();
			this.em.createNativeQuery(String.format("create sequence %s start with %d", sequenceName, val)).executeUpdate();
		}
	}
	
	@Override
	public Long getNextValueFromSequence(String sequenceName) {
		// We'll probably get an Integer
		return ConversionUtils.toLong(this.em.createNativeQuery(String.format("values next value for %s", sequenceName)).getSingleResult());
	}
}
