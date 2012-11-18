package ch.inftec.ju.db;

import java.util.List;

import javax.persistence.EntityManager;


/**
 * Wrapper class around a SQL Connection instance. Implementations may wait to establish a Connection until
 * it is required. When the DbConnection isn't used anymore, it has to be closed explicitly. When a connection
 * is established, a transaction context is also created. The transaction is committed when closing the connection.
 * If it should be rolled back, rollback has to be called before closing.
 * <p>
 * DbConnections must implement the AutoCloseable interface so they can be used in the Java 7
 * try clause.
 * @author Martin
 *
 */
public interface DbConnection extends AutoCloseable {
	/**
	 * Gets the unique name of the DB connection.
	 * @return Connection name
	 */
	public String getName();
	
//	/**
//	 * Creates a connection to the DB.
//	 * @return Connection instance
//	 * @throws JuDbException If the connection cannot be created
//	 */
//	public Connection getConnection() throws JuDbException;
	
	/**
	 * Gets the name of the table's primary key column. Multi-Column primary keys are not supported.
	 * Column names are upper case.
	 * @param tableName Table name
	 * @return Name of the table's primary key
	 * @throws JuDbException If the primary key cannot be evaluated
	 */
	public String getPrimaryColumnName(String tableName) throws JuDbException;
	
	/**
	 * Gets all column names of the specified table in the order they are defined, or rather in the
	 * order the driver returns them. Column names are upper case.
	 * @param tableName Table name
	 * @return Column names of the table
	 * @throws JuDbException If the column names cannot be evaluated
	 */
	public List<String> getColumnNames(String tableName) throws JuDbException;
	
	/**
	 * Gets a DbQueryRunner instance that is based on this DbConnection.
	 * @return DbQueryRunner instance
	 */
	public DbQueryRunner getQueryRunner();
	
	/**
	 * Gets an EntityManager instance that is backing this DbConnection.
	 * Don't use the getTransaction of the EntityManager manually as this is
	 * handled by the DbConnection instance.
	 * @return EntityManager instance
	 */
	public EntityManager getEntityManager();
	
	/**
	 * Rolls back any changes.
	 */
	public void rollback();
	
	/*
	 * 'Override' the AutoCloaseable close not to throw an Exception.
	 */
	public void close();
}
