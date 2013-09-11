package ch.inftec.ju.ee.db.test;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

import ch.inftec.ju.testing.db.AbstractDbTest;

/**
 * Base class for tests that require DB access and CDI.
 * <p>
 * Will be run in a CDI container using Arquillian. Any CDI related classes
 * must be added manually to the Arquillian Deployment.
 * @author Martin
 *
 */
public class AbstractDbTestCdi extends AbstractDbTest {
	/**
	 * Producer method that gets the current EntityManager of the test case.
	 * @return EntityManager instance
	 */
	@Produces
	@RequestScoped
	public EntityManager getEntityManager() {
		return this.em;
	}
}
