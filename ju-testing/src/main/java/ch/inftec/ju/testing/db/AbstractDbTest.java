package ch.inftec.ju.testing.db;

import java.net.URL;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;

import ch.inftec.ju.db.EmfWork;
import ch.inftec.ju.db.JuEmfUtil;
import ch.inftec.ju.testing.db.TestDbProvider.TestDbInfo;
import ch.inftec.ju.util.IOUtil;

/**
 * Base class for DB tests.
 * <p>
 * Provides a machanism to evaluate the test DB at runtime, thus enabling DB tests
 * targeting various DB implementations.
 * @author Martin
 *
 */
public class AbstractDbTest {
	protected EntityManager em;
	
	private EmfWork emfWork;
	
	@Before
	public void initDb() {
		// Get the TestDbProvider instance
		TestDbProvider provider = null;
		
		URL url = IOUtil.getResourceURL("META-INF/testDbProvider.impl");
		if (url != null) {
			// TODO: Implement
		} else {
			 provider = new TestDbProviderDerby();
		}
		
		String persistenceUnitName = "ju-testing pu-test";
		TestDbInfo testDbInfo = provider.getTestDbInfo(persistenceUnitName);
		
		JuEmfUtil emfUtil = JuEmfUtil.create()
			.persistenceUnitName(persistenceUnitName)
			.connectionUrl(testDbInfo.getConnectionUrl())
			.build();
		
		this.emfWork = emfUtil.startWork();
		this.em = this.emfWork.getEm();
	}
	
	@After
	public void cleanupDb() {
		if (this.emfWork != null) {
			this.emfWork.close();
			this.emfWork = null;
			this.em = null;
		}
	}
}