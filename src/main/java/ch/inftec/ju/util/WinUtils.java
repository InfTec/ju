package ch.inftec.ju.util;

import com.sun.security.auth.module.NTSystem;

/**
 * Utility class for windows specific functionality.
 * <p>
 * This class uses the NTSystem class and will not work on
 * non-windows environments.
 * @author tgdmemae
 *
 */
@SuppressWarnings("restriction")
public class WinUtils {
	/**
	 * Gets the windows login name.
	 * @return Windows login name
	 */
	public static String getUserName() {
		return new NTSystem().getName();
	}
	
	/**
	 * Gets the windows domain name.
	 * @return Windows domain name
	 */
	public static String getDomainName() {
		return new NTSystem().getDomain();
	}
}
