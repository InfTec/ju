package ch.inftec.ju.util.context;

import java.util.List;
import java.util.Map;

/**
 * A GenericContext provides getter methods to generically evaluate objects.
 * <p>
 * The class or interface must be matched exactly. There is no support for inheritance, so
 * for instance getObjects(Object.class) will not yield any results if there are only
 * objects defined for Integer.class.
 * <p> 
 * A GenericContext implementation may evaluate its result dynamically, so data doesn'n necessarily
 * have to be immutable.
 * 
 * @author Martin
 *
 */
public interface GenericContext {
	/**
	 * Gets an (immutable) list of objects of the specified class.
	 * @param clazz Class of the list elements
	 * @return Immutable list containing elements of type clazz. If no objects exist
	 * for the specified class type, an empty list is returned.
	 */
	public <T> List<T> getObjects(Class<T> clazz);
	
	/**
	 * Gets an (immutable) list of objects of the specified class. The map
	 * is used as a key value parameter list to evaluate the objects.
	 * @param clazz Clazz of the list elements
	 * @param map Map of key (String) value (Object) pairs used as parameters to
	 * evaluate the objects.
	 * @return Immutable list containing elements of type clazz. If no objects could
	 * be evaluated for the specified class type and parameters, an empty list is
	 * returned.
	 */
	public <T> List<T> getObjects(Class<T> clazz, Map<String, Object> map);
}
