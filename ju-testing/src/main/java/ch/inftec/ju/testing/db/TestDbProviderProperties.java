package ch.inftec.ju.testing.db;

import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.PropertyChain;
import ch.inftec.ju.util.PropertyChainBuilder;

public class TestDbProviderProperties implements TestDbProvider {
	@Override
	public TestDbInfo getTestDbInfo(String persistenceUnitName) {
		if ("ju-testing pu-test".equals(persistenceUnitName)) {
			PropertyChain pc = new PropertyChainBuilder()
				.addSystemPropertyEvaluator()
				.addResourcePropertyEvaluator("/META-INF/ju-dbutil-test.properties", true)
				.addResourcePropertyEvaluator("/META-INF/ju-dbutil-test_user.properties", true)
				.addResourcePropertyEvaluator("/META-INF/ju-dbutil-test_default.properties", false)				
				.setDefaultThrowExceptionIfUndefined(true)
				.getPropertyChain();
			
			String profileName = pc.get("ju-dbutil-test.profile");
			String prefix = "ju-dbutil-test." + profileName;
			
			String connectionUrl = pc.get(prefix + ".connectionUrl");
			String user = pc.get(prefix + ".user", false);
			String password = pc.get(prefix + ".password", false);
			
			return new TestDbInfo(connectionUrl, user, password);
		} else {
			throw new JuRuntimeException("Unknown persistence unit: " + persistenceUnitName);
		}
	}
}
