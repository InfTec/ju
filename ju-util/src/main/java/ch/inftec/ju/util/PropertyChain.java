package ch.inftec.ju.util;

/**
 * Interface to access properties that can be arranged and priorized in a chain using the
 * PropertyChainBuilder.
 * <p>
 * If a property has the value null, it is considered undefined.
 * <p>
 * The PropertyChain interface provides getter methods for String and for generic types. Support for 
 * generic type conversion support may very depending on the implementation of the property chain.
 * 
 * @author Martin
 *
 */
public interface PropertyChain {
	/**
	 * Gets the property for the specified key
	 * @param key Key name
	 * @return Property or null if none is defined
	 */
	String get(String key);
	
	/**
	 * Gets the property with the specified key throwing an exception
	 * if it is not defined.
	 * @param key Key name
	 * @param throwExceptionIfNotDefined
	 * @return Property value
	 * @throws JuRuntimeException If the property doesn't exist
	 */
	String get(String key, boolean throwExceptionIfNotDefined);
	
	/**
	 * Gets the property with the specified key. If it doesn't exist,
	 * the default value is returned.
	 * @param key Key name
	 * @param defaultValue Value to return if the property doesn't exist
	 * @return Property value or default value if it doesn't exist
	 */
	String get(String key, String defaultValue);
	
	/**
	 * Gets the property for the specified key
	 * @param key Key name
	 * @return Property or null if none is defined
	 */
	<T> T get(String key, Class<T> clazz);
	
	/**
	 * Gets the property with the specified key throwing an exception
	 * if it is not defined.
	 * @param key Key name
	 * @param throwExceptionIfNotDefined
	 * @return Property value
	 * @throws JuRuntimeException If the property doesn't exist
	 */
	<T> T get(String key, Class<T> clazz, boolean throwExceptionIfNotDefined);
	
	/**
	 * Gets the property with the specified key. If it doesn't exist,
	 * the default value is returned.
	 * @param key Key name
	 * @param defaultValue Value to return if the property doesn't exist
	 * @return Property value or default value if it doesn't exist
	 */
	<T> T get(String key, Class<T> clazz, String defaultValue);
}
