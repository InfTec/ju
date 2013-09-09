package ch.inftec.ju.dbutil.test;

import org.junit.Test;

import ch.inftec.ju.testing.db.AbstractDbTest;
import ch.inftec.ju.testing.db.DbSchemaUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LiquibaseTestDataTest extends AbstractDbTest {
	@Test
	public void tablesAreCreated_usingLiquibaseExplicitly() {
		// TODO: Clear Schema...
		assertThat(this.emUtil.getTableNames(), not(hasItem("TESTINGENTITY")));
		
		new DbSchemaUtil(this.em).runLiquibaseChangeLog("ch/inftec/ju/dbutil/test/LiquibaseTestDataTest_testingEntity.xml");
		
		assertThat(this.emUtil.getTableNames(), hasItem("TESTINGENTITY"));
	}
}
