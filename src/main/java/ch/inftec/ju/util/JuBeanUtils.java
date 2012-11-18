package ch.inftec.ju.util;

import java.util.ArrayList;

import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * Helper class containing Java Beans related methods.
 * @author tgdmemae
 *
 */
public class JuBeanUtils {
	/**
	 * Makes sure that the specified fields of the class (may be private)
	 * are not null.
	 * <p>
	 * Throws an IllegalStateException if they are.
	 * <p>
	 * If a field cannot be accessed, an exception is thrown too.
	 * @param obj Object to be checked
	 * @param fieldNames Names of the fields that must not be null
	 * @throws IllegalStateException containing the name of the fields that are null
	 */
	public static void checkFieldsNotNull(Object obj, String... fieldNames) {
		ArrayList<String> nullFields = new ArrayList<String>();
		ArrayList<String> errorFields = new ArrayList<String>();
		
		for (String fieldName : fieldNames) {
			try {
				Object res = FieldUtils.readField(obj, fieldName, true);
				if (res == null) nullFields.add(fieldName);
			} catch (IllegalAccessException ex) {
				errorFields.add(fieldName);
			}
		}
		
		if (nullFields.size() > 0 || errorFields.size() > 0) {
			XString xs = new XString("Fields must be set: ");
			xs.addItems(", ", nullFields.toArray());
			if (errorFields.size() > 0) {
				xs.addText(". Failed to access fields: ");
				xs.addItems(", ", errorFields.toArray());
			}
			
			throw new IllegalStateException(xs.toString());
		}
	}
}
