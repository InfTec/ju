package ch.inftec.ju.db;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.db.JuEmUtil.DbType;
import ch.inftec.ju.testing.db.AbstractDbTest;

/**
 * Test class containing Derby specific tests.
 * @author Martin
 *
 */
public class MySqlTest extends AbstractDbTest {
	@Test
	public void dbType_isReckognized() {
		Assert.assertEquals(DbType.MYSQL, this.emUtil.getDbType());
	}
}
