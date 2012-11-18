package ch.inftec.ju.util;

/**
 * Utility class containing object related methods.
 * @author TGDMEMAE
 *
 */
public final class JuObjectUtils {
	/**
	 * Don't instantiate.
	 */
	private JuObjectUtils() {
		throw new AssertionError("use only statically");
	}
	
	/**
	 * Gets an identity Integer for the specified object. This is the same number the
	 * Object.toString method would return, i.e. System.identityHashCode converted
	 * to a hex string. If the object is null 'null' is returned.
	 * @param obj Object to get identity string for
	 * @return Identity string, i.e. original hashCode converted to a hex string
	 * or 'null' if the specified object is null.
	 */
	public static String getIdentityString(Object obj) {
		if (obj == null) return "null";
		
		return Integer.toHexString(System.identityHashCode(obj));	
	}
	
}
