package ch.inftec.ju.dbutil.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.testing.db.AbstractDbTest;
import ch.inftec.ju.testing.db.DbSchemaUtil;
import ch.inftec.ju.util.JuCollectionUtils;

public class DbSchemaUtilTest extends AbstractDbTest {
	@Test
	public void tablesAreCreated_usingLiquibaseExplicitly() {
		DbSchemaUtil su = new DbSchemaUtil(this.em);
		
		su.clearSchema();
		Assert.assertFalse(JuCollectionUtils.collectionContainsIgnoreCase(this.emUtil.getTableNames(), "TestingEntity_LB"));
		
		su.runLiquibaseChangeLog("ch/inftec/ju/dbutil/test/LiquibaseTestDataTest_testingEntity.xml");
		
		Assert.assertTrue(JuCollectionUtils.collectionContainsIgnoreCase(this.emUtil.getTableNames(), "TestingEntity_LB"));
	}
	
	@Test
	public void tablesAreCreated_usingFlywayExplicitly() {
		DbSchemaUtil su = new DbSchemaUtil(this.em);
		
		su.clearSchema();
		Assert.assertFalse(JuCollectionUtils.collectionContainsIgnoreCase(this.emUtil.getTableNames(), "TESTINGENTITY_FW"));
		
		su.runFlywayMigration("db/DbSchemaUtilTest-migration");
		
		Assert.assertTrue(JuCollectionUtils.collectionContainsIgnoreCase(this.emUtil.getTableNames(), "TESTINGENTITY_FW"));
	}
	
	/**
	 * Check if we can use the replaceOrExists tag. Liquibase doesn't support it with Derby, throwing an Exception.
	 * Therefore we filter the change logs before submitting them to Liquibase.
	 */
	@Test
	public void liquibase_canUseReplaceOrExists() {
		DbSchemaUtil su = new DbSchemaUtil(this.em);
		
		su.clearSchema();
		su.runLiquibaseChangeLog("ch/inftec/ju/dbutil/test/DbSchemaUtilTest_liquibase_canUseReplaceOrExists.xml");
	}
}
