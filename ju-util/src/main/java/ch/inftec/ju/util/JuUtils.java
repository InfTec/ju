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
	 *   <li>System Property: Overrides all other properties</li>
	 *   <li>ju_user.properties: Optional user properties file overriding default properties. 
	 *       Must be on the classpath</li>
	 *   <li>ju_default.properties: Default properties that are used when no 
	 *       overriding properties are specified or found</li>
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
				.addSystemPropertyEvaluator()
				.addResourcePropertyEvaluator("/ju_user.properties", true)
				.addResourcePropertyEvaluator("/ju_default.properties", false)
				.getPropertyChain();
		}
		return juPropertyChain;
	}
}
