package ch.inftec.ju.testing.db;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;

import ch.inftec.ju.db.EmfWork;

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
		this.emfWork = new EmfWorkProvider().createEmfWork();
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