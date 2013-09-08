package ch.inftec.ju.dbutil.test;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.db.DatabaseMetaDataCallback;
import ch.inftec.ju.testing.db.AbstractDbTest;

public class JuEmUtilTest extends AbstractDbTest {
	@Test
	public void processMetaData() {
		String url = this.emUtil.extractDatabaseMetaData(new DatabaseMetaDataCallback<String>() {
			@Override
			public String processMetaData(DatabaseMetaData dbmd) throws SQLException {
				return dbmd.getURL();
			}
		});
		
		logger.debug("Got DB URL: " + url);
		Assert.assertTrue(url.startsWith("jdbc:"));
	}
	
	@Test
	public void getDriverName_returnsNotNull() {
		Assert.assertNotNull(this.emUtil.getDbType());
	}
}
