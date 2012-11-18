package ch.inftec.ju.db.specific;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import ch.inftec.ju.db.AbstractBasicDbTest;
import ch.inftec.ju.db.AbstractJpaTest;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.db.change.AbstractDbActionTest;
import ch.inftec.ju.db.change.AbstractDbChangeSetTest;
import ch.inftec.ju.db.data.AbstractTestDbTest;
import ch.inftec.ju.db.data.TestDb;
import ch.inftec.ju.db.data.TestDbUtils;

/**
 * Helper class that extends all abstract DB tests using a Derby Test DB.
 * @author tgdmemae
 *
 */
@RunWith(Enclosed.class)
public class DerbyTests {
	public static class DerbyBasicDbTest extends AbstractBasicDbTest {
		@Override
		protected TestDb getTestDb() {
			return TestDbUtils.getDerbyInMemoryTestDb();
		}
	}
	
	public static class DerbyTestDbTest extends AbstractTestDbTest {
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
	
	public static class DerbyDbChangeSetTest extends AbstractDbChangeSetTest {
		@Override
		protected TestDb getTestDb() throws JuDbException {
			return TestDbUtils.getDerbyInMemoryTestDb();
		}
	}
	
	public static class DerbyDbActionTest extends AbstractDbActionTest {
		@Override
		protected TestDb getTestDb() throws JuDbException {
			return TestDbUtils.getDerbyInMemoryTestDb();
		}
	}
}
