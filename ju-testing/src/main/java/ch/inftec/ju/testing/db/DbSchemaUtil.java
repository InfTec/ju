package ch.inftec.ju.testing.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.hibernate.jdbc.Work;

import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.util.JuRuntimeException;

/**
 * Util class containing methods to perform DB Schema actions. Uses an EntityManager
 * to establish the connection to the corresponding DB.
 * @author Martin
 *
 */
public class DbSchemaUtil {
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
	
					Liquibase liquibase = new Liquibase(changeLogResourceName, new ClassLoaderResourceAccessor(), jdbcConn);
					liquibase.update(null);
				} catch (Exception ex) {
					throw new JuRuntimeException("Couldn't run Liquibase change log " + changeLogResourceName, ex);
				}
			}
		});
	}
}
