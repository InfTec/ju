package ch.inftec.ju.testing.db.specific;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import ch.inftec.ju.db.data.TestDb;
import ch.inftec.ju.db.data.TestDbUtils;
import ch.inftec.ju.testing.db.AbstractDbDataUtilsTest;

/**
 * Test class that contains all the DB tests using a Derby in Memory DB.
 * @author Martin
 *
 */
@RunWith(Enclosed.class)
public class DerbyTests {
	public static class DerbyDbDataUtilsTest extends AbstractDbDataUtilsTest {
		@Override
		protected TestDb getTestDb() {
			return TestDbUtils.getDerbyInMemoryTestDb();
		}
	}
}
