package ch.inftec.ju.testing.db;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.eclipse.persistence.internal.oxm.record.DOMInputSource;
import org.w3c.dom.Document;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbConnectionFactory;
import ch.inftec.ju.db.DbConnectionFactoryLoader;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.xml.XmlOutputConverter;

/**
 * Utility class containing methods to import and export data from a DB.
 * <p>
 * The class needs a DbConnection instance to work with. Make sure the connection
 * is avaiable when using the util class.
 * @author Martin
 *
 */
public class DbDataUtil {
	private final DbConnection dbConnection;
	private final IDatabaseConnection iConnection;
	
	/**
	 * Creates a new DbDataUtil instance using the specified DbConnection.
	 * @param dbConnection DbConnection instance
	 */
	public DbDataUtil(DbConnection dbConnection) {
		this.dbConnection = dbConnection;
		try {
			this.iConnection = new DatabaseConnection(dbConnection.getConnection());
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
	 * Builder class to configure and execute DB data exports.
	 * @author Martin
	 *
	 */
	public static class ExportBuilder {
		//private final DbConnection dbConnection;
		private final QueryDataSet queryDataSet;
		
		private ExportBuilder(IDatabaseConnection iConnection) {
			this.queryDataSet = new QueryDataSet(iConnection);
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
				if (query == null) {
					this.queryDataSet.addTable(tableName);
				} else {
					this.queryDataSet.addTable(tableName, query);
				}
				return this;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't add table", ex);
			}
		}		

		/**
		 * Writes the DB data to an (in-memory) XML Document.
		 * @return Xml Document instance
		 */
		public Document writeToXmlDocument() {
			try {
				XmlOutputConverter xmlConv = new XmlOutputConverter();
				FlatXmlDataSet.write(this.queryDataSet, xmlConv.getOutputStream());
				
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

				FlatXmlDataSet.write(this.queryDataSet, stream);
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
		 * @param xmlUrl URL to XML file location
		 */
		public ImportBuilder from(URL xmlUrl) {
			try {
				flatXmlDataSet = new FlatXmlDataSetBuilder().build(xmlUrl);
				return this;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't import data from XML: xmlUrl", ex);
			}
		}
		
		/**
		 * Imports DB data from the specified XML Document
		 * @param xmlUrl URL to XML file location
		 */
		public ImportBuilder from(Document doc) {
			try {
				flatXmlDataSet = new FlatXmlDataSetBuilder().build(new DOMInputSource(doc));
				return this;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't import data from XML Document", ex);
			}
		}
		
		/**
		 * Performs a clean import of the data into the DB, i.e. cleans any existing
		 * data and imports the data.
		 */
		public void cleanImport() {
			try {
				DatabaseOperation.CLEAN_INSERT.execute(iConnection, flatXmlDataSet);
			} catch (Exception ex) {
				throw new JuDbException("Couldnt clean and insert data into DB", ex);
			}
		}
	}
	
	public void exportDataToXml() {
		DbConnectionFactory factory = DbConnectionFactoryLoader.createInstance();
		try (DbConnection dbConn = factory.openDbConnection("ESW Localdev")) {
			IDatabaseConnection connection = new DatabaseConnection(dbConn.getConnection());
			
			// partial database export
	        QueryDataSet partialDataSet = new QueryDataSet(connection);
	        //partialDataSet.addTable("FOO", "SELECT * FROM TABLE WHERE COL='VALUE'");
	        partialDataSet.addTable("CONTACTROLE");
	        partialDataSet.addTable("ATTRIBUTEVALUE");
	        partialDataSet.addTable("ATTRIBUTETYPE");
	        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("esw.xml"));
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't export database contents", ex);
		}
	}
}
