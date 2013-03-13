package ch.inftec.ju.testing.db;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;

import org.dbunit.Assertion;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.w3c.dom.Document;

import ch.inftec.ju.db.ConnectionInfo;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.ReflectUtils;
import ch.inftec.ju.util.XString;
import ch.inftec.ju.util.xml.XmlOutputConverter;

/**
 * Utility class containing methods to import and export data from a DB.
 * <p>
 * The class needs a DbConnection instance to work with. Make sure the connection
 * is available when using the util class.
 * @author Martin
 *
 */
public class DbDataUtil {
	private final IDatabaseConnection iConnection;
	
	/**
	 * Creates a new DbDataUtil instance using the specifiec Connection.
	 * <p>
	 * If you need to specify a DB Schema, use the DbDataUtil(Connection, String) constructor.
	 * @param connection Connection instance
	 */
	public DbDataUtil(Connection connection) {
		this(connection, (String)null);
	}
	
	public DbDataUtil(Connection connection, String schema) {
		try {
			this.iConnection = new DatabaseConnection(connection, schema);
		} catch (Exception ex) {
			throw new JuDbException("Couldn't initialize DatabaseConnection", ex);
		}
	}
	
	/**
	 * Creates a new DbDataUtil instance using the specified Connection and the Schema
	 * from the ConnectionInfo
	 * @param connection Connection instance
	 * @param ConnectionInfo to get the Schema to use
	 */
	public DbDataUtil(Connection connection, ConnectionInfo connectionInfo) {
		try {
			this.iConnection = new DatabaseConnection(connection, connectionInfo.getSchema());
		} catch (Exception ex) {
			throw new JuDbException("Couldn't initialize DatabaseConnection", ex);
		}
	}
	
	/**
	 * Returns a new ExportBuilder to configure and execute DB data exports.
	 * @return ExportBuilder instance
	 */
	public ExportBuilder buildExport() {
		return new ExportBuilder(iConnection);
	}
	
	/**
	 * Returns a new ImportBuilder to import data from XML resources into the DB.
	 * @return ImportBuilder instance
	 */
	public ImportBuilder buildImport() {
		return new ImportBuilder(iConnection);
	}
	
	/**
	 * Returns a new AssertBuilder to assert that table data equals expected data
	 * specified in an XML file.
	 * @return AssertBuilder instance
	 */
	public AssertBuilder buildAssert() {
		return new AssertBuilder(iConnection);
	}
	
	/**
	 * Builder class to configure and execute DB data exports.
	 * @author Martin
	 *
	 */
	public static class ExportBuilder {
		private final IDatabaseConnection iConnection;
		private QueryDataSet queryDataSet;
		
		private ExportBuilder(IDatabaseConnection iConnection) {
			this.iConnection = iConnection;			
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
		 * If no query is specified, all table data is exported. Otherwise, only
		 * the data returned by the query is exported.
		 * @param tableName TableName
		 * @param query Optional query to select sub data
		 * @return ExportBuilder to allow for chaining
		 */
		public ExportBuilder addTable(String tableName, String query) {
			try {
				if (queryDataSet == null) {
					queryDataSet = new QueryDataSet(iConnection);
				}
				
				if (query == null) {
					queryDataSet.addTable(tableName);
				} else {
					queryDataSet.addTable(tableName, query);
				}
				return this;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't add table", ex);
			}
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

		private IDataSet getExportSet() {
			if (queryDataSet != null) {
				return queryDataSet;
			} else {
				try {
					return iConnection.createDataSet();
				} catch (Exception ex) {
					throw new JuDbException("Couldn't create DataSet from DB");
				}
			}
		}
		
		/**
		 * Writes the DB data to an (in-memory) XML Document.
		 * @return Xml Document instance
		 */
		public Document writeToXmlDocument() {
			try {
				XmlOutputConverter xmlConv = new XmlOutputConverter();
				FlatXmlDataSet.write(this.getExportSet(), xmlConv.getOutputStream());
				
				return xmlConv.getDocument();
			} catch (Exception ex) {
				throw new JuDbException("Couldn't write DB data to XML document", ex);
			}
		}
		
		/**
		 * Write the DB data to an XML file.
		 * @param fileName Path of the file
		 */
		public void writeToXmlFile(String fileName) {
			try (OutputStream stream = new BufferedOutputStream(
							new FileOutputStream(fileName))) {

				FlatXmlDataSet.write(this.getExportSet(), stream);
			} catch (Exception ex) {
				throw new JuDbException("Couldn't write DB data to file " + fileName, ex);
			}
		}		
	}
	
	/**
	 * Builder class to configure and execute DB data imports.
	 * @author Martin
	 *
	 */
	public static class ImportBuilder {
		private final IDatabaseConnection iConnection;
		private FlatXmlDataSet flatXmlDataSet;
		
		private ImportBuilder(IDatabaseConnection iConnection) {
			this.iConnection = iConnection;
		}
		
		/**
		 * Imports DB data from the specified XML
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
					.build(xmlUrl);
				return this;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't import data from XML: xmlUrl", ex);
			}
		}
		
		/**
		 * Performs a clean import of the data into the DB, i.e. cleans any existing
		 * data in affected tables and imports the rows specified in in this builder.
		 */
		public void executeCleanInsert() {
			try {
				DatabaseOperation.CLEAN_INSERT.execute(iConnection, flatXmlDataSet);
			} catch (Exception ex) {
				throw new JuDbException("Couldnt clean and insert data into DB", ex);
			}
		}
		
		/**
		 * Truncates all tables included in the data set.
		 */
		public void executeDeleteAll() {
			try {
				DatabaseOperation.DELETE_ALL.execute(iConnection, flatXmlDataSet);
			} catch (Exception ex) {
				throw new JuDbException("Couldnt truncate data in DB", ex);
			}
		}
		
		/**
		 * Performs an import of the data into the DB, without cleaning any data
		 * previously.
		 */
		public void executeInsert() {
			try {
				DatabaseOperation.INSERT.execute(iConnection, flatXmlDataSet);
			} catch (Exception ex) {
				throw new JuDbException("Couldnt insert data into DB", ex);
			}
		}
	}	
	
	/**
	 * Builder class to configure and execute DB data asserts.
	 * @author Martin
	 *
	 */
	public static class AssertBuilder {
		private final IDatabaseConnection iConnection;
		private FlatXmlDataSet flatXmlDataSet;
		
		private AssertBuilder(IDatabaseConnection iConnection) {
			
			this.iConnection = iConnection;
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
			try {
				IDataSet  dbDataSet = iConnection.createDataSet();
				Assertion.assertEquals(flatXmlDataSet, dbDataSet);
			} catch (Exception ex) {
				throw new JuDbException("Couldn't assert DB data", ex);
			}
		}
		
		/**
		 * Asserts that the export from the specified table equals the expected data.
		 * @param tableName Name of the table to assert
		 * @param orderColumnName Name of the column to order data by for the export
		 */
		public void assertEqualsTable(String tableName, String orderColumnName) {
			try {
				QueryDataSet tableDataSet = new QueryDataSet(iConnection);
				tableDataSet.addTable(tableName, String.format("select * from %s order by %s", tableName, orderColumnName));
				
				Assertion.assertEquals(flatXmlDataSet, tableDataSet);
			} catch (Exception ex) {
				throw new JuDbException("Couldn't assert DB data", ex);
			}
		}
		
	}
}
