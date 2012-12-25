package ch.inftec.ju.util;

import java.lang.reflect.Method;

/**
 * Utility class for windows specific functionality.
 * <p>
 * This class uses the NTSystem class and will not work on
 * non-windows environments.
 * @author tgdmemae
 *
 */
public class WinUtils {
	/**
	 * Gets the windows login name.
	 * @return Windows login name
	 */
	public static String getUserName() {
		return WinUtils.getNTSystemValue("getName");
	}
	
	/**
	 * Gets the windows domain name.
	 * @return Windows domain name
	 */
	public static String getDomainName() {
		return WinUtils.getNTSystemValue("getDomain");
	}
	
	private static String getNTSystemValue(String methodName) {
		String className = "com.sun.security.auth.module.NTSystem";
		try {
			Class<?> clazz = Class.forName(className);
			Method method = clazz.getMethod(methodName);
			Object instance = clazz.newInstance();
			
			return (String)method.invoke(instance);
		} catch (Exception ex) {
			throw new JuRuntimeException(String.format("Couldn't invoke method %s of %s. Make sure this is a Oracle Windows JDK.", methodName, className));
		}
	}
}
