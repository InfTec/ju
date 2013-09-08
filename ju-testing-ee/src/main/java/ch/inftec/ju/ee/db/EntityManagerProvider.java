package ch.inftec.ju.ee.db;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.EmfWork;
import ch.inftec.ju.db.JuEmfUtil;
import ch.inftec.ju.util.PropertyChain;
import ch.inftec.ju.util.PropertyChainBuilder;

/**
 * CDI persistence provider class.
 * @author Martin
 *
 */
public class EntityManagerProvider {
	private Logger logger = LoggerFactory.getLogger(EntityManagerProvider.class);
	
	@Produces
	@RequestScoped
	private EmfWork createEmfWork() {
		logger.debug("Creating EmfWork");
		
		String persistenceUnitName = "ju-pu-test";
		
		PropertyChain pc = new PropertyChainBuilder()
			.addSystemPropertyEvaluator()
			.addResourcePropertyEvaluator("/META-INF/ju-testing-ee.properties", true)
			.addResourcePropertyEvaluator("/META-INF/ju-testing-ee_user.properties", true)
			.addResourcePropertyEvaluator("/META-INF/ju-testing-ee_default.properties", false)				
			.setDefaultThrowExceptionIfUndefined(true)
			.getPropertyChain();
		
		String profileName = pc.get("ju-dbutil-test.profile");
		String prefix = "ju-dbutil-test." + profileName;
		
		String connectionUrl = pc.get(prefix + ".connectionUrl");
		String user = pc.get(prefix + ".user", false);
		String password = pc.get(prefix + ".password", false);
		
		JuEmfUtil emfUtil = JuEmfUtil.create()
				.persistenceUnitName(persistenceUnitName)
				.connectionUrl(connectionUrl)
				.user(user)
				.password(password)
				.build();
	
		return emfUtil.startWork();
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
