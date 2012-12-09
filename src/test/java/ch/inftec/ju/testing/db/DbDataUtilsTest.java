package ch.inftec.ju.testing.db;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.xml.XPathGetter;


/**
 * Test cases for the DbDataUtils methods.
 * @author Martin
 *
 */
public class DbDataUtilsTest extends AbstractBaseDbTest {
	@Override
	protected void loadDefaultTestData() {
		this.loadDataSet(DefaultDataSet.FULL);
	}
	
	/**
	 * Tests the data export function writing DB data to an XML file.
	 */
	@Test
	public void writeToXmlFile() {
		DbDataUtil du = new DbDataUtil(dbConn);
		
		// Whole table to file
		String fileName = "writeToXmlFile_team.xml";
		File file = new File(fileName);
		if (file.exists()) Assert.fail(String.format("File %s already exists", fileName));
		du.buildExport()
			.addTable("Team", null)
			.writeToXmlFile(fileName);
		
		Assert.assertTrue(file.exists());
		file.delete();
	}
	
	/**
	 * Tests the data export function writing DB data to an XML Document.
	 */
	@Test
	public void writeToDocument() {
		DbDataUtil du = new DbDataUtil(dbConn);
		
		// Whole table to XML Document
		Document doc = du.buildExport()
			.addTable("Team", null)
			.writeToXmlDocument();
		
		XPathGetter xg = new XPathGetter(doc);
		Assert.assertEquals(2, xg.getSingleLong("count(//Team)").intValue());
	}
	
	/**
	 * Tests the data import from an XML file.
	 */
	@Test
	public void importDataFromXml() {
		Assert.assertEquals(1, em.createQuery("Select t from TestingEntity t").getResultList().size());
		
		DbDataUtil du = new DbDataUtil(dbConn);
		du.buildImport()
			.from(IOUtil.getResourceURL("DbDataUtilsTest_importDataFromXml.xml"))
			.executeCleanInsert();
		
		Assert.assertEquals(2, em.createQuery("Select t from TestingEntity t").getResultList().size());		
	}
	
	/**
	 * Asserts that the complete DB export equals the data in an XML file. 
	 */
	@Test
	public void assertEqualsAll() {
		DbDataUtil du = new DbDataUtil(dbConn);
		
//		// Can be used to create full export XML
//		du.buildExport().writeToXmlFile("completeExport.xml");
		
		du.buildAssert()
			.expected(IOUtil.getResourceURL("DbDataUtilsTest_assertEqualsAll.xml"))
			.assertEqualsAll();
	}
	
	/**
	 * Asserts that a specific table export equals the data in an XML file.
	 */
	@Test
	public void assertEqualsTables() {
		DbDataUtil du = new DbDataUtil(dbConn);
		
//		du.buildExport()
//			.addTable("team", "select * from team order by name")
//			.writeToXmlFile("teamExport.xml");
		
		du.buildAssert()
			.expected(IOUtil.getResourceURL("DbDataUtilsTest_assertEqualsTables.xml"))
			.assertEqualsTable("team", "name");
	}
}
