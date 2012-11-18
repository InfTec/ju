package ch.inftec.ju.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
