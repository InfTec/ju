package ch.inftec.ju.testing.db;

import org.junit.Test;

public class DefaultTestDataTest extends AbstractDbTest {
	@Test
	public void tableStructure_canBeCreatedUsingLiquibase() {
		DbSchemaUtil su = new DbSchemaUtil(this.em);
		su.clearSchema();
		
		su.runLiquibaseChangeLog("ju-testing/data/default-changeLog.xml");
	}
}
