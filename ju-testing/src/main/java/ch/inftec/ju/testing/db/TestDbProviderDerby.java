package ch.inftec.ju.testing.db;

import ch.inftec.ju.util.JuRuntimeException;

public class TestDbProviderDerby implements TestDbProvider {
	@Override
	public TestDbInfo getTestDbInfo(String persistenceUnitName) {
		if ("ju-testing pu-test".equals(persistenceUnitName)) {
			return new TestDbInfo("jdbc:derby:memory:ju-testing_pu-test;create=true");
		} else {
			throw new JuRuntimeException("Unknown persistence unit: " + persistenceUnitName);
		}
	}
}
