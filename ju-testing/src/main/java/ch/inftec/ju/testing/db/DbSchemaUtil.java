package ch.inftec.ju.testing.db;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.util.Enumeration;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.DsWork;
import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.db.JuEmUtil.DbType;
import ch.inftec.ju.db.TxHandler;
import ch.inftec.ju.testing.db.DbDataUtil.ImportBuilder;
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
	private final TxHandler tx;
	
	public DbSchemaUtil(JuEmUtil emUtil) {
		this(emUtil, null);
	}
	
	private DbSchemaUtil(JuEmUtil emUtil, UserTransaction tx) {
		this.emUtil = emUtil;
		this.tx = new TxHandler(tx);
	}
	
	public DbSchemaUtil(EntityManager em) {
		this(new JuEmUtil(em));
	}
	
	/**
	 * Initializes the DbSchemaUtil with an EntityManager and a UserTransaction object.
	 * <p>
	 * This initialization must be used in a container environment in a bean
	 * managed transaction context. Otherwise, we cannot control the transactions the way
	 * we have. Liquibase uses its own transaction management and this is not possible if there
	 * is a managed transaction running. On the other hand, we perform some operations on the
	 * EntityManager that require a transaction to be active.
	 * @param em EntityManager provided by the container
	 * @param tx UserTransaction instance of a bean managed transaction context
	 */
	public DbSchemaUtil(EntityManager em, UserTransaction tx) {
		this(new JuEmUtil(em), tx);
	}
	
	/**
	 * Runs the specified liquibase change log.
	 * @param changeLogResourceName Name of the change log resource. The resource will be loaded using the
	 * default class loader.
	 */
	public void runLiquibaseChangeLog(final String changeLogResourceName) {
		try {
			// Make sure we have a transaction when accessing entity manager meta data
			this.tx.begin();
			final DbType dbType = this.emUtil.getDbType();
			final String metaDataUserName = this.emUtil.getMetaDataUserName();
			this.tx.commit();
			
			this.emUtil.doWork(new DsWork() {
				@Override
				public void execute(DataSource ds) {
					try (Connection conn = ds.getConnection()) {
						JdbcConnection jdbcConn = new JdbcConnection(conn);
						
						/*
						 * The default implementation of Liquibase for Oracle has an error in the default Schema
						 * lookup, so we'll set it here to avoid problems.
						 */
						Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConn);
						if (dbType == DbType.ORACLE) {
							db.setDefaultSchemaName(metaDataUserName);
						}
						
						/*
						 * Derby doesn't support the CREATE OR REPLACE syntax for Views and Liquibase will throw
						 * an error if the attribute is specified for Derby or H2.
						 * As we will use those DBs usually in memory, we'll just remote the attribute in all change logs
						 * using a custom ResourceAccessor that will filter the character stream.
						 */
						ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
						if (dbType == DbType.DERBY || dbType == DbType.H2) {
							resourceAccessor = new ResourceAccessorFilter(resourceAccessor);
						}
						
						Liquibase liquibase = new Liquibase(changeLogResourceName, resourceAccessor, db);
						liquibase.update(null);
					} catch (Exception ex) {
						throw new JuRuntimeException("Couldn't run Liquibase change log " + changeLogResourceName, ex);
					}
				}
			});
		} finally {
			this.tx.rollbackIfNotCommitted();
		}
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
				flyway.clean(); // FIXME: Try Liquibase.dropAll()
			}
		});
	}
	
	/**
	 * Creates the Default test DB Schema (Player, Team, TestingEntity...) and
	 * loads the default test data.
	 * <p>
	 * Also resets the sequences to 1.
	 */
	public void prepareDefaultSchemaAndTestData() {
		this.prepareDefaultTestData(false, false, true);
	}
	
	/**
	 * Loads the default test data and resets the sequences to 1.
	 * <p>
	 * Doesn't perform Schema updates.
	 */
	public void loadDefaultTestData() {
		this.prepareDefaultTestData(false, true, false);
	}
	
	/**
	 * Loads the default test data (Player, Team, TestingEntity, ...), making
	 * sure that the tables have been created using Liquibase.
	 * @param emptyTables If true, the default tables will be cleaned
	 * @param resetSequences If true, sequences (or identity columns) will be reset to 1
	 * @param createSchema If true, the Schema will be created (or verified) using Liquibase
	 */
	public void prepareDefaultTestData(boolean emptyTables, boolean resetSequences, boolean createSchema) {
		try {
			this.tx.begin();
			DbType dbType = this.emUtil.getDbType();
			
			if (createSchema) {
				this.tx.commit();
				this.runLiquibaseChangeLog("ju-testing/data/default-changeLog.xml");
				
				// For non-MySQL DBs, we also need to create the hibernate_sequence sequence...
				if (dbType != DbType.MYSQL) {
					this.runLiquibaseChangeLog("ju-testing/data/default-changeLog-hibernateSequence.xml");
				}
				
				this.tx.begin();
			}
			
			DbDataUtil du = new DbDataUtil(emUtil);
			ImportBuilder fullData = du.buildImport().from("/ju-testing/data/default-fullData.xml");
			if (emptyTables) {
				fullData.executeDeleteAll();
			} else {
				fullData.executeCleanInsert();
			}
			
			// Load TIMEFIELD for non-oracle DBs
			if (this.emUtil.getDbType() != DbType.ORACLE && !emptyTables) {
				du.buildImport().from("/ju-testing/data/default-fullData-dataTypes.xml").executeUpdate();
			}
			
			if (resetSequences) {
				this.emUtil.resetIdentityGenerationOrSequences(1);
			}
			
			this.tx.commit();
		} finally {
			this.tx.rollbackIfNotCommitted();
		}
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
