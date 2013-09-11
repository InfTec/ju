package ch.inftec.ju.testing.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.EmfWork;
import ch.inftec.ju.db.JuEmfUtil;
import ch.inftec.ju.util.PropertyChain;
import ch.inftec.ju.util.PropertyChainBuilder;

/**
 * Helper class to create EmfWork instance based on property files or
 * system properties.
 * <p>
 * Make sure to call the EmfWork.close method when the resource is no longer needed.
 * @author Martin
 *
 */
public final class EmfWorkProvider {
	private Logger logger = LoggerFactory.getLogger(EmfWorkProvider.class);

	/**
	 * Create an EmfWork instance for the specified persistence unit and profile.
	 * @param persistenceUnitName PersistenceUnit name
	 * @param profile Profile name. If null, it will be loaded from the property files / system properties
	 * @return EmfWork instance
	 */
	public EmfWork createEmfWork(String persistenceUnitName, String profile) {
		logger.debug("Creating EmfWork");
		
		PropertyChain pc = new PropertyChainBuilder()
			.addSystemPropertyEvaluator()
			.addResourcePropertyEvaluator("/META-INF/ju-testing.properties", true)
			.addResourcePropertyEvaluator("/META-INF/ju-testing_user.properties", true)
			.addResourcePropertyEvaluator("/META-INF/ju-testing_default.properties", false)				
			.getPropertyChain();
		
		String profileName = profile != null ? profile : pc.get("ju-dbutil-test.profile", true);
		String prefix = "ju-dbutil-test." + profileName;
		
		String connectionUrl = pc.get(prefix + ".connectionUrl", false);
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
}
