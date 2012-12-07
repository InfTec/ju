package ch.inftec.ju.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.DbRowUtils.DbRowsImpl;
import ch.inftec.ju.util.JuStringUtils;
import ch.inftec.ju.util.change.DbAction;
import ch.inftec.ju.util.change.DbActionUtils;


/**
 * Implementation of the DbConnection interface.
 * @author Martin
 *
 */
final class DbConnectionImpl implements DbConnection {
	Logger log = LoggerFactory.getLogger(DbConnectionImpl.class);

	private static int idCnt = 0;
	private final int id;
	
	private String name;
	private EntityManagerFactory entityManagerFactory;
	
	private EntityManager entityManager;
	private Connection connection;
	
	/**
	 * Creates a new connection creator with the specified name.
	 * @param name Unique name of the connection
	 * @param entityManagerFactory Factory used to created an EntityManager that will back
	 * up the connection
	 */
	protected DbConnectionImpl(String name, EntityManagerFactory entityManagerFactory) {
		this.name = name;
		this.entityManagerFactory = entityManagerFactory;
		
		synchronized(this) {
			this.id = DbConnectionImpl.idCnt++;
		}
	}
	
	/**
	 * Establishes an EntityManager, i.e. makes sure the DbConnection has an active
	 * EntityManager with a started transaction.
	 * @return EntityManager instance
	 */
	private EntityManager establishEntityManager() {
		if (this.entityManager == null) {
			log.debug("Establishing connection to EntityManager: " + this);
			this.entityManager = this.entityManagerFactory.createEntityManager();
			
			try {
				this.entityManager.getTransaction().begin();
			} catch (Exception ex) {
				this.entityManager = null;
				throw ex;
			}
		}
		
		return this.entityManager;
	}
	
