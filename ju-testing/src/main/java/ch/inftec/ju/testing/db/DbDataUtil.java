package ch.inftec.ju.testing.db;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DefaultMetadataHandler;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.IMetadataHandler;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.jdbc.Work;
import org.w3c.dom.Document;

import ch.inftec.ju.db.ConnectionInfo;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.db.JuEmUtil.DbType;
import ch.inftec.ju.util.DataHolder;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuCollectionUtils;
import ch.inftec.ju.util.ReflectUtils;
import ch.inftec.ju.util.XString;
import ch.inftec.ju.util.xml.XPathGetter;
import ch.inftec.ju.util.xml.XmlOutputConverter;
import ch.inftec.ju.util.xml.XmlUtils;

/**
 * Utility class containing methods to import and export data from a DB.
 * <p>
 * The class needs a DbConnection instance to work with. Make sure the connection
 * is available when using the util class.
 * @author Martin
 *
 */
public class DbDataUtil {
	private final Connection connection;
	private final JuEmUtil emUtil;
	
	private String schemaName = null;
	
	private Map<String, Object> configProperties = new HashMap<>();

	/**
	 * Creates a new DbDataUtil instance using the specifiec Connection.
	 * <p>
	 * If you need to specify a DB Schema, use the DbDataUtil(Connection, String) constructor.
	 * @param connection Connection instance
	 * @Deprecated Use constructor with EntityManager
	 */
	@Deprecated
	public DbDataUtil(Connection connection) {
		this(connection, (String)null);
	}
	
	/**
	   @Deprecated Use constructor with EntityManager
	 */
	@Deprecated
	public DbDataUtil(Connection connection, String schema) {
		this.emUtil = null;
		this.connection = connection;
		this.schemaName = schema;
	}
	
	/**
	 * Creates a new DbDataUtil instance using the specified Connection and the Schema
	 * from the ConnectionInfo
	 * @param connection Connection instance
	 * @param ConnectionInfo to get the Schema to use
	 * @Deprecated Use constructor with EntityManager
	 */
	@Deprecated
	public DbDataUtil(Connection connection, ConnectionInfo connectionInfo) {
		this(connection, connectionInfo.getSchema());
	}
	
	/**
	 * Create a new DbDataUtil that will use the specified EntityManager to get
	 * a raw connection to the DB and execute SQL queries.
	 * @param em EntityManager instance to execute SQL in a JDBC connection
	 */
	public DbDataUtil(EntityManager em) {
		this(new JuEmUtil(em));
	}
	
