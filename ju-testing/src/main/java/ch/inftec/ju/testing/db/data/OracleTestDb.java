package ch.inftec.ju.testing.db.data;

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
		this.setNoDataXmlImportFile("/datasets/noData.xml");
	}
	
	public static OracleTestDb getInstance() throws JuDbException {
		if (OracleTestDb.instance == null) {
			OracleTestDb.instance = new OracleTestDb();
		}
		return OracleTestDb.instance;
	}

	@Override
	protected void createTables() throws JuDbException {
		this.jdbcTemplate.update("call drop_if_exists.dropTable('TEST_A')");
		
		this.jdbcTemplate.update("CREATE TABLE TEST_A ("
				+ "  Aid NUMBER(19) not null primary key,"
				+ "  text VARCHAR(64),"
				+ "  b_fk NUMBER(19)"
				+ ")");
		
		this.jdbcTemplate.update("call drop_if_exists.dropTable('TEST_B')");
		
		this.jdbcTemplate.update("CREATE TABLE TEST_B ("
				+ "  Bid NUMBER(19) not null primary key,"
				+ "  text VARCHAR(64)"
				+ ")");
		
		this.jdbcTemplate.update("call drop_if_exists.dropTable('TEST_C')");
		
		this.jdbcTemplate.update("CREATE TABLE TEST_C ("
				+ "  Cid NUMBER(19) not null primary key,"
				+ "  text VARCHAR(64),"
				+ "  a_fk NUMBER(19),"
				+ "  d_fk NUMBER(19)"
				+ ")");
		
		this.jdbcTemplate.update("call drop_if_exists.dropTable('TEST_D')");
		
		this.jdbcTemplate.update("CREATE TABLE TEST_D ("
				+ "  Did NUMBER(19) not null primary key,"
				+ "  text VARCHAR(64)"
				+ ")");
		
		this.jdbcTemplate.update("call drop_if_exists.dropTable('TEST_DATATYPES')");
		
		this.jdbcTemplate.update("CREATE TABLE TEST_DATATYPES ("
				+ "  id NUMBER(19) not null primary key,"
				+ "  integerNumber NUMBER(19),"
				+ "  varcharText VARCHAR(64),"
				+ "  clobText CLOB,"
				+ "  dateField DATE"
				+ ")");
	}
	
	@Override
	protected void cleanup() throws JuDbException {
		this.jdbcTemplate.update("DROP TABLE TEST_DATATYPES");
		
		this.jdbcTemplate.update("DROP TABLE TEST_A");
	}
	
	@Override
	protected void resetPlatformSpecificData() throws JuDbException {
		this.jdbcTemplate.update("DELETE FROM TEST_DATATYPES");		
		this.jdbcTemplate.update("INSERT INTO TEST_DATATYPES (ID, INTEGERNUMBER, VARCHARTEXT, CLOBTEXT, DATEFIELD) VALUES (1, 1, 'one', 'oneClob', to_date('03.12.1980', 'dd.mm.yyyy'))");
	}
}