	/**
	 * Establishes a Connection, i.e. makes sure the DbConnection has an active Connection
	 * with a started transaction.
	 * @return Connection instance
	 */
	private Connection establishConnection() {
		if (this.connection == null) {
			this.connection = this.establishEntityManager().unwrap(Connection.class);
		}
		
		return this.connection;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getPrimaryColumnName(String tableName) throws JuDbException {
		DbMetaData md = new DbMetaData();
		return md.getPrimaryColumnName(tableName);
	}

	@Override
	public List<String> getColumnNames(String tableName) throws JuDbException {
		DbMetaData md = new DbMetaData();
		return Arrays.asList(md.getColumnNames(tableName));			
	}

	@Override
	public DbQueryRunner getQueryRunner() {
		return new DbQueryRunnerImpl(this);
	}
	
	@Override
	public EntityManager getEntityManager() {
		return this.establishEntityManager();
	}
	
	@Override
	public Connection getConnection() {
		return this.establishConnection();
	}
	
	@Override
	public void rollback() {
		if (this.entityManager != null) {
			this.entityManager.getTransaction().rollback();
			this.entityManager.close();
			this.entityManager = null;
		}
	}
	
	@Override
	public void close() {
		if (this.entityManager != null) {
			// The transaction might have been marked for rollback (e.g. by an OptimisticLockException)
			if (this.entityManager.getTransaction().getRollbackOnly()) {
				log.debug("Rolling back transaction: " + this);
				this.rollback();
			} else {
				log.debug("Committing transaction: " + this);
				this.entityManager.getTransaction().commit();
				this.entityManager.close();
				this.entityManager = null;
			}
		}
	}
	
	/**
	 * Helper class to wrap around a DatabaseMetaData object.
	 * Make sure to call the close() method when the object is not used any longer.
	 * @author Martin
	 *
	 */
	private class DbMetaData {
		private DatabaseMetaData metaData;
		private ResultSet rs;
		
		/**
		 * Creates a new DbMetaData instance. If an exception occurs, close will be called
		 * implicitly.
		 * @throws JuDbException If the MetaData cannot be accessed
		 */
		public DbMetaData() throws JuDbException {
			try {
				this.metaData = DbConnectionImpl.this.establishConnection().getMetaData();
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't access DatabaseMetaData", ex);
			}
		}
		
		/**
		 * Gets the primary column name for the specified table.
		 * @param tableName Table name
		 * @return Primary column name
		 * @throws JuDbException If the primary column name cannot be evaluated
		 */
		public String getPrimaryColumnName(String tableName) throws JuDbException {
			try {
				this.rs = this.metaData.getPrimaryKeys(null, null, tableName.toUpperCase());
				
				String columnName = null;
				if (rs.next()) columnName = rs.getString("COLUMN_NAME");
				else throw new JuDbException("Couldn't evaluate primary key for table " + tableName);
				
				if (rs.next()) {
					throw new JuDbException("Driver returned multiple primary keys for table " + tableName);
				}
				
				return columnName.toUpperCase();
			} catch (JuDbException ex) {
				throw ex;
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't evaluate primary key for table " + tableName, ex);
			} finally {
				DbUtil.closeQuietly(this.rs);
				this.rs = null;
			}
		}
		
		/**
		 * Gets all column names of the specified table in the order they are defined, or rather in the
		 * order the driver returns them.
		 * @param tableName Table name
		 * @return Column names of the table
		 * @throws JuDbException If the column names cannot be evaluated
		 */
		public String[] getColumnNames(String tableName) throws JuDbException {
			try {
				this.rs = this.metaData.getColumns(null, null, tableName.toUpperCase(), null);			
				
				ArrayList<String> columnNames = new ArrayList<String>();
				
				while (rs.next()) {
					String columnName = rs.getString("COLUMN_NAME");
					columnNames.add(columnName.toUpperCase());
				}
				
				if (columnNames.size() == 0) {
					throw new JuDbException("Couldn't evaluate column names for table " + tableName + ": Driver returned empty ResultSet.");
				}
				
				return (String[])columnNames.toArray(new String[0]);
			} catch (JuDbException ex) {
				throw ex;
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't evaluate primary key for table " + tableName, ex);
			} finally {
				DbUtil.closeQuietly(this.rs);
				this.rs = null;
			}
		}
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(this, "name", this.getName(), "id", this.id);
	}
	
	/**
	 * Implementation of the DbQueryRunner interface that works with DbConnection
	 * instances.
	 * @author TGDMEMAE
	 *
	 */
	private final class DbQueryRunnerImpl implements DbQueryRunner {
		private DbConnectionImpl dbConnection;
		
		/**
		 * Creates a new DbQueryRunner using the specified DbConnection.
		 * @param dbConnection DbConnection instance
		 */
		public DbQueryRunnerImpl(DbConnectionImpl dbConnection) {
			this.dbConnection = dbConnection;
		}
		
		/**
		 * Gets a connection to the database.
		 * @return Connection instance
		 * @throws JuDbException If the connection cannot be established
		 */
		private Connection getConnection() throws JuDbException {
			return this.dbConnection.establishConnection();
		}
			
		@Override
		public DbRowsImpl query(String query, Object... params) throws JuDbException {
			try {
				QueryRunner qr = new QueryRunner();
				return qr.query(this.getConnection(), query, new DbRowResultSetHandler(), this.processParams(params));
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't execute query: " + query, ex);
			}
		}

		@Override
		public int update(String query, Object... params) throws JuDbException {
			try {
				QueryRunner qr = new QueryRunner();
				return qr.update(this.getConnection(), query, this.processParams(params));
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't execute update: " + query, ex);
			}
		}
		
		@Override
		public DbRow primaryKeyQuery(String tableName, Object primaryKeyValue) throws JuDbException {
			String selectQry = "SELECT * FROM " + tableName + " WHERE " + this.dbConnection.getPrimaryColumnName(tableName) + "=?";

			DbRows dbRows = this.query(selectQry, primaryKeyValue);
			
			if (dbRows.getRowCount() > 1) {
				throw new JuDbException("PrimaryKeyQuery for " + tableName + " with key=" + primaryKeyValue 
						+ ". Expected exactly 1 row, but got " + dbRows.getRowCount());
			}
			
			return dbRows.getRowCount() == 0 ? null : dbRows.getRow(0);
		}
		
		/**
		 * Executes a select * query on the specified table that returns no rows. Can be used
		 * to obtain an empty DbRows instance.
		 * @param tableName Table name
		 * @return DbRows instance with no rows
		 * @throws JuDbException If the query fails
		 */
		@Override
		public DbRow emptyRowQuery(String tableName) throws JuDbException {
			String selectQry = "SELECT * FROM " + tableName + " WHERE 1=0";
			return this.query(selectQry).getBaseRow();
		}
		
		@Override
		public DbAction getUpdateAction(String tableName, Object primaryKeyValue) throws JuDbException {
			DbRow row = this.primaryKeyQuery(tableName, primaryKeyValue);
			return row == null ? null : DbActionUtils.newUpdateAction(this.dbConnection, row, tableName).getAction();
		}
		
		@Override
		public DbAction getInsertAction(String tableName) throws JuDbException {
			return DbActionUtils.newInsertAction(this.dbConnection, tableName).getAction();
		}
		
		@Override
		public DbAction getDeleteAction(String tableName, Object primaryKeyValue) throws JuDbException {
			return DbActionUtils.newDeleteAction(this.dbConnection, tableName, primaryKeyValue);
		}
		
		/**
		 * Processes the parameters that are send to the QueryRunner.
		 * <p>
		 * For instance, this will convert a java.util.Date to a java.sql.Date
		 * @param object Parameters array
		 * @return Array with converted parameters
		 */
		private Object[] processParams(Object[] params) {
			Object[] newParams = Arrays.copyOf(params, params.length);
			
			for (int i = 0; i < newParams.length; i++) {
				Object param = newParams[i];
				
				if (param != null) {
					if (param.getClass() == java.util.Date.class) {
						newParams[i] = new java.sql.Date(((java.util.Date)param).getTime());
					}
				}
			}
			
			return newParams;
		}
	}
}
