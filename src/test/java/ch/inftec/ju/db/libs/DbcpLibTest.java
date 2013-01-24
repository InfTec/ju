package ch.inftec.ju.db.libs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPoolFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.Test;

/**
 * Class to experiment with Apache DBCP (database connection pooling) library.
 * @author Martin
 *
 */
public class DbcpLibTest {
	@Test
	public void testPoolingDataSource() throws Exception {
		DataSource ds = DbcpLibTest.getPoolingDataSource();

		Connection conn = ds.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("VALUES 1");
		if (!rs.next()) {
			Assert.fail("ResultSet contained no row");
		}
		
		PreparedStatement ps = conn.prepareStatement("VALUES 2");
		rs = ps.executeQuery();
		if (!rs.next()) {
			Assert.fail("ResultSet contained no row");
		}
		
//		ps = conn.prepareStatement("VALUES 2");
//		ps.setInt(1, 1);
//		rs = ps.executeQuery();
//		if (!rs.next()) {
//			Assert.fail("ResultSet contained no row");
//		}
	}
	
	/**
	 * Gets a PoolingDataSource using DBCP library.
	 * @return PoolingDataSource instance
	 */
	public static DataSource getPoolingDataSource() {
		// Parameters used to connect to the database (here: Derby in-memory DB)
		String connectionUri = "jdbc:derby:memory:TestDB;create=true";
		String userName = "sa";
		String password = "";
		
		// The connection factory will create connections using the DriverManager mechanism, i.e.
		// automatically finding the correct database driver using the connection URI
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectionUri, userName, password);
		
		// Now we need an implementation of an object pool used for pooling. We need a factory less pool, as
		// the PoolableConnectionFactory will automatically register itself.
		ObjectPool connectionPool = new GenericObjectPool(null, 2); // Default: 8
		
		// Create a GenericKeyedObjectPoolFactory for pooling PreparedStatements
		GenericKeyedObjectPoolFactory keyedObjectPoolFactory = new GenericKeyedObjectPoolFactory(null, 2); // Default: 8

		// Create the connection factory. The factory will register itself in the pool
		new PoolableConnectionFactory(
				connectionFactory,
				connectionPool,
				keyedObjectPoolFactory, // KeyedObjectPoolFactory for PreparedStatements
				"VALUES 1", // Validation Query, Should return at least one row
				false, // Default Read-Only
				false); // Default Auto-Commit
		
		// Create the DataSource using our connection pool
		PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
		return dataSource;
	}
}
