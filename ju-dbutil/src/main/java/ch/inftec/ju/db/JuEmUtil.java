package ch.inftec.ju.db;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.ejb.EntityManagerImpl;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jdbc.Work;
import org.hibernate.service.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;

import ch.inftec.ju.util.DataHolder;

/**
 * Helper class building on EntityManager that provides DB utility functions.
 * <p>
 * This class won't handle disposing of EntityManager. It won't handle transactions either. These things
 * should either be handled by a container or by the JuEmfUtil class.
 * @author Martin
 *
 */
public class JuEmUtil {
	private final EntityManager em;
	
	/**
	 * Creates a new JuDbUtil based on the specified EntityManager instance.
	 * <p>
	 * Make sure the client takes care of EntityManager disposing
	 * @param em EntityManager instance
	 */
	public JuEmUtil(EntityManager em) {
		this.em = em;
	}
	
	/**
	 * Gets the EntityManager wrapped by this util instance.
	 * @return EntityManager
	 */
	public EntityManager getEm() {
		return this.em;
	}
	
	/**
	 * Executes some DB work using a raw JDBC connection.
	 * <p>
	 * Makes use of the Hibernate Work facility.
	 * @param work Work callback interface
	 */
	public void doWork(Work work) {
		Session session = this.em.unwrap(Session.class);
		session.doWork(work);
	}
	
	/**
	 * Executes some DB work that require a DataSource instance.
	 * @param work DsWork callback interface
	 */
	public void doWork(DsWork work) {
		HibernateEntityManagerFactory factory = (HibernateEntityManagerFactory) ((EntityManagerImpl) this.em).getEntityManagerFactory();
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) factory.getSessionFactory();
		
		ConnectionProvider connProvider = sessionFactory.getConnectionProvider();
		DataSource ds = null;
		if (connProvider instanceof DatasourceConnectionProviderImpl) {
			ds = ((DatasourceConnectionProviderImpl) connProvider).getDataSource();
		} else {
			ds = new ConnectionProviderDataSource(connProvider);
		}
		
