package ch.inftec.ju.dbutil.test;

import org.junit.Test;

import ch.inftec.ju.testing.db.AbstractDbTest;
import ch.inftec.ju.testing.db.DbSchemaUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DbSchemaUtilTest extends AbstractDbTest {
	@Test
	public void tablesAreCreated_usingLiquibaseExplicitly() {
		DbSchemaUtil su = new DbSchemaUtil(this.em);
		
		su.clearSchema();
		assertThat(this.emUtil.getTableNames(), not(hasItem("TESTINGENTITY")));
		
		su.runLiquibaseChangeLog("ch/inftec/ju/dbutil/test/LiquibaseTestDataTest_testingEntity.xml");
		
		assertThat(this.emUtil.getTableNames(), hasItem("TESTINGENTITY"));
	}
	
	@Test
	public void tablesAreCreated_usingFlywayExplicitly() {
		DbSchemaUtil su = new DbSchemaUtil(this.em);
		
		su.clearSchema();
		assertThat(this.emUtil.getTableNames(), not(hasItem("TESTINGENTITY_FW")));
		
		su.runFlywayMigration("db/DbSchemaUtilTest-migration");
		
		assertThat(this.emUtil.getTableNames(), hasItem("TESTINGENTITY_FW"));
	}
	
	/**
	 * Check if we can use the replaceOrExists tag. Liquibase doesn't support it with Derby, throwing an Exception.
	 * Therefore we filter the change logs before submitting them to Liquibase.
	 */
	@Test
	public void liquibase_canUseReplaceOrExists() {
		DbSchemaUtil su = new DbSchemaUtil(this.em);
		su.runLiquibaseChangeLog("ch/inftec/ju/dbutil/test/DbSchemaUtilTest_liquibase_canUseReplaceOrExists.xml");
	}
}
