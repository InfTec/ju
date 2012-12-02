package ch.inftec.ju.testing.db;

import org.junit.Test;

import ch.inftec.ju.db.AbstractBaseDbTest;

/**
 * Test cases for the DbDataUtils methods.
 * @author Martin
 *
 */
public abstract class AbstractDbDataUtilsTest extends AbstractBaseDbTest {
	/**
	 * Tests the data export function writing DB data to an XML file.
	 */
	@Test
	public void writeToXmlFile() {
		DbDataUtil du = new DbDataUtil(dbConn);
		
		// Whole table
		du.buildExport()
			.addTable("Team", null)
			.writeToXmlFile("writeToXmlFile_team.xml");
	}
}
