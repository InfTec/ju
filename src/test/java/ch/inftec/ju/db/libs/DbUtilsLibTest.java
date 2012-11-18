package ch.inftec.ju.db.libs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * Class to experiment with DbUtils library.
 * @author Martin
 *
 */
public class DbUtilsLibTest {
	@Test
	public void testDbUtils() throws Exception {
		DataSource ds = DbcpLibTest.getPoolingDataSource();
		Connection conn = ds.getConnection();
		
		QueryRunner qr = new QueryRunner();
		
		qr.update(conn, "CREATE TABLE TEST ("
					+ "  testid INTEGER not null primary key,"
					+ "  text VARCHAR(64),"
					+ "  val INTEGER"
					+ ")");
		// Commit must be executed for Derby, otherwise the import will fail...
		conn.commit();
		
		// Perform a batch insert
		
		String insert = "INSERT INTO TEST (testid, text, val) VALUES (?, ?, ?)";
		int res[] = qr.batch(conn, insert, new Object[][] {
				new Object[] {1, "1", 101},
				new Object[] {2, "2", 102},
				new Object[] {3, "3", 103},
				new Object[] {4, "4", 104},
				new Object[] {5, "5", 105}
			}
		);
		conn.commit();
		
		Assert.assertArrayEquals(res, new int[] {1, 1, 1, 1, 1});
		
		// Select a column using a predefined handler
		
		List<Object> textObjs = qr.query(conn, "SELECT TEXT FROM TEST ORDER BY TESTID", new ColumnListHandler());
		Assert.assertEquals(textObjs.size(), 5);
		
		// Implement a custom handler
		
		ResultSetHandler<String[]> h = new ResultSetHandler<String[]>() {
			@Override
			public String[] handle(ResultSet rs) throws SQLException {
				ArrayList<String> strings = new ArrayList<String>();
				
				while (rs.next()) {
					strings.add(rs.getString(1));
				}
				
				return (String[])strings.toArray(new String[0]);
			}
		};
		
		String textStrings[] = qr.query(conn, "SELECT TEXT FROM TEST ORDER BY TESTID", h);
		Assert.assertArrayEquals(textStrings, new String[] {"1", "2", "3", "4", "5"});
		
		DbUtils.close(conn);
	}
}
