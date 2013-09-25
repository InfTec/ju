package ch.inftec.ju.testing.db;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import ch.inftec.ju.db.JuEmUtil.DbType;
import ch.inftec.ju.testing.db.data.entity.TestingEntity;
import ch.inftec.ju.util.xml.XPathGetter;
import ch.inftec.ju.util.xml.XmlUtils;

public class DbDataUtilTest extends AbstractDbTest {
	@Test
	public void canImportData_fromDatasetFile() {
		DbDataUtil du = new DbDataUtil(this.em);
		du.prepareDefaultTestData(true, true, true);
		
		du.cleanImport("/ch/inftec/ju/testing/db/DbDataUtilTest_testingEntity.xml");
		TestingEntity te = this.em.find(TestingEntity.class, 1L);
		Assert.assertEquals("DbDataUtilTest", te.getName());
	}
	
	@Test
	public void canExportData_toXmlDocument() {
		DbDataUtil du = new DbDataUtil(this.em);
		du.prepareDefaultTestData(true, true, true);
		
		TestingEntity te = new TestingEntity();
		te.setName("Export Test");
		this.em.persist(te);
		
		Document doc = du.buildExport().addTable("TestingEntity").writeToXmlDocument();
		XPathGetter xg = new XPathGetter(doc);
		logger.debug("Exported XML\n" + XmlUtils.toString(doc, false, true));
		
		Assert.assertEquals(1, xg.getArray("//TestingEntity").length);
		Assert.assertEquals("Export Test", xg.getSingle("//TestingEntity/@NAME"));
	}
	
	@Test
	public void xmlExport_copesWithCamelCaseTable_andUsesUpperCaseColumnNames() {
		DbDataUtil du = new DbDataUtil(this.em);
		du.prepareDefaultTestData(true, true, true);
		
		TestingEntity te = new TestingEntity();
		te.setName("Export Test");
		this.em.persist(te);
		
		// Export table with camel case
		Document doc = du.buildExport().addTable("TestingEntity").writeToXmlDocument();
		XPathGetter xg = new XPathGetter(doc);
		logger.debug("Exported XML\n" + XmlUtils.toString(doc, false, true));
		
		Assert.assertEquals(1, xg.getArray("//TestingEntity").length);
		Assert.assertEquals("Export Test", xg.getSingle("//TestingEntity/@NAME"));
	}
	
	@Test
	public void xmlExport_copesWithLowerCaseTable_andUsesUpperCaseColumnNames() {
		// MySQL is case sensitive
		JuAssumeUtil.dbIsNot(this.emUtil, DbType.MYSQL);
				
		DbDataUtil du = new DbDataUtil(this.em);
		du.prepareDefaultTestData(true, true, true);
		
		TestingEntity te = new TestingEntity();
		te.setName("Export Test");
		this.em.persist(te);
		
		// Export table with camel case
		Document doc = du.buildExport().addTable("testingentity").writeToXmlDocument();
		XPathGetter xg = new XPathGetter(doc);
		logger.debug("Exported XML\n" + XmlUtils.toString(doc, false, true));
		
		Assert.assertEquals(1, xg.getArray("//testingentity").length);
		Assert.assertEquals("Export Test", xg.getSingle("//testingentity/@NAME"));
	}
	
	@Test
	public void xmlExport_copesWithUpperCaseTable_andUsesUpperCaseColumnNames() {
		// MySQL is case sensitive
		JuAssumeUtil.dbIsNot(this.emUtil, DbType.MYSQL);
		
		DbDataUtil du = new DbDataUtil(this.em);
		du.prepareDefaultTestData(true, true, true);
		
		TestingEntity te = new TestingEntity();
		te.setName("Export Test");
		this.em.persist(te);
		
		// Export table with camel case
		Document doc = du.buildExport().addTable("TESTINGENTITY").writeToXmlDocument();
		XPathGetter xg = new XPathGetter(doc);
		logger.debug("Exported XML\n" + XmlUtils.toString(doc, false, true));
		
		Assert.assertEquals(1, xg.getArray("//TESTINGENTITY").length);
		Assert.assertEquals("Export Test", xg.getSingle("//TESTINGENTITY/@NAME"));
	}
}
