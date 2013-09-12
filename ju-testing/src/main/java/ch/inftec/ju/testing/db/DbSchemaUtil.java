package ch.inftec.ju.testing.db;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.DsWork;
import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.db.JuEmUtil.DbType;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuRuntimeException;

import com.googlecode.flyway.core.Flyway;

/**
 * Util class containing methods to perform DB Schema actions. Uses an EntityManager
 * to establish the connection to the corresponding DB.
 * @author Martin
 *
 */
public class DbSchemaUtil {
	private final Logger logger = LoggerFactory.getLogger(DbSchemaUtil.class);
	
	private final JuEmUtil emUtil;
	
	public DbSchemaUtil(JuEmUtil emUtil) {
		this.emUtil = emUtil;
	}
	
	public DbSchemaUtil(EntityManager em) {
		this(new JuEmUtil(em));
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
					
					/*
					 * The default implementation of Liquibase for Oracle has an error in the default Schema
					 * lookup, so we'll set it here to avoid problems.
					 */
					Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConn);
					if (emUtil.getDbType() == DbType.ORACLE) {
						db.setDefaultSchemaName(emUtil.getMetaDataUserName());
					}
					
					/*
					 * Derby doesn't support the CREATE OR REPLACE syntax for Views and Liquibase will throw
					 * an error if the attribute is specified for Derby or H2.
					 * As we will use those DBs usually in memory, we'll just remote the attribute in all change logs
					 * using a custom ResourceAccessor that will filter the character stream.
					 */
					ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
					if (emUtil.getDbType() == DbType.DERBY || emUtil.getDbType() == DbType.H2) {
						resourceAccessor = new ResourceAccessorFilter(resourceAccessor);
					}
					
					Liquibase liquibase = new Liquibase(changeLogResourceName, resourceAccessor, db);
					liquibase.update(null);
				} catch (Exception ex) {
					throw new JuRuntimeException("Couldn't run Liquibase change log " + changeLogResourceName, ex);
				}
			}
		});
	}
	
	/**
	 * Runs Flyway migration scripts.
	 * @param locations Locations containing scripts in Flyway structure (e.g. db/migration).
	 */
	public void runFlywayMigration(final String... locations) {
		this.emUtil.doWork(new DsWork() {
			@Override
			public void execute(DataSource ds) {
				Flyway flyway = new Flyway();
				flyway.setDataSource(ds);
				flyway.setLocations(locations);
				flyway.migrate();
			}
		});
	}
	
	/**
	 * Clears the DB Schema.
	 * <p>
	 * Uses Flyway functionality.
	 */
	public void clearSchema() {
		this.emUtil.doWork(new DsWork() {
			@Override
			public void execute(DataSource ds) {
				Flyway flyway = new Flyway();
				flyway.setDataSource(ds);
				flyway.clean();
			}
		});
	}
	
	private class ResourceAccessorFilter implements ResourceAccessor {
		private final ResourceAccessor accessor;
		
		private ResourceAccessorFilter(ResourceAccessor accessor) {
			this.accessor = accessor;
		}

		@Override
		public InputStream getResourceAsStream(String file) throws IOException {
			logger.debug("Removing replaceIfExists attribute for Derby for resource " + file);
			InputStream is = this.accessor.getResourceAsStream(file);
			InputStreamReader reader = new InputStreamReader(is, "UTF-8");
			String text = IOUtil.toString(reader);
			String newText = text.replaceAll("replaceIfExists=\"true\"", "");
			
			return new BufferedInputStream(new ByteArrayInputStream(newText.getBytes()));
		}

		@Override
		public Enumeration<URL> getResources(String packageName)
				throws IOException {
			return this.accessor.getResources(packageName);
		}

		@Override
		public ClassLoader toClassLoader() {
			return this.accessor.toClassLoader();
		}
		
		
	}
}
