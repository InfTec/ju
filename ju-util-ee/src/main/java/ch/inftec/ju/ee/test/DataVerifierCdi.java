package ch.inftec.ju.ee.test;

import javax.persistence.EntityManager;

import ch.inftec.ju.ee.client.ServiceLocator;
import ch.inftec.ju.testing.db.DataVerifier;

/**
 * Extended version of a DataVerifier that provides a ServiceLocator.
 * <p>
 * Note that not all test cases might support initializing a DataVerifierCdi.
 * @author Martin
 *
 */
public abstract class DataVerifierCdi extends DataVerifier {
	protected ServiceLocator serviceLocator;
	
	/**
	 * Initializes the DataVerifierCdi. Needs to be called from the testing
	 * framework before the verify method is invoked.
	 * @param em EntityManager instance of the current persistence context
	 * @param serviceLocator ServiceLocator instance to perform CDI lookups
	 */
	public final void init(EntityManager em, ServiceLocator serviceLocator) {
		super.init(em);
		
		this.serviceLocator = serviceLocator;
	}
}