package ch.inftec.ju.db.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.DbConnectionFactory;
import ch.inftec.ju.db.DbConnectionFactoryLoader;
import ch.inftec.ju.db.data.entity.CustomObject;
import ch.inftec.ju.util.JuCollectionUtils;
import ch.inftec.ju.util.TestUtils;

/**
 * Test class for DbConnection related classes.
 * @author Martin
 *
 */
public class DbConnectionTest {
	
	/**
	 * Tests the creation of a DbConnectionFactory using the default connection.xml path.
	 */
	@Test
	public void connectionFactoryFromXml() throws Exception {
		DbConnectionFactory factory = DbConnectionFactoryLoader.createInstance();
		
		// Test available connections
		Assert.assertEquals(3, factory.getAvailableConnections().size());
		TestUtils.assertCollectionEquals(
				JuCollectionUtils.arrayList("ESW MyTTS", "Derby InMemory-DB", "PU_mementoObject"), 
				factory.getAvailableConnections());
				
		// Test flags
		Assert.assertEquals(2, factory.getAvailableConnections("connection").size());
		List<String> eswConnections = factory.getAvailableConnections("esw");
		Assert.assertEquals(1, eswConnections.size());
		Assert.assertEquals("ESW MyTTS", eswConnections.get(0));
		eswConnections = factory.getAvailableConnections("esw", "connection");
		Assert.assertEquals(1, eswConnections.size());
		Assert.assertEquals("ESW MyTTS", eswConnections.get(0));
		
		// Try getting a connection
		
		try (DbConnection derbyConnection = factory.openDbConnection("Derby InMemory-DB")) {
			Assert.assertEquals("Derby InMemory-DB", derbyConnection.getName());
		}
	}
	
	/**
	 * Tests the creation of a DbConnectionFactory using a custom persistence.xml path.
	 */
	@Test
	public void connectionFactoryFromCustomXml() throws Exception {
		DbConnectionFactory factory = DbConnectionFactoryLoader.createInstance("/META-INF/customPersistence.xml");
		
		// Try getting a connection
		
		try (DbConnection derbyConnection = factory.openDbConnection("CustomDerby")) {
			Assert.assertEquals("CustomDerby", derbyConnection.getName());
			
			// Store CustomObject
			EntityManager em = derbyConnection.getEntityManager();
			CustomObject co = new CustomObject();
			co.setText("Test");
			em.persist(co);
			em.flush();
		}
	}
}
