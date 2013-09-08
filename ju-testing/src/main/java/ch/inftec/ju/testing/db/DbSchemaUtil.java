package ch.inftec.ju.testing.db;

import java.sql.Connection;
import java.sql.SQLException;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.hibernate.jdbc.Work;

import ch.inftec.ju.db.EmfWork;
import ch.inftec.ju.db.JuEmfUtil;
import ch.inftec.ju.util.JuRuntimeException;

/**
 * Util class containing methods to perform DB Schema actions.
 * @author Martin
 *
 */
public class DbSchemaUtil {
	/**
	 * Runs the specified liquibase change log.
	 * @param persistenceUnitName Persistence unit referring to the DB we want to update
	 * @param changeLogResourceName Name of the change log resource. The resource will be loaded using the
	 * default class loader.
	 */
	public void runLiquibaseChangeLog(String persistenceUnitName, final String changeLogResourceName) {
		JuEmfUtil emfUtil = JuEmfUtil.create().persistenceUnitName(persistenceUnitName).build();
		
		try (EmfWork work = emfUtil.startWork()) {
			work.getEmUtil().doWork(new Work() {
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
}
