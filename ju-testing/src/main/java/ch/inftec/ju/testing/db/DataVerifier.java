package ch.inftec.ju.testing.db;

import javax.persistence.EntityManager;

import ch.inftec.ju.db.JuEmUtil;

/**
 * Base class for data verifiers, i.e. a container for code that will run
 * after a DB test case has completed to verify that the data has been
 * persisted correctly. 
 * <p>
 * Extending classes must provide a public default constructor.
 * @author Martin
 *
 */
public abstract class DataVerifier {
	protected EntityManager em;
	protected JuEmUtil emUtil;
	
	/**
	 * Initializes the DataVerifier. Needs to be called from the testing
	 * framework before the verify method is invoked.
	 * @param em EntityManager instance of the current persistence context
	 */
	public final void init(EntityManager em) {
		this.em = em;
		this.emUtil = new JuEmUtil(em);
	}
	
	/**
	 * Method that will be called by the testing framework after
	 * the data test method has completed and the transaction has been
	 * either committed or rolled back.
	 * @throws Exception If verification fails
	 */
	public abstract void verify() throws Exception;
}
