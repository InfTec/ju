package ch.inftec.ju.ee.test;

import javax.persistence.EntityManager;

import org.w3c.dom.Document;

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
	 * @param doc XML Document of an (optional) data export. If no data was exported, the
	 * document will be null
	 * @param serviceLocator ServiceLocator instance to perform CDI lookups
	 */
	public final void init(EntityManager em, Document doc, ServiceLocator serviceLocator) {
		super.init(em, doc);
		
		this.serviceLocator = serviceLocator;
	}
}