		work.execute(ds);
	}

	/**
	 * Method to extract info from the DatabaseMetaData.
	 * @param action Callback method to do the data extracting into an arbitrary data structure.
	 * @return Data as returned by the callback function
	 */
	public <T> T extractDatabaseMetaData(final DatabaseMetaDataCallback<T> action) {
		final DataHolder<T> res = new DataHolder<>();
		
		this.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				DatabaseMetaData metaData = connection.getMetaData();
				res.setValue(action.processMetaData(metaData));
			}
		});
		
		return res.getValue();
	}
	
	/**
	 * Gets the Connection URL of this EntityManager from the MetaData.
	 * @return Connection URL
	 */
	public String getMetaDataUrl() {
		return this.extractDatabaseMetaData(new DatabaseMetaDataCallback<String>() {
			@Override
			public String processMetaData(DatabaseMetaData dbmd)
					throws SQLException {
				return dbmd.getURL();
			}
		});
	}
	
	/**
	 * Gets the UserName from the connection of this EntityManager from the MetaData.
	 * @return Connection UserName
	 */
	public String getMetaDataUserName() {
		return this.extractDatabaseMetaData(new DatabaseMetaDataCallback<String>() {
			@Override
			public String processMetaData(DatabaseMetaData dbmd)
					throws SQLException {
				return dbmd.getUserName();
			}
		});
	}
	
	/**
	 * Gets the name of the connection catalog or null if there is none.
	 * @return Catalog name
	 */
	public String getConnectionCatalog() {
		final DataHolder<String> catalog = new DataHolder<>();
		
		this.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				catalog.setValue(connection.getCatalog());
			}
		});
		
		return catalog.getValue();
	}
	
	/**
	 * Gets a list of all table names of the DB. Table names are all upper case.
	 * @return List of Table names
	 * @throws JuDbException If the list cannot be evaluated
	 */
	public List<String> getTableNames() throws JuDbException {
		List<String> tableNames = this.extractDatabaseMetaData(new DatabaseMetaDataCallback<List<String>>() {
			@Override
			public List<String> processMetaData(DatabaseMetaData dbmd) throws SQLException {
				// TODO: Consider Schema names for other DBs; refactor
				String schemaName = null;
				if (getDbType() == DbType.ORACLE) {
					schemaName = getMetaDataUserName();
				}
				
				ResultSet rs = dbmd.getTables(schemaName, schemaName, null, new String[]{"TABLE"});
				
				List<String> tableNames = new ArrayList<>();
				while (rs.next()) {
					String tableName = rs.getString("TABLE_NAME").toUpperCase();
					// We check if the TableName already exists in the list as
					// Oracle seems to return the same table names multiple times on some
					// Schemas...
					if (!tableNames.contains(tableName)) {
						tableNames.add(tableName);
					}
				}
				rs.close();
				
				Collections.sort(tableNames);
				
				return tableNames;
			}
		});
		
		return tableNames;		
	}
	
	/**
	 * Gets the type of the DB implementation of this EntityManager. If the type is not known (or supported)
	 * by JuEmUtil, an exception is thrown.
	 * @return DbType
	 */
	public DbType getDbType() {
		String productName = this.extractDatabaseMetaData(new DatabaseMetaDataCallback<String>() {
			@Override
			public String processMetaData(DatabaseMetaData dbmd) throws SQLException {
				return dbmd.getDatabaseProductName();
			}
		});
		
		return DbType.evaluateDbType(productName);
	}
	
	public enum DbType {
		DERBY,
		H2,
		MYSQL,
		ORACLE;
		
		private static DbType evaluateDbType(String productName) {
			if (productName.toLowerCase().contains("derby")) {
				return DERBY;
			} else if (productName.toLowerCase().contains("h2")) {
				return H2; 
			} else if (productName.toLowerCase().contains("mysql")) {
				return MYSQL;
			} else if (productName.toLowerCase().contains("oracle")) {
				return ORACLE;
			} else {
				throw new JuDbException("Unknown DB. Product name: " + productName);
			}
		}
	}
	
	private static class ConnectionProviderDataSource implements DataSource {
		private final ConnectionProvider connectionProvider;
		
		public ConnectionProviderDataSource(ConnectionProvider connProvider) {
			this.connectionProvider = connProvider;
		}
		
		@Override
		public PrintWriter getLogWriter() throws SQLException {
			throw new UnsupportedOperationException("unwrap");
		}

		@Override
		public void setLogWriter(PrintWriter out) throws SQLException {
			throw new UnsupportedOperationException("unwrap");			
		}

		@Override
		public void setLoginTimeout(int seconds) throws SQLException {
			throw new UnsupportedOperationException("unwrap");			
		}

		@Override
		public int getLoginTimeout() throws SQLException {
			throw new UnsupportedOperationException("unwrap");
		}

		@Override
		public Logger getParentLogger() throws SQLFeatureNotSupportedException {
			throw new UnsupportedOperationException("unwrap");
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			throw new UnsupportedOperationException("unwrap");
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			throw new UnsupportedOperationException("unwrap");
		}

		@Override
		public Connection getConnection() throws SQLException {
			Connection conn = this.connectionProvider.getConnection();
			Connection connProxy = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[] { Connection.class}, new ConnectionInvocationHandler(conn));
			return connProxy;
			
			
		}

		@Override
		public Connection getConnection(String username, String password) throws SQLException {
			throw new UnsupportedOperationException("unwrap");
		}
		
		private class ConnectionInvocationHandler implements InvocationHandler {
			private final Connection wrappedConnection;
			
			private ConnectionInvocationHandler(Connection wrappedConnection) {
				this.wrappedConnection = wrappedConnection;
			}

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (method.getName().equals("close")) {
					connectionProvider.closeConnection(this.wrappedConnection);
					return null;
				} else {
					return method.invoke(this.wrappedConnection, args);
				}
			}
			
			
		}
		
	}
}
