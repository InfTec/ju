package ch.inftec.ju.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to temporarly set system properties that can will be set
 * to their original values on (auto) close.
 * <p>
 * Useful for tests that rely on system properties when we don't want to
 * change global state.
 * @author Martin
 *
 */
public class SystemPropertyTempSetter implements AutoCloseable {
	private final Map<String, String> originalValues = new HashMap<>();
	
	/**
	 * Temporarly set the property to the specified value.
	 * @param key Key
	 * @param value Value
	 */
	public void setProperty(String key, String value) {
		if (!this.originalValues.containsKey(key)) {
			this.originalValues.put(key, System.getProperty(key));
		}
		if (value == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, value);
		}
	}
	
	@Override
	public void close() {
		for (String key : this.originalValues.keySet()) {
			String value = this.originalValues.get(key);
			if (value == null) {
				System.clearProperty(key);
			} else {
				System.setProperty(key, value);
			}
		}
	}
}
