package ch.inftec.ju.util.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the ObjectEvaluator interface. This adapter directs the
 * call to getObjects to one of the protected methods depending on the
 * parameter type.
 * <br>
 * To implement a method, it must be overridden and return a list instead of null.
 * If a specific method is not overridden, the parameters are passed to the next
 * possible method.
 * @author Martin
 *
 * @param <T> Type of the elements that are returned in the list.
 */
public class ObjectEvaluatorAdapter<T> implements ObjectEvaluator<T> {
	/**
	 * Object evaluator using one parameter.
	 * @param parameter String parameter
	 * @return List of T elements, empty list if none objects were found or null if the objects
	 * should be evaluated using the getObjects(key, value) method
	 */
	protected List<T> getObjects(String parameter) {
		return null;
	}
	
	/**
	 * Object evaluator using a key value pair.
	 * @param key Key
	 * @param value Value
	 * @return List of T elements, empty list if none objects were found or null if the objects
	 * should be evaluated using the doGetObjects(map) method
	 */
	protected List<T> getObjects(String key, Object value) {
		return null;
	}
	
	/**
	 * Object evaluator using a map of parameters.
	 * @param map Map containing key value parameters.
	 * @return List of T elements or an empty list if non objects were found.
	 */
	protected List<T> doGetObjects(Map<String, Object> map) {
		return null;
	}
	
	
	@Override
	final public List<T> getObjects(Map<String, Object> map) {
		List<T> list = null;
		
		if (map.size() == 1) {
			String key = map.keySet().iterator().next();
			list = this.getObjects(key);
			if (list != null) {
				return list;
			} else {
				list = this.getObjects(key, map.get(key));
				if (list != null) return list;
			}
		}
		
		list = this.doGetObjects(map);
		
		return list != null ? Collections.unmodifiableList(list) : Collections.unmodifiableList(new ArrayList<T>());
	}
}
