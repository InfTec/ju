package ch.inftec.ju.testing.db;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
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
	
	/**
	 * Creates a new DbDataUtil instance using the specified DbConnection.
	 * @param dbConnection DbConnection instance
	 */
	public DbDataUtil(DbConnection dbConnection) {
		this.dbConnection = dbConnection;
	}
	
	/**
	 * Returns a new ExportBuilder to configure and execute DB data exports.
	 * @return ExportBuilder instance
	 */
	public ExportBuilder buildExport() {
		return new ExportBuilder(dbConnection);
	}
	
	/**
	 * Builder class to configure and execute DB data exports.
	 * @author Martin
	 *
	 */
	public static class ExportBuilder {
		//private final DbConnection dbConnection;
		private final QueryDataSet queryDataSet;
		
		private ExportBuilder(DbConnection dbConnection) {
			//this.dbConnection = dbConnection;
			
			try {
				IDatabaseConnection connection = new DatabaseConnection(dbConnection.getConnection());
				this.queryDataSet = new QueryDataSet(connection);
			} catch (Exception ex) {
				throw new JuDbException("Couldn't initialize DB", ex);
			}
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
