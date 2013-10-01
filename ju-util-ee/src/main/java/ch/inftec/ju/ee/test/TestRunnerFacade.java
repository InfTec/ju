package ch.inftec.ju.ee.test;

import java.io.Serializable;

import javax.ejb.Remote;

/**
 * Helper facade to invoke (test) methods from local unit tests in a remote JVM server container
 * context.
 * <p>
 * The facade is supposed to perform regular EJB transaction commit or rollback, depending on the
 * exception that might arise when executing test methods.
 * @author Martin
 *
 */
@Remote
public interface TestRunnerFacade {
	/**
	 * Runs a (test) method in an EJB context, allowing it to use container functionality and
	 * beans.
	 * @param className Class name
	 * @param methodName Method name
	 * @param context TestRunnerContext containing meta information about the text execution
	 * @throws Exception If the method fails with an exception (including test assertion failures)
	 */
	public void runTestMethodInEjbContext(String className, String methodName, TestRunnerContext context) throws Exception;

	
	public void runDataVerifierInEjbContext(String... verifierClassNames) throws Exception;
	
	/**
	 * Runs an arbitrary method in an EJB context and returns the result of the method.
	 * @param className Class name that contains the method
	 * @param methodName Method name
	 * @param parameterTypes Array of argument types
	 * @param args Array of arguments
	 * @return Result value of the method
	 */
	public Object runMethodInEjbContext(String className, String methodName, Class<?> parameterTypes[], Object args[]) throws Exception;
	
	/**
	 * Helper class that contains information about the context the test runs within.
	 * @author Martin
	 *
	 */
	public static class TestRunnerContext implements Serializable {
		private String localRoot;

		/**
		 * Gets the root path of the local (i.e. not server) test. This can be used to
		 * create files in a location relative to the test class VM rather than the JBoss server.
		 * @return Root path of the local test execution VM
		 */
		public String getLocalRoot() {
			return localRoot;
		}

		public void setLocalRoot(String localRoot) {
			this.localRoot = localRoot;
		}
	}
	
	/**
	 * Interface for classes that are context aware, i.e. allow for a TestRunnerContext to be
	 * set when executed by the TestRunnerFacade.
	 * @author Martin
	 *
	 */
	public interface ContextAware {
		/**
		 * Sets the TestRunnerContext
		 * @param context
		 */
		void setContext(TestRunnerContext context);
	}
	
	/**
	 * Interfaces for classes that would like to be initialized before the test method is run
	 * (some functionality as a @Before method would provide).
	 * @author Martin
	 *
	 */
	public interface Initializable {
		/**
		 * Initializer method that is called before the unit test is executed.
		 * <p>
		 * The method will be called within the same EJB / transaction context as the test method
		 */
		void init();
	}
}
