package ch.inftec.ju.db.auth;

import ch.inftec.ju.testing.db.AbstractBaseDbTest;
import ch.inftec.ju.testing.db.data.TestDb;
import ch.inftec.ju.testing.db.data.TestDbUtils;

/**
 * Base class for DB tests using the auth entities (AuthUser and AuthRole)
 * @author Martin
 *
 */
public class AbstractAuthBaseDbTest extends AbstractBaseDbTest {
	@Override
	protected TestDb createTestDb() {
		return new TestDbUtils().buildTestDb("Derby InMemory-DB")
			.persistenceFile("/META-INF/authPersistence.xml")
			.noDataXmlImportFile("/datasets/auth/noData.xml")
			.createDerbyDb();
	}
}
