package ch.inftec.ju.testing.db;

import org.junit.Test;

public class DefaultDataTest extends AbstractDbTest {
	@Test
	@JuDbTest(profile="derby-testing", persistenceUnit="ju-testing pu-default-test-data")
	public void canLoadDefaultData_inDefaultPersistenceUnit() {
		new DbSchemaUtil(this.emUtil).prepareDefaultSchemaAndTestData();
	}
}