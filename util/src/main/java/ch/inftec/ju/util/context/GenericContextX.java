package ch.inftec.ju.util.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.inftec.ju.util.JuCollectionUtils;
import ch.inftec.ju.util.JuRuntimeException;

/**
 * Wrapper class around a GenericContext implementation providing some convenience
 * methods.
 * <p>
 * Use the GenericContextUtils to wrap GenericContext instances with a GenericContextX.
 * @author Martin
 *
 */
public class GenericContextX implements GenericContext {
	/**
	 * The GenericContext implementation that is wrapped.
	 */
	private GenericContext context;
	
	/**
	 * Creates a new wrapper class around the specified context.
	 * @param context GenericContext implementation to wrap
	 */
	public GenericContextX(GenericContext context) {
		this.context = context;
	}
	
	/**
	 * Gets an object of the specified type. If multiple objects exist,
	 * the first is returned.
	 * @param clazz Class type
	 * @return Instance of class type or null if no object exists
	 */
	public <T> T getObject(Class<T> clazz) {
		List<T> list = this.getObjects(clazz);
		return list.size() > 0 ? list.get(0) : null;
	}
	
	/**
	 * Same as getObject, but throws a JuRuntimeException if no object exists
	 * in the context.
	 * @param clazz Class type
	 * @return Instance of class type.
	 * @throws JuRuntimeException If no object exists
	 */
	public <T> T evalObject(Class<T> clazz) {
		T obj = this.getObject(clazz);
		if (obj == null) throw new JuRuntimeException("Object not defined in generic context: " + clazz);
		return obj;
	}
	
	@Override
	public <T> List<T> getObjects(Class<T> clazz) {
		return this.context.getObjects(clazz);
	}

	/**
	 * Gets an object of the specified class and for the
	 * specified String parameter.
	 * @param clazz Clazz of the object
	 * @param parameter String parameter
	 * @return Instance of class type or null if no object exists
	 */
	public <T> T getObject(Class<T> clazz, String parameter) {
		List<T> list = this.getObjects(clazz, JuCollectionUtils.stringMap(parameter, parameter));
		return list.size() > 0 ? list.get(0) : null;
	}
	
	/**
	 * Gets an (immutable) list of objects of the specified class for the
	 * specified String parameter.
	 * @param clazz Clazz of the list elements
	 * @param parameter String parameter
	 * @return Immutable list containing elements of type clazz. If no objects could
	 * be evaluated for the specified class type and parameters, an empty list is
	 * returned.
	 */
	public <T> List<T> getObjects(Class<T> clazz, String parameter) {
		return this.getObjects(clazz, JuCollectionUtils.stringMap(parameter, parameter));
	}
	
	/**
	 * Gets an object the specified class for the
	 * specified key value pair.
	 * @param clazz Clazz of the object
	 * @param key Key
	 * @param value Value
	 * @return Instance of class type or null if no object exists
	 */
	public <T> T getObject(Class<T> clazz, String key, Object value) {
		List<T> list = this.getObjects(clazz, key, value);
		return list.size() > 0 ? list.get(0) : null;
	}
	
	/**
	 * Gets an (immutable) list of objects of the specified class for the
	 * specified key value pair.
	 * @param clazz Clazz of the list elements
	 * @param key Key
	 * @param value Value
	 * @return Immutable list containing elements of type clazz. If no objects could
	 * be evaluated for the specified class type and parameters, an empty list is
	 * returned.
	 */
	public <T> List<T> getObjects(Class<T> clazz, String key, Object value) {
		Map<String, Object> map = new HashMap<>();
		map.put(key,  value);
		
		return this.getObjects(clazz, map);
	}
	
	/**
	 * Gets an object of the specified class. The map
	 * is used as a key value parameter list to evaluate the object.
	 * @param clazz Clazz of the object
	 * @param map Map of key (String) value (Object) pairs used as parameters to
	 * evaluate the objects.
	 * @return Instance of class type or null if no object exists
	 */
	public <T> T getObject(Class<T> clazz, Map<String, Object> map) {
		List<T> list = this.getObjects(clazz, map);
		return list.size() > 0 ? list.get(0) : null;
	}
	
	@Override
	public <T> List<T> getObjects(Class<T> clazz, Map<String, Object> map) {
		return this.context.getObjects(clazz, map);
	}
	
	@Override
	public String toString() {
		return this.context.toString();
	}
}