package ch.inftec.ju.ee.test;


/**
 * Interface that can be used as a remote interface for bean testing.
 * @author Martin
 *
 */
public interface TestRemote {
	/**
	 * Gets a greeting containing the specified name and the simple name of the class.
	 * @param name Name to greet
	 * @return String containing the simple name of the class and the specified name
	 */
	public String getGreeting(String name);
}
