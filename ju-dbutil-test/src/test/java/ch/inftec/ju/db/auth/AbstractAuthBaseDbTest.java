package ch.inftec.ju.db.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;

import ch.inftec.ju.testing.db.AbstractBaseDbTest;
import ch.inftec.ju.testing.db.data.TestDb;
import ch.inftec.ju.testing.db.data.TestDbUtils;

/**
 * Base class for DB tests using the auth entities (AuthUser and AuthRole)
 * @author Martin
 *
 */
@ContextConfiguration(classes={AbstractAuthBaseDbTest.Configuration.class})
public abstract class AbstractAuthBaseDbTest extends AbstractBaseDbTest {
	@ImportResource("classpath:ch/inftec/ju/db/auth/AbstractAuthBaseDbTest-context.xml")
	static class Configuration {
		@Bean
		private TestDb testDb() {
			return new TestDbUtils().buildTestDb("Derby Auth InMemory-DB")
					.persistenceFile("/META-INF/authPersistence.xml")
					.createDerbyDb();
		}
	}
}