	/**
	 * Create a new DbDataUtil that will use the specified EntityManager to get
	 * a raw connection to the DB and execute SQL queries.
	 * @param emUtil JuEmUtil wrapping an EntityManager instance to execute SQL in a JDBC connection
	 */
	public DbDataUtil(JuEmUtil emUtil) {
		this.emUtil = emUtil;
		this.connection = null;

		// Initialize
		DefaultDataTypeFactory dataTypeFactory = null;
		IMetadataHandler metadataHandler = new DefaultMetadataHandler();
		switch (this.emUtil.getDbType()) {
		case DERBY:
			dataTypeFactory = new DefaultDataTypeFactory();
			break;
		case H2:
			dataTypeFactory = new H2DataTypeFactory();
			break;
		case MYSQL:
			dataTypeFactory = new MySqlDataTypeFactory();
			metadataHandler = new MySqlMetadataHandler();
			break;
		case ORACLE:
			this.setSchema(this.emUtil.getMetaDataUserName());
			dataTypeFactory = new Oracle10DataTypeFactory();
			break;
		default:
			throw new JuDbException("Unsupported DB: " + this.emUtil.getDbType());
		}
		
		this.setConfigProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, dataTypeFactory);
		this.setConfigProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, metadataHandler);
	}
	
	/**
	 * Loads the default test data (Player, Team, TestingEntity...), creating the
	 * DB Schema as well.
	 */
	public void prepareDefaultTestData() {
		this.prepareDefaultTestData(false, false, true);
	}
	
	/**
	 * Loads the default test data (Player, Team, TestingEntity, ...), making
	 * sure that the tables have been created using Liquibase.
	 * @param emptyTables If true, the default tables will be cleaned
	 * @param resetSequences If true, sequences (or identity columns) will be reset to 1
	 * @param createSchema If true, the Schema will be created (or verified) using Liquibase
	 */
	public void prepareDefaultTestData(boolean emptyTables, boolean resetSequences, boolean createSchema) {
		if (createSchema) {
			DbSchemaUtil su = new DbSchemaUtil(this.emUtil);
			su.runLiquibaseChangeLog("ju-testing/data/default-changeLog.xml");
			
			// For non-MySQL DBs, we also need to create the hibernate_sequence sequence...
			if (this.emUtil.getDbType() != DbType.MYSQL) {
				su.runLiquibaseChangeLog("ju-testing/data/default-changeLog-hibernateSequence.xml");
			}
		}
		
		ImportBuilder fullData = this.buildImport().from("/ju-testing/data/default-fullData.xml");
		if (emptyTables) {
			fullData.executeDeleteAll();
		} else {
			fullData.executeCleanInsert();
		}
		
		// Load TIMEFIELD for non-oracle DBs
		if (this.emUtil.getDbType() != DbType.ORACLE && !emptyTables) {
			this.buildImport().from("/ju-testing/data/default-fullData-dataTypes.xml").executeUpdate();
		}
		
		if (resetSequences) {
			this.emUtil.resetIdentityGenerationOrSequences(1);
		}
		
	}
	
	/**
	 * Sets the DB schema name to work with.
	 * <p>
	 * May be necessary for DBs like oracle to avoid duplicate name problems.
	 * @param schemaName DB schema name
	 * @return This util to allow for chaining
	 */
	public DbDataUtil setSchema(String schemaName) {
		this.schemaName = schemaName;
		return this;
	}
	
	/**
	 * Sets a config attribute of the underlying DbUnit IDatabaseConnection instance.
	 * @param name Name of the attribute
	 * @param value Value of the attribute
	 * @return This instance to allow for chaining
	 */
	public DbDataUtil setConfigProperty(String name, Object value) {
		this.configProperties.put(name, value);
		return this;
	}
	
	private void execute(final DbUnitWork work) {
		if (this.connection != null) {
			this.doExecute(this.connection, work);
		} else if (this.emUtil != null) {
			this.emUtil.doWork(new Work() {
				@Override
				public void execute(Connection connection) throws SQLException {
					doExecute(connection, work);
				}
			});
		} else {
			throw new IllegalStateException("DbDataUtil hasn't been initialized correctly");
		}
	}
	
	private void doExecute(Connection connection, DbUnitWork work) {
		/**
		 * Due to a JDBC 1.4 spec imcompatibility of the Oracle driver
		 * (doesn't return IS_AUTOINCREMENT in table meta data), we need
		 * to unwrap the actual JDBC connection in case this is a (Hibernate)
		 * proxy.
		 */
		Connection unwrappedConn = null;
		if (this.emUtil.getDbType() == DbType.ORACLE && connection instanceof Proxy) {
			try {
				unwrappedConn = connection.unwrap(Connection.class);
			} catch (Exception ex) {
				throw new JuDbException("Couldn't unwrap Connection", ex);
			}
		}
		final Connection realConn = unwrappedConn != null
				? unwrappedConn
				: connection;
		
		try {
			IDatabaseConnection conn = new DatabaseConnection(realConn, this.schemaName);
			for (String key : this.configProperties.keySet()) {
				conn.getConfig().setProperty(key, this.configProperties.get(key));
			}
			work.execute(conn);
		} catch (DatabaseUnitException ex) {
			throw new JuDbException("Couldn't execute DbUnitWork", ex);
		}
	}
	
	/**
	 * Shortcut to execute a clean import from a dataset resource file.
	 * @param resourcePath Path to dataset resource
	 */
	public void cleanImport(String resourcePath) {
		this.buildImport().from(resourcePath).executeCleanInsert();
	}
	
	/**
	 * Returns a new ExportBuilder to configure and execute DB data exports.
	 * @return ExportBuilder instance
	 */
	public ExportBuilder buildExport() {
		return new ExportBuilder(this);
	}
	
	/**
	 * Returns a new ImportBuilder to import data from XML resources into the DB.
	 * @return ImportBuilder instance
	 */
	public ImportBuilder buildImport() {
		return new ImportBuilder(this);
	}
	
	/**
	 * Returns a new AssertBuilder to assert that table data equals expected data
	 * specified in an XML file.
	 * @return AssertBuilder instance
	 */
	public AssertBuilder buildAssert() {
		return new AssertBuilder(this);
	}
	
	/**
	 * Helper callback interface to execute code that needs a IDatabaseConnection
	 * instance.
	 * @author tgdmemae
	 * <T> Return value
	 *
	 */
	private static interface DbUnitWork {
		public void execute(IDatabaseConnection conn);
	}
	
	/**
	 * Builder class to configure and execute DB data exports.
	 * @author Martin
	 *
	 */
	public static class ExportBuilder {
		private final DbDataUtil dbDataUtil;
		
		private final List<ExportItem> exportItems = new ArrayList<>();
		
		/**
		 * List that may contain table names the way we are supposed to write them (casing)
		 */
		private List<String> casedTableNames = new ArrayList<>();
		
		private ExportBuilder(DbDataUtil dbDataUtil) {
			this.dbDataUtil = dbDataUtil;	
		}
		
		/**
		 * Loads the table names from the specified dataset XML resource and uses it as a template
		 * of how to case any table name that will be exported.
		 * <p>
		 * Note that calling this method doesn't actually ADD a table.
		 * @param resourcePath Resource path to dataset XML
		 * @return ExportBuilder to allow for chaining
		 */
		public ExportBuilder setTableNamesCasingByDataSet(String resourcePath) {
			try {
				this.casedTableNames = new XPathGetter(XmlUtils.loadXml(IOUtil.getResourceURL(resourcePath))).getNodeNames("dataset/*");
			} catch (Exception ex) {
				throw new JuDbException("Couldn't load table names data set " + resourcePath, ex);
			}
			return this;
		}
		
		/**
		 * Adds the specific table to the builder, exporting the table data.
		 * @param tableName Table name
		 * @return ExportBuilder to allow for chaining
		 */
		public ExportBuilder addTable(String tableName) {
			return this.addTable(tableName, null);
		}
		
		/**
		 * Adds the specified table to the builder, exporting the table data.
		 * <p>
		 * If no query is specified (null), all table data is exported. Otherwise, only
		 * the data returned by the query is exported.
		 * <p>
		 * The query has to be a full SQL query like <code>select * from table where id=7</code>
		 * @param tableName TableName
		 * @param query Optional query to select sub data
		 * @return ExportBuilder to allow for chaining
		 */
		public ExportBuilder addTable(final String tableName, final String query) {
			this.exportItems.add(new TableQueryExportItem(tableName, query));
			
			return this;
		}
		
		/**
		 * Adds the data of the specified table, ordering by the specified columns.
		 * @param tableName Table names
		 * @param orderColumns List of columns to order by
		 * @return ExportBuilder to allow for chaining
		 */
		public ExportBuilder addTableSorted(String tableName, String... orderColumns) {
			if (orderColumns.length == 0) {
				return this.addTable(tableName);
			} else {
				XString xs = new XString();
				xs.addFormatted("SELECT * FROM %s ORDER BY ", tableName);
				for (String orderColumn : orderColumns) {
					xs.assertText("ORDER BY ", ", ");
					xs.addText(orderColumn);
				}
				
				return this.addTable(tableName, xs.toString());
			}
		}

		/**
		 * Adds the data of the tables contained in the specified data set.
		 * <p>
		 * It doesn't matter what kind of dataset we got, we're just extracting the table names.
		 * @param resourcePath
		 * @return
		 */
		public ExportBuilder addTablesByDataSet(String resourcePath) {
			try {
				Set<String> tableNames = JuCollectionUtils.asSameOrderSet(new XPathGetter(XmlUtils.loadXml(IOUtil.getResourceURL(resourcePath))).getNodeNames("dataset/*"));
				for (String tableName : tableNames) {
					this.addTable(tableName);
				}
				
				return this;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't add tables by dataset " + resourcePath, ex);
			}
		}
		
		private void doWork(final DataSetWork dataSetWork) {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					if (exportItems.size() > 0) {
						QueryDataSet dataSet = new QueryDataSet(conn, false);
						for (ExportItem item : exportItems) {
							item.addToQueryDataSet(dataSet, casedTableNames);
						}
						dataSetWork.execute(dataSet);
					} else {
						// Export whole DB
						try {
							IDataSet dataSet = conn.createDataSet();
							dataSetWork.execute(dataSet);
						} catch (Exception ex) {
							throw new JuDbException("Couldn't export whole DB");
						}
					}
				};
			});
		}
		
		/**
		 * Writes the DB data to an (in-memory) XML Document.
		 * @return Xml Document instance
		 */
		public Document writeToXmlDocument() {
			final DataHolder<Document> doc = new DataHolder<>();
			
			this.doWork(new DataSetWork() {
				@Override
				public void execute(IDataSet dataSet) {
					try {
						XmlOutputConverter xmlConv = new XmlOutputConverter();
						ExportBuilder.writeToXml(dataSet, xmlConv.getOutputStream());
						doc.setValue(xmlConv.getDocument());
					} catch (Exception ex) {
						throw new JuDbException("Couldn't write DB data to XML document", ex);
					}
				}
			});
			
			return doc.getValue();
		}
		
		/**
		 * Write the DB data to an XML file.
		 * @param fileName Path of the file
		 */
		public void writeToXmlFile(final String fileName) {
			try (final OutputStream stream = new BufferedOutputStream(
							new FileOutputStream(fileName))) {

				this.doWork(new DataSetWork() {
					 @Override
					public void execute(IDataSet dataSet) {
						try {
							ExportBuilder.writeToXml(dataSet, stream);
						} catch (Exception ex) {
							throw new JuDbException("Couldn't write DB data to file " + fileName, ex);
						}
					}
				});
			} catch (Exception ex) {
				throw new JuDbException("Couldn't write DB data to file " + fileName, ex);
			}
		}
		
		private interface DataSetWork {
			void execute(IDataSet dataSet);
		}
		
		private interface ExportItem {
			void addToQueryDataSet(QueryDataSet dataSet, List<String> casedTableNames);
		}
		
		private static class TableQueryExportItem implements ExportItem {
			private final String tableName;
			private final String query;
			
			private TableQueryExportItem(String tableName, String query) {
				this.tableName = tableName;
				this.query = query;
			}
			
			@Override
			public void addToQueryDataSet(QueryDataSet dataSet, List<String> casedTableNames) {
				try {
					String actualTableName = this.tableName;
					for (String casedTableName : casedTableNames) {
						if (casedTableName.equalsIgnoreCase(this.tableName)) {
							actualTableName = casedTableName;
							break;
						}
					}
					
					dataSet.addTable(actualTableName, this.query);
				} catch (AmbiguousTableNameException ex) {
					throw new JuDbException(String.format("Couldn't add table %s to QueryDataSet: %s", this.tableName, this.query), ex);
				}
			}
		}
		
		/**
		 * Custom implementation of FlatXmlDataSet.write so we can enforce column casing
		 * @param dataSet
		 * @param out
		 * @throws IOException
		 * @throws DataSetException
		 */
		private static void writeToXml(IDataSet dataSet, OutputStream out) throws IOException, DataSetException {
			CaseAwareFlatXmlWriter datasetWriter = new CaseAwareFlatXmlWriter(out);
	        datasetWriter.setIncludeEmptyTable(true);
	        datasetWriter.write(dataSet);
		}
	}
	
	/**
	 * Builder class to configure and execute DB data imports.
	 * @author Martin
	 *
	 */
	public static class ImportBuilder {
		private final DbDataUtil dbDataUtil;
		private FlatXmlDataSet flatXmlDataSet;
		
		private ImportBuilder(DbDataUtil dbDataUtil) {
			this.dbDataUtil = dbDataUtil;	
		}
		
		/**
		 * Imports DB data from the specified XML.
		 * <p>
		 * Only one 'from' is possible per import.
		 * @param resourcePath Resource path, either absolute or relative to the current class
		 * @return ImportBuilder
		 */
		public ImportBuilder from(String resourcePath) {
			URL url = IOUtil.getResourceURL(resourcePath, ReflectUtils.getCallingClass());
			return from(url);
		}
		
		/**
		 * Imports DB data from the specified XML
		 * @param xmlUrl URL to XML file location
		 */
		public ImportBuilder from(URL xmlUrl) {
			try {
				flatXmlDataSet = new FlatXmlDataSetBuilder()
					.setColumnSensing(true)
					.setCaseSensitiveTableNames(false)
					.build(xmlUrl);
				return this;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't import data from XML: " + xmlUrl, ex);
			}
		}
		
		/**
		 * Performs a clean import of the data into the DB, i.e. cleans any existing
		 * data in affected tables and imports the rows specified in in this builder.
		 */
		public void executeCleanInsert() {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						DatabaseOperation.CLEAN_INSERT.execute(conn, flatXmlDataSet);
					} catch (Exception ex) {
						throw new JuDbException("Couldnt clean and insert data into DB", ex);
					}
				}
			});
		}
		
		/**
		 * Truncates all tables included in the data set.
		 */
		public void executeDeleteAll() {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						DatabaseOperation.DELETE_ALL.execute(conn, flatXmlDataSet);
					} catch (Exception ex) {
						throw new JuDbException("Couldnt truncate data in DB", ex);
					}
				};
			});
		}
		
		/**
		 * Performs an import of the data into the DB, without cleaning any data
		 * previously.
		 */
		public void executeInsert() {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						DatabaseOperation.INSERT.execute(conn, flatXmlDataSet);
					} catch (Exception ex) {
						throw new JuDbException("Couldnt insert data into DB", ex);
					}
				};
			});
			
		}
		
		/**
		 * Performs an update of the existing data in the DB, without inserting new data.
		 */
		public void executeUpdate() {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						DatabaseOperation.UPDATE.execute(conn, flatXmlDataSet);
					} catch (Exception ex) {
						throw new JuDbException("Couldnt update data in DB", ex);
					}
				};
			});
			
		}
	}	
	
	/**
	 * Builder class to configure and execute DB data asserts.
	 * @author Martin
	 *
	 */
	public static class AssertBuilder {
		private final DbDataUtil dbDataUtil;
		private FlatXmlDataSet flatXmlDataSet;
		
		private AssertBuilder(DbDataUtil dbDataUtil) {
			this.dbDataUtil = dbDataUtil;	
		}
		
		/**
		 * URL to XML of expected data.
		 * @param xmlUrl URL to XML file location
		 * @return This builder to allow for chaining
		 */
		public AssertBuilder expected(URL xmlUrl) {
			try {
				flatXmlDataSet = new FlatXmlDataSetBuilder().build(xmlUrl);
				return this;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't import data from XML: xmlUrl", ex);
			}
		}
		
		/**
		 * Asserts that the whole data set in the DB equals the expected data.
		 */
		public void assertEqualsAll() {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						IDataSet  dbDataSet = conn.createDataSet();
						Assertion.assertEquals(flatXmlDataSet, dbDataSet);
					} catch (Exception ex) {
						throw new JuDbException("Couldn't assert DB data", ex);
					}
				}
			});
		}
		
		/**
		 * Asserts that the export from the specified table equals the expected data.
		 * @param tableName Name of the table to assert
		 * @param orderColumnName Name of the column to order data by for the export
		 */
		public void assertEqualsTable(final String tableName, final String orderColumnName) {
			this.dbDataUtil.execute(new DbUnitWork() {
				@Override
				public void execute(IDatabaseConnection conn) {
					try {
						QueryDataSet tableDataSet = new QueryDataSet(conn);
						tableDataSet.addTable(tableName, String.format("select * from %s order by %s", tableName, orderColumnName));
						
						Assertion.assertEquals(flatXmlDataSet, tableDataSet);
					} catch (Exception ex) {
						throw new JuDbException("Couldn't assert DB data", ex);
					}
				}
			});
		}
	}
}
