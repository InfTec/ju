package ch.inftec.ju.testing.db;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
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
	
	/**
	 * Rule to initialize DB fields. We need to use a rule so we can evaluate the method
	 * annotation.
	 */
	@Rule
	public DbInitializerRule dbInitializer = new DbInitializerRule();
	
	@After
	public void cleanupDb() {
		if (this.emfWork != null) {
			this.emfWork.close();
			this.emfWork = null;
			this.em = null;
		}
	}
	
	private class DbInitializerRule implements MethodRule {
		@Override
		public Statement apply(Statement base, FrameworkMethod method,
				Object target) {
			
			AbstractDbTest dbTest = (AbstractDbTest) target;
			
			// Evaluate Persistence Unit name
			String persistenceUnit = "ju-pu-test";
			String profile = null;
			
			// Check if the persistenceUnit is overwritten by an annotation (method overrules
			// class annotation)
			JuDbTest juDbTest = method.getAnnotation(JuDbTest.class);
			if (juDbTest == null) {
				juDbTest = target.getClass().getAnnotation(JuDbTest.class);
			}
			if (juDbTest != null) {
				persistenceUnit = juDbTest.persistenceUnit();
				profile = juDbTest.profile();
			}
			
			dbTest.emfWork = new EmfWorkProvider().createEmfWork(persistenceUnit, profile);
			dbTest.em = dbTest.emfWork.getEm();
			dbTest.emUtil = new JuEmUtil(dbTest.em);
			
			return base;
		}
	}
}