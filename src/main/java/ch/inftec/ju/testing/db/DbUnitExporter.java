package ch.inftec.ju.testing.db;

import java.io.FileOutputStream;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbConnectionFactory;
import ch.inftec.ju.db.DbConnectionFactoryLoader;
import ch.inftec.ju.util.JuRuntimeException;

public class DbUnitExporter {
	public static void main(String[] args) {
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
	
	/*
    public static void main2(String[] args) throws Exception
    {
        // database connection
        Class driverClass = Class.forName("org.hsqldb.jdbcDriver");
        Connection jdbcConnection = DriverManager.getConnection(
                "jdbc:hsqldb:sample", "sa", "");
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

        // partial database export
        QueryDataSet partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("FOO", "SELECT * FROM TABLE WHERE COL='VALUE'");
        partialDataSet.addTable("BAR");
        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("partial.xml"));

        // full database export
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("full.xml"));
        
        // dependent tables database export: export table X and all tables that
        // have a PK which is a FK on X, in the right order for insertion
        String[] depTableNames = 
          TablesDependencyHelper.getAllDependentTables( connection, "X" );
        IDataSet depDataset = connection.createDataSet( depTableNames );
        FlatXmlDataSet.write(depDataset, new FileOutputStream("dependents.xml"));          
        
    }
    */
}
