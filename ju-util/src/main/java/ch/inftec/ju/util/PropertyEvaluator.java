package ch.inftec.ju.util;

/**
 * Interface for classes that evaluate properties based on keys.
 * <p>
 * Used by the PropertyChainBuilder class.
 * @author Martin
 *
 */
public interface PropertyEvaluator {
	/**
	 * Gets the property for the specified key. If the property isn't defined,
	 * null is returned.
	 * @param key Key name
	 * @return Property value or null if the property is not defined
	 */
	Object get(String key);
}
