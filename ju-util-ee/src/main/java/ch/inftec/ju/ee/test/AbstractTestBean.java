package ch.inftec.ju.ee.test;

/**
 * Base class for simple test beans.
 * @author Martin
 *
 */
public class AbstractTestBean {
	/**
	 * Gets a greeting containing the specified name and the simple name of the class.
	 * @param name Name to greet
	 * @return String containing the simple name of the class and the specified name
	 */
	public String getGreeting(String name) {
		return String.format("%s says hello to %s", this.getClass().getSimpleName(), name);
	}
}
