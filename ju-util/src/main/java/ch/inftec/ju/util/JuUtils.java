package ch.inftec.ju.util;

/**
 * Utility class containing functions related to the JU library itself.
 * @author Martin
 *
 */
public class JuUtils {
	private static PropertyChain juPropertyChain;
	
	/**
	 * Gets a PropertyChain to evaluate ju properties.
	 * <p>
	 * Evaluation of the properties is as follows:
	 * <ol>
	 *   <li><b>ju_module.properties</b>: Optional file to override properties in a module that we don't wan't
	 *       to override even with system properties. Is intended to be checked into source control.</li>
	 *   <li><b>System Property</b>: Overrides all other properties</li>
	 *   <li><b>ju_module_default.properties</b>: Optional file to override default properties in a module. Is intended
	 *       to be checked into source control. In this file, we should only add properties for
	 *       which we want to change the default value for a module (or whatever scope the
	 *       classpath has).</li>
	 *   <li><b>ju_user.properties</b>: Optional user properties file overriding default properties. 
	 *       Must be on the classpath and is not intended to be checked into source control</li>
	 *   <li><b>ju_default.properties</b>: Global default properties that are used when no 
	 *       overriding properties are specified or found. This file also contains the description
	 *       for all possible properties.</li>
	 * </ol>
	 * The PropertyChain of this method is cached, so it is loaded only the first time
	 * it is accessed.
	 * <p>
	 * The PropertyChain is configured <i>not</i> to throw exceptions by default if a property
	 * is undefined.
	 * 
	 * @return PropertyChain implemenation to evaluate JU properties
	 */
	public static PropertyChain getJuPropertyChain() {
		if (juPropertyChain == null) {
			juPropertyChain = new PropertyChainBuilder()
				.addResourcePropertyEvaluator("/ju_module.properties", true)
				.addSystemPropertyEvaluator()
				.addResourcePropertyEvaluator("/ju_module_default.properties", true)
				.addResourcePropertyEvaluator("/ju_user.properties", true)
				.addResourcePropertyEvaluator("/ju_default.properties", false)
				.getPropertyChain();
		}
		return juPropertyChain;
	}
}
