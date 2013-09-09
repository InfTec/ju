package ch.inftec.ju.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Query;

import org.hibernate.jdbc.Work;
import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.db.JuEmUtil.DbType;
import ch.inftec.ju.testing.db.AbstractDbTest;

/**
 * Test class containing Derby specific tests.
 * @author Martin
 *
 */
public class OracleTest extends AbstractDbTest {
	@Test
	public void dbType_isReckognized() {
		Assert.assertEquals(DbType.ORACLE, this.emUtil.getDbType());
	}
	
	@Test
	public void canEvaluateDefaultSchema() {
		Query qry = this.em.createNativeQuery("select sys_context( 'userenv', 'current_schema' ) from dual");
		Object res = qry.getSingleResult();
		Assert.assertNotNull(res);
	}
	
	@Test
	public void canEvaluateDefaultSchema_usingPrepareCall() {
		this.emUtil.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				CallableStatement cs = connection.prepareCall("select sys_context( 'userenv', 'current_schema' ) from dual");
				ResultSet rs = cs.executeQuery();
				rs.next();
			}
		});
	}
}
