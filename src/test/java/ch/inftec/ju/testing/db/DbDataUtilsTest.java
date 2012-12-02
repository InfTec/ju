package ch.inftec.ju.testing.db;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import ch.inftec.ju.testing.db.data.TestDbUtils;
import ch.inftec.ju.util.xml.XPathGetter;


/**
 * Test cases for the DbDataUtils methods.
 * @author Martin
 *
 */
public class DbDataUtilsTest extends AbstractBaseDbTest {
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
			.writeToXmlFile("writeToXmlFile_team.xml");
		
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
		Assert.assertEquals(TestDbUtils.ENTITY_TEAM_COUNT, xg.getSingleLong("count(//Team)").intValue());
	}
}
