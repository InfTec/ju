package ch.inftec.ju.util.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.inftec.ju.util.JuStringUtils;


/**
 * GenericContextUtils contains utility methods related to the GenericContext interface.
 * 
 * @author tgdmemae
 *
 */
public final class GenericContextUtils {
	/**
	 * Don't instantiate.
	 */
	private GenericContextUtils() {
		throw new AssertionError("use only statically");
	}
	
	/**
	 * Provides a builder to construct GenericContext instances.
	 * @return GenericContext instance
	 */
	public static GenericContextBuilder builder() {
		return new GenericContextBuilder();
	}
	
	/**
	 * Wraps the specified context in a GenericContextX instance. If the
	 * context is already an instance of GenericContextX, the same reference
	 * is returned.
	 * @param context GenericContextX wrapper around the context
	 * @return GenericContextX working on the specified context.
	 */
	public static GenericContextX asX(GenericContext context) {
		if (context instanceof GenericContextX) return (GenericContextX)context;
		else return new GenericContextX(context);
	}
	
	/**
	 * Implementation of the GenericContext interface using Object lists and
	 * ObjectEvaluators.
	 * 
	 * @author Martin
	 *
	 */
	private static final class GenericContextImpl implements GenericContext {
		/**
		 * HashMap containing the object lists for the class types.
		 */
		HashMap<Class<?>, ArrayList<Object>> objects = new HashMap<>();
		
		/**
		 * HashMap containing the ObjectEvaluator instances for the class types.
		 */
		HashMap<Class<?>, ObjectEvaluator<?>> evaluators = new HashMap<>();
		
		@Override
		public <T> List<T> getObjects(Class<T> clazz) {
			return Collections.unmodifiableList(this.getList(clazz));
		}

		/**
		 * Adds the specified objects of type clazz to the context.
		 * <br>
		 * We add the annotation SafeVarargs and make the method final to indicate that
		 * the method will not produce heap pollution by performing faulty array casts.
		 * @param clazz Clazz type of the objects
		 * @param objs Objects
		 */
		@SafeVarargs	
		final public <T> void addObjects(Class<T> clazz, T... objs) {
			ArrayList<T> list = this.getList(clazz);
			
			for (T obj : objs) list.add(obj);
		}
		
		/**
		 * Clears all objects for the given type.
		 * @param clazz Class type
		 */
		<T> void clearObjects(Class<T> clazz) {
			this.objects.remove(clazz);
		}
		
		/**
		 * Clears all objects for the given type (if any) and sets
		 * the specified object.
		 * @param clazz 
		 */
		<T> void setObject(Class<T> clazz, T obj) {
			this.clearObjects(clazz);
			this.addObjects(clazz, obj);
		}
		
		/**
		 * Gets the list of objects for the specified clazz.
		 * <br>
		 * Because the objects map contains ArrayLists of type object, we need to
		 * perform an unchecked cast here.
		 * @param clazz Clazz type
		 * @return List of objects for clazz
		 */		
		<T> ArrayList<T> getList(Class<T> clazz) {
			if (!this.objects.containsKey(clazz)) {
				this.objects.put(clazz, new ArrayList<Object>());
			}
			
			@SuppressWarnings(value = "unchecked") 
			ArrayList<T> list = (ArrayList<T>)this.objects.get(clazz); 
			return list;
		}

		@Override
		public <T> List<T> getObjects(Class<T> clazz, Map<String, Object> map) {
			ObjectEvaluator<T> evaluator = this.getEvaluator(clazz);
			
			return evaluator != null ? evaluator.getObjects(map) : Collections.unmodifiableList(new ArrayList<T>());
		}
		
		/**
		 * Sets the ObjecteEvaluator instance for the specified class type, overriding
		 * any previous evaluator.
		 * <br>
		 * The evaluator is used to evaluate objects using parameters.
		 * @param clazz Clazz type
		 * @param evaluator ObjectEvaluator instance
		 */
		<T> void setObjectEvaluator(Class<T> clazz, ObjectEvaluator<T> evaluator) {
			this.evaluators.put(clazz, evaluator);
		}
		
		/**
		 * Gets the ObjectEvaluator instance for the specified clazz.
		 * <br>
		 * Because the object map contains ObjectEvaluators of type ?, we need
		 * to perform an unchecked cast here.
		 * @param clazz Clazz type
		 * @return ObjectEvaluator instance or null if none is defined
		 */		
		<T> ObjectEvaluator<T> getEvaluator(Class<T> clazz) {
			@SuppressWarnings(value = "unchecked")
			ObjectEvaluator<T> evaluator = (ObjectEvaluator<T>)this.evaluators.get(clazz);
			
			return evaluator;
		}
		
		@Override
		public String toString() {
			return JuStringUtils.toString(this, "objects", this.objects, "evaluators" , this.evaluators);
		}
	}
	
	/**
	 * A GenericContextBuilder can be used to create GenericContext instances.
	 * <p>
	 * Use the GenericContextUtils.builder() method to use the builder.
	 * 
	 * @author tgdmemae
	 *
	 */
	public static class GenericContextBuilder {
		private GenericContextImpl genericContext = new GenericContextImpl();
		
		private GenericContextBuilder() {			
		}
		
		/**
		 * Clears all objects for the given type (if any) and sets
		 * the specified object.
		 * @param clazz Type
		 * @param obj Object value to be set
		 * @return This builder to allow for chaining
		 */
		public <T> GenericContextBuilder setObject(Class<T> clazz, T obj) {
			this.genericContext.setObject(clazz, obj);
			return this;
		}
		
		/**
		 * Adds the specified objects of type clazz to the context.
		 * <br>
		 * We add the annotation SafeVarargs and make the method final to indicate that
		 * the method will not produce heap pollution by performing faulty array casts.
		 * @param clazz Clazz type of the objects
		 * @param objs Objects
		 * @return This builder to allow for chaining
		 */
		@SafeVarargs	
		final public <T> GenericContextBuilder addObjects(Class<T> clazz, T... objs) {
			this.genericContext.addObjects(clazz, objs);
			return this;
		}
		
		/**
		 * Sets the ObjecteEvaluator instance for the specified class type, overriding
		 * any previous evaluator.
		 * <br>
		 * The evaluator is used to evaluate objects using parameters.
		 * @param clazz Clazz type
		 * @param evaluator ObjectEvaluator instance
		 * @return This builder to allow for chaining
		 */
		public <T> GenericContextBuilder setObjectEvaluator(Class<T> clazz, ObjectEvaluator<T> evaluator) {
			this.genericContext.setObjectEvaluator(clazz, evaluator);
			return this;
		}
		
		/**
		 * Gets the GenericContext built with the builder. Note that this method always
		 * returns the same instance.
		 * @return GenericContext implementation
		 */
		public GenericContext build() {
			return this.genericContext;
		}
	}
}
