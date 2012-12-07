package ch.inftec.ju.testing.db.data;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbQueryRunner;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.testing.db.data.TestDbUtils.AbstractTestDb;

/**
 * Test database using an in-memory derby DB.
 * @author tgdmemae
 *
 */
class DerbyTestDb extends AbstractTestDb {
	DerbyTestDb() throws JuDbException {
		super("Derby InMemory-DB");
	}

	@Override
	protected void createTables() throws JuDbException {
		try (DbConnection dbConn = this.openDbConnection()) {
			DbQueryRunner qr = dbConn.getQueryRunner();
			
			qr.update("CREATE TABLE TEST_A ("
					+ "  Aid INTEGER not null primary key,"
					+ "  text VARCHAR(64),"
					+ "  b_fk INTEGER"
					+ ")");
			
			qr.update("CREATE TABLE TEST_B ("
					+ "  Bid INTEGER not null primary key,"
					+ "  text VARCHAR(64)"
					+ ")");
			
			qr.update("CREATE TABLE TEST_C ("
					+ "  Cid INTEGER not null primary key,"
					+ "  text VARCHAR(64),"
					+ "  a_fk INTEGER,"
					+ "  d_fk INTEGER"
					+ ")");
			
			qr.update("CREATE TABLE TEST_D ("
					+ "  Did INTEGER not null primary key,"
					+ "  text VARCHAR(64)"
					+ ")");
			
			qr.update("CREATE TABLE TEST_DATATYPES ("
					+ "  id INTEGER not null primary key,"
					+ "  integerNumber INTEGER,"
					+ "  varcharText VARCHAR(64),"
					+ "  clobText CLOB,"
					+ "  dateField DATE"
					+ ")");
		}
	}

	@Override
	protected void cleanup() throws JuDbException {
		try (DbConnection dbConn = this.openDbConnection()) {
			DbQueryRunner qr = dbConn.getQueryRunner();
		
			qr.update("DROP TABLE TEST_DATATYPES");
			
			qr.update("DROP TABLE TEST_A");
		}
	}
	
	@Override
	protected void resetPlatformSpecificData() throws JuDbException {
		try (DbConnection dbConn = this.openDbConnection()) {
			DbQueryRunner qr = dbConn.getQueryRunner();
//						
//			qr.update("DELETE FROM TEST_DATATYPES");
//			qr.update("INSERT INTO TEST_DATATYPES (ID, INTEGERNUMBER, VARCHARTEXT, CLOBTEXT, DATEFIELD) VALUES (1, 1, 'one', 'oneClob', '1980-12-03')");
//			qr.update("INSERT INTO TEST_DATATYPES (ID, INTEGERNUMBER, VARCHARTEXT, CLOBTEXT, DATEFIELD) VALUES (2, null, null, null, null)");

			// Reset sequence to guarantee predictable primary key values
			qr.update("UPDATE SEQUENCE SET SEQ_COUNT=? WHERE SEQ_NAME=?", 9, "SEQ_GEN");
		}
	}
}
