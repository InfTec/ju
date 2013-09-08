package ch.inftec.ju.testing.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.Assert;
import org.junit.Test;

@JuDbTest(profile="derby-lb", persistenceUnit="ju-testing-pu-liquibase")
public class LiquibaseTest extends AbstractDbTest {
	@Test
	public void canGenerateSchema_usingLiquibase() {
		Assert.assertEquals(0, this.emUtil.getTableNames().size());
		
		new DbSchemaUtil(this.em).runLiquibaseChangeLog("ch/inftec/ju/testing/db/LiquibaseTest_testingEntityChangeLog.xml");
		
		assertThat(this.emUtil.getTableNames(), hasItem("TESTINGENTITY"));
	}
}
