package ch.inftec.ju.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;

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
		MYSQL,
		ORACLE;
		
		private static DbType evaluateDbType(String productName) {
			if (productName.toLowerCase().contains("derby")) {
				return DERBY;
			} else if (productName.toLowerCase().contains("mysql")) {
				return MYSQL;
			} else if (productName.toLowerCase().contains("oracle")) {
				return ORACLE;
			} else {
				throw new JuDbException("Unknown DB. Product name: " + productName);
			}
		}
	}
}
