package ch.inftec.ju.testing.db;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.EmfWork;
import ch.inftec.ju.db.JuEmUtil;

/**
 * Base class for DB tests.
 * <p>
 * Provides a machanism to evaluate the test DB at runtime, thus enabling DB tests
 * targeting various DB implementations.
 * @author Martin
 *
 */
public class AbstractDbTest {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected EntityManager em;
	protected JuEmUtil emUtil;
	
	private EmfWork emfWork;
	
	@Before
	public void initDb() {
		this.emfWork = new EmfWorkProvider().createEmfWork();
		this.em = this.emfWork.getEm();
		this.emUtil = new JuEmUtil(this.em);
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