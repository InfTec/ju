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
class OracleTestDb extends AbstractTestDb {
	private static OracleTestDb instance;
	
	public OracleTestDb() throws JuDbException {
		super("ESW MyTTS");
	}
	
	public static OracleTestDb getInstance() throws JuDbException {
		if (OracleTestDb.instance == null) {
			OracleTestDb.instance = new OracleTestDb();
		}
		return OracleTestDb.instance;
	}

	@Override
	protected void createTables() throws JuDbException {
		try (DbConnection dbConn = this.openDbConnection()) {
			DbQueryRunner qr = dbConn.getQueryRunner();
		
			qr.update("call drop_if_exists.dropTable('TEST_A')");
			
			qr.update("CREATE TABLE TEST_A ("
					+ "  Aid NUMBER(19) not null primary key,"
					+ "  text VARCHAR(64),"
					+ "  b_fk NUMBER(19)"
					+ ")");
			
			qr.update("call drop_if_exists.dropTable('TEST_B')");
			
			qr.update("CREATE TABLE TEST_B ("
					+ "  Bid NUMBER(19) not null primary key,"
					+ "  text VARCHAR(64)"
					+ ")");
			
			qr.update("call drop_if_exists.dropTable('TEST_C')");
			
			qr.update("CREATE TABLE TEST_C ("
					+ "  Cid NUMBER(19) not null primary key,"
					+ "  text VARCHAR(64),"
					+ "  a_fk NUMBER(19),"
					+ "  d_fk NUMBER(19)"
					+ ")");
			
			qr.update("call drop_if_exists.dropTable('TEST_D')");
			
			qr.update("CREATE TABLE TEST_D ("
					+ "  Did NUMBER(19) not null primary key,"
					+ "  text VARCHAR(64)"
					+ ")");
			
			qr.update("call drop_if_exists.dropTable('TEST_DATATYPES')");
			
			qr.update("CREATE TABLE TEST_DATATYPES ("
					+ "  id NUMBER(19) not null primary key,"
					+ "  integerNumber NUMBER(19),"
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
		
			qr.update("DELETE FROM TEST_DATATYPES");		
			qr.update("INSERT INTO TEST_DATATYPES (ID, INTEGERNUMBER, VARCHARTEXT, CLOBTEXT, DATEFIELD) VALUES (1, 1, 'one', 'oneClob', to_date('03.12.1980', 'dd.mm.yyyy'))");
		}
	}
}
