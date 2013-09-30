package ch.inftec.ju.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * Class containing reflection related utility methods.
 * 
 * @author tgdmemae
 * 
 */
public final class ReflectUtils {
	/**
	 * Don't instantiate.
	 */
	private ReflectUtils() {
		throw new AssertionError("use only statically");
	}
	
	/**
	 * Get the underlying class for a type, or null if the type is a variable
	 * type.
	 * 
	 * @param type
	 *            the type
	 * @return the underlying class
	 */
	public static Class<?> getClass(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		} else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type)
					.getGenericComponentType();
			Class<?> componentClass = getClass(componentType);
			if (componentClass != null) {
				return Array.newInstance(componentClass, 0).getClass();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Get the actual type arguments a child class has used to extend a generic
	 * base class.
	 * 
	 * @param baseClass
	 *            the base class
	 * @param childClass
	 *            the child class
	 * @return a list of the raw classes for the actual type arguments.
	 */
	public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass,
			Class<? extends T> childClass) {
		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
		Type type = childClass;
		// start walking up the inheritance hierarchy until we hit baseClass
		while (!getClass(type).equals(baseClass)) {
			if (type instanceof Class) {
				// there is no useful information for us in raw types, so just
				// keep going.
				type = ((Class<?>) type).getGenericSuperclass();
			} else {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Class<?> rawType = (Class<?>) parameterizedType.getRawType();

				Type[] actualTypeArguments = parameterizedType
						.getActualTypeArguments();
				TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
				for (int i = 0; i < actualTypeArguments.length; i++) {
					resolvedTypes
							.put(typeParameters[i], actualTypeArguments[i]);
				}

				if (!rawType.equals(baseClass)) {
					type = rawType.getGenericSuperclass();
				}
			}
		}

		// finally, for each actual type argument provided to baseClass,
		// determine (if possible)
		// the raw class for that type argument.
		Type[] actualTypeArguments;
		if (type instanceof Class) {
			actualTypeArguments = ((Class<?>) type).getTypeParameters();
		} else {
			actualTypeArguments = ((ParameterizedType) type)
					.getActualTypeArguments();
		}
		List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
		// resolve types by chasing down type variables.
		for (Type baseType : actualTypeArguments) {
			while (resolvedTypes.containsKey(baseType)) {
				baseType = resolvedTypes.get(baseType);
			}
			typeArgumentsAsClasses.add(getClass(baseType));
		}
		return typeArgumentsAsClasses;
	}
	
	/**
	 * Gets the class whose method called the current method.
	 * @return Class that called the current method
	 */
	public static Class<?> getCallingClass() {
		try {
			StackTraceElement stackTrace[] = Thread.currentThread().getStackTrace();
		
			// The first element in the stack trace will be the getStackTrace method,
			// the second this method, the third the called method - i.e. the method
			// of the calling class is the 4th element.
			return Class.forName(stackTrace[3].getClassName());
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("Couldn't get calling class", ex);
		}
	}
	
	/**
	 * Gets an array of types for the specified objects. If an object is null,
	 * it is assumed to be of type Object.
	 * @param objs Array ob objects
	 * @return Array of corresponding class types for the objects
	 */
	public static Class<?>[] getTypes(Object objs[]) {
		ArrayList<Class<?>> types = new ArrayList<Class<?>>();
		
		for (Object obj : objs) {
			types.add(obj == null ? Object.class : obj.getClass());
		}
		
		return (Class<?>[])types.toArray(new Class<?>[0]);
	}
	
	/**
	 * Gets the first declared method that matches the specified name and parameter types. In contrast
	 * to the Java reflection method, this method will try to find a method that only matches super types
	 * of the specified parameters instead of the exact type.
	 * <br>
	 * If multiple methods match, the first returned by the Class.getMethods method will be returned
	 * @param clazz Class instance
	 * @param paramTypes Parameter types
	 * @return Method of the class with the specified parameter types or null if no such
	 * method exists
	 */
	public static Method getDeclaredMethod(Class<?> clazz, String name, Class<?> paramTypes[]) {
		// Try to get the exact method using the Java built in method
		try {
			return clazz.getDeclaredMethod(name, paramTypes);
		} catch (NoSuchMethodException ex) {
			// Ignore this exception and try to find a method that matches
			// super types of the parameter types
		}
		
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(name) && m.getParameterTypes().length == paramTypes.length) {
				boolean mismatch = false;
				for (int i = 0; i < paramTypes.length; i++) {
					if (!m.getParameterTypes()[i].isAssignableFrom(paramTypes[i])) {
						mismatch = true;
						break;
					}
				}
				if (!mismatch) return m;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the value of a static field.
	 * @param clazz Clazz to get static field from
	 * @param fieldName Name of the field
	 * @param defaultValue Default value that is returned if the field doesn't exist or has the value null.
	 * @return Value of the field or the specified return value if the field either doesn't exist or is null
	 * @throws JuRuntimeException If the field cannot be read
	 */
	public static Object getStaticFieldValue(Class<?> clazz, String fieldName, Object defaultValue) {
		try {
			Object fieldValue = FieldUtils.readStaticField(clazz, fieldName, true);
			return fieldValue != null ? fieldValue : defaultValue;
		} catch (IllegalArgumentException ex) {
			return defaultValue;
		} catch (Exception ex) {
			throw new JuRuntimeException(String.format("Couldn't read static field %s from class %s", fieldName, clazz), ex);
		}
	}
	
	/**
	 * Gets the value of the specified field in the specified object.
	 * <p>
	 * If the field is not accessible, the method tries to overwrite the
	 * accessibility.
	 * @param obj Object to get field value of
	 * @param field Field to get
	 * @return
	 */
	public static Object getFieldValue(Object obj, Field field) {
		try {
			if (!field.isAccessible()) field.setAccessible(true);
			return field.get(obj);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't access field", ex);
		}
	}
	
	/**
	 * Creates an instance of the specified class, forcing access to the default constructor if necessary.
	 * @param clazz Clazz
	 * @param forceAccess If true, access to a private constructor is forces
	 * @return New instance of the specified class
	 * @throws JuRuntimeException if the instance cannot be created using the default constructor
	 */
	public static Object newInstance(Class<?> clazz, boolean forceAccess) {
		try {
			if (!forceAccess) {
				return clazz.newInstance();
			} else {
				Constructor<?> constructor = clazz.getDeclaredConstructor();
				constructor.setAccessible(true);
				return constructor.newInstance();
			}
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't create instance using default constructor for class " + clazz, ex);
		}
	}
	
	/**
	 * Gets all declared fields of the specified class that have the specified annotation.
	 * <p>
	 * This will not returned any inherited fields of base classes.
	 * <p>
	 * Annotations need to have retention=RUNTIME to be found at runtime
	 * @param clazz Class of the object to find fields of
	 * @param annotationClass Class of the annotation the field needs to have
	 * @return All declared fields of the class (regardless of accessibility) that have the annotation assigned.
	 * Fields will be sorted by name
	 */
	public static List<Field> getDeclaredFieldsByAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		Map<String, Field> fields = new TreeMap<>();
		
		for (Field field : clazz.getDeclaredFields()) {
			Annotation a = field.getAnnotation(annotationClass);
			if (a != null) fields.put(field.getName(), field);			
		}
		
		return new ArrayList<>(fields.values());
	}
	
	/**
	 * Gets the value if the (alphabetically) first declared field of the specified object that has the
	 * specified annotation assigned.
	 * <p>
	 * If no such field can be found, null is returned.
	 * @param obj Object to find field of
	 * @param annotationClass Class of the annotation the field needs to have
	 * @param forceAccess If true, access is forced even if the field is private
	 * @return Value of the (alphabetically) first declared field with the specified annotation or null if none exists
	 */
	public static Object getDeclaredFieldValueByAnnotation(Object obj, Class<? extends Annotation> annotationClass, boolean forceAccess) {
		List<Field> fields = ReflectUtils.getDeclaredFieldsByAnnotation(obj.getClass(), annotationClass);
		if (fields.isEmpty()) return null;
		
		Field field = fields.get(0);
		try {
			if (forceAccess) field.setAccessible(true);
			return field.get(obj);
		} catch (IllegalAccessException ex) {
			throw new JuRuntimeException("Couldn't access field " + field.getName(), ex);
		}
	}
	
	/**
	 * Gets all annotations of the specified type for the specified class.
	 * <p>
	 * If no annotation is found, an empty list is returned.
	 * <p>
	 * If includeSuperClassesAnnotations is true, all super classes of the class are searched for the
	 * specified annotation. The annotations are returned in order class (first) to super classes (Object last).
	 * @param clazz Class to search for annotations
	 * @param annotationClass Type of annotation
	 * @param includeSuperClassesAnnotations If true, super classes are searched as well. If false, we can only get 0 or 1 results
	 * @return List of annotations, in order of clazz (first) to super class (Object is last)
	 */
	public static <A extends Annotation> List<A> getAnnotations(
			Class<?> clazz, 
			Class<A> annotationClass, 
			boolean includeSuperClassesAnnotations) {
		
		List<A> annos = new ArrayList<>();
		
		Class<?> c = clazz;
		do {
			A anno = c.getAnnotation(annotationClass);
			if (anno != null) annos.add(anno);
			
			c = c.getSuperclass();
		} while (includeSuperClassesAnnotations && c != null);
		
		return annos;
	}
	
	/**
	 * Gets annotations of the specified type for the specified method.
	 * <p>
	 * If no annotation is found, an empty list is returned.
	 * <p>
	 * If includeOverriddenMethods and includeClassAnnotations are both false, we can get either 1
	 * or 2 results.
	 * <p>
	 * Annotations are returned in the following order:
	 * <ol>
	 *   <li>method</li>
	 *   <li>overridden method(s)</li>
	 *   <li>declaring class</li>
	 *   <li>super classes of declaring class</li>
	 * </ol>
	 * @param method Method to get annotations for
	 * @param annotationClass Class of the annotation to get
	 * @param includeOverriddenMethods If true, overridden methods of super classes are also searched
	 * @param includeClassAnnotations If true, the declaring class of the method is also searched
	 * @param includeSuperClassesAnnotations If true, super classes of the declaring class are also searched. This
	 * parameter is ignored if includeClassAnnotations is false.
	 * @return List of annotations. If no annotations were found, an empty list is returned.
	 */
	public static <A extends Annotation> List<A> getAnnotations(
			Method method,
			Class<A> annotationClass,
			boolean includeOverriddenMethods,
			boolean includeClassAnnotations,
			boolean includeSuperClassesAnnotations) {
		
		List<A> annos = new ArrayList<>();
		
		Method m = method;
		do {
			A anno = m.getAnnotation(annotationClass);
			if (anno != null) annos.add(anno);
			
			// Check if the method is overriding a method in the super class
			Class<?> superClass = m.getDeclaringClass().getSuperclass();
			if (superClass != null) {
				try {
					m = superClass.getMethod(m.getName(), m.getParameterTypes());
				} catch (NoSuchMethodException ex) {
					m = null;
				}
			} else {
				m = null;
			}
		} while (includeOverriddenMethods && m != null);
		
		if (includeClassAnnotations) {
			List<A> classAnnos = ReflectUtils.getAnnotations(method.getDeclaringClass(), annotationClass, includeSuperClassesAnnotations);
			annos.addAll(classAnnos);
		}
		
		return annos;
	}
}
