package ch.inftec.ju.db.data.specific;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.db.data.AbstractBasicDbTest;
import ch.inftec.ju.db.data.AbstractJpaTest;
import ch.inftec.ju.db.data.TestDb;
import ch.inftec.ju.db.data.TestDbUtils;

/**
 * Helper class that extends all abstract DB tests using a Derby Test DB.
 * @author tgdmemae
 *
 */
@RunWith(Enclosed.class)
public class DerbyTest {
	public static class DerbyBasicDbTest extends AbstractBasicDbTest {
		@Override
		protected TestDb getTestDb() {
			return TestDbUtils.getDerbyInMemoryTestDb();
		}
	}
	
	public static class DerbyJpaTest extends AbstractJpaTest {
		@Override
		protected TestDb getTestDb() throws JuDbException {
			return TestDbUtils.getDerbyInMemoryTestDb();
		}
	}
}
