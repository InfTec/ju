package ch.inftec.ju.testing.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.db.JuEmUtil.DbType;
import ch.inftec.ju.util.JuRuntimeException;

/**
 * Util class containing methods to perform DB Schema actions. Uses an EntityManager
 * to establish the connection to the corresponding DB.
 * @author Martin
 *
 */
public class DbSchemaUtil {
	private final Logger logger = LoggerFactory.getLogger(DbSchemaUtil.class);
	
	private final JuEmUtil emUtil;
	
	public DbSchemaUtil(EntityManager em) {
		this.emUtil = new JuEmUtil(em);
	}
	
	/**
	 * Runs the specified liquibase change log.
	 * @param changeLogResourceName Name of the change log resource. The resource will be loaded using the
	 * default class loader.
	 */
	public void runLiquibaseChangeLog(final String changeLogResourceName) {
		this.emUtil.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				try {
					JdbcConnection jdbcConn = new JdbcConnection(connection);
					
					/**
					 * The default implementation of Liquibase for Oracle has an error in the default Schema
					 * lookup, so we'll set it here to avoid problems.
					 */
					Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConn);
					if (emUtil.getDbType() == DbType.ORACLE) {
						db.setDefaultSchemaName(emUtil.getMetaDataUserName());
					}
					
					Liquibase liquibase = new Liquibase(changeLogResourceName, new ClassLoaderResourceAccessor(), db);
					liquibase.update(null);
				} catch (Exception ex) {
					throw new JuRuntimeException("Couldn't run Liquibase change log " + changeLogResourceName, ex);
				}
			}
		});
	}
	
	/**
	 * Clears the DB Schema.
	 */
	public void clearSchema() {
		for (String tableName : this.emUtil.getTableNames()) {			
			String dropSql = String.format("DROP TABLE %s", tableName);
			logger.debug("Dropping table: " + dropSql);
			Query qry = this.emUtil.getEm().createNativeQuery(dropSql);
			qry.executeUpdate();
		}
	}
}
