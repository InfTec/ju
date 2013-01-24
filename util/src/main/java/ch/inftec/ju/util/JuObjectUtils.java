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
	
	/**
	 * Implementation of the .NET style as-operator.
	 * <p>
	 * If the object is of type clazz, it is converted and returned. Otherwise, null is
	 * returned.
	 * <p>
	 * Example:<br>
	 * <code>
	 *   Object obj = new String("test");<br>
	 *   String s = JuObjectUtils.as(obj, String.class);<br>
	 * </code>
	 * @param obj
	 * @param clazz
	 * @return
	 */
	public static <T> T as(Object obj, Class<T> clazz) {
		if (clazz.isInstance(obj)) {
			@SuppressWarnings("unchecked")
			T t = (T)obj;
			return t;
		} else {
			return null;
		}
	}
	
}
