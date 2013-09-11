package ch.inftec.ju.ee.db;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.EmfWork;
import ch.inftec.ju.testing.db.EmfWorkProvider;

/**
 * CDI persistence provider class. Provides a RequestScoped EntityManager instance that can be used
 * for DB tests.
 * @author Martin
 *
 */
public class EntityManagerProvider {
	private Logger logger = LoggerFactory.getLogger(EntityManagerProvider.class);
	
	@Produces
	@RequestScoped
	private EmfWork createEmfWork() {
		return new EmfWorkProvider().createEmfWork("ju-pu-test", null);
	}
	
	@Produces
	@RequestScoped
	public EntityManager createEntityManager(EmfWork emfWork) {
		logger.debug("Getting EntityManager from EmfWork");
		return emfWork.getEm();
	}
	
	public void closeEntityManager(@Disposes EmfWork emfWork) {
		logger.debug("Closing EmfWork, disposing EntityManager");
		emfWork.close();
	}
}
