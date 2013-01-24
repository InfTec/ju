package ch.inftec.ju.util.persistable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import ch.inftec.ju.util.JuStringUtils;
import ch.inftec.ju.util.ReflectUtils;
import ch.inftec.ju.util.persistable.GenericMemento.MementoAttribute;
import ch.inftec.ju.util.persistable.MementoStorage.GenericMementoItem;

/**
 * GenericContextUtils contains utility methods related to the GenericContext interface.
 * 
 * @author tgdmemae
 *
 */
public final class GenericMementoUtils {
	/**
	 * Don't instantiate.
	 */
	private GenericMementoUtils() {
		throw new AssertionError("use only statically");
	}

	/**
	 * Instance of a default type handler, i.e. a type handler that will use the full
	 * class name as a type name for any Persistable object.
	 */
	public static final TypeHandler DEFAULT_TYPE_HANDLER = new TypeHandlerImpl();
	
	/**
	 * Wraps the specified GenericMemento in a GenericMementoX instance. If genericMemento
	 * already is a GenericMementoX instance, it is just returned.
	 * @param genericMemento GenericMemento instance
	 * @return GenericMementoX wrapper around the specified memento
	 */
	public static GenericMementoX asX(GenericMemento genericMemento) {
		if (genericMemento instanceof GenericMementoX) return (GenericMementoX)genericMemento;
		else return new GenericMementoX(genericMemento);
	}
	
	/**
	 * Creates a new GenericMementoBuilder to build a GenericMemento instance.
	 * @return GenericMementoBuilder
	 */
	public static GenericMementoBuilder builder() {
		return new GenericMementoBuilder(null);
	}
	
	/**
	 * Creates a new MementoStorage that stores Mementos in memory.
	 * @return Memory based MementoStorage
	 */
	public static MementoStorage newMemoryPersistenceStorage() {
		return new MemoryMementoStorage();
	}
	
	/**
	 * Creates a new MementoStorage that stores Mementos in a String.
	 * This can be useful for debugging.
	 * <p>
	 * Note that loading of mementos from the StringStorage is not supported yet.
	 * <p>
	 * Long values are prefixed with L:, Date value with D:
	 * @return String based MementoStorage
	 */
	public static MementoStorage newStringMementoStorage() {
		return new StringMementoStorage();
	}
	
	/**
	 * Persists the specified memento to a String using a StringMementoStorage and
	 * returns the yielded String.
	 * @param memento Memento to be persisted
	 * @param type Type of the memento to submit to the MementoStorage persistMemento method
	 * @return String as returned ty StringMementoStorage
	 */
	public static String persistToString(GenericMemento memento, String type) {
		MementoStorage stringStorage = GenericMementoUtils.newStringMementoStorage();
		stringStorage.persistMemento(memento, type);
		return stringStorage.toString();
	}
	
	/**
	 * Creates a new PersistableManager using the specified storage and type handler.
	 * @param storage MementoStorage backing the manager
	 * @param typeHandler TypeHandler used to handle Persistable types
	 * @return PersistableManager instance
	 */
	public static PersistableManager newPersistableManager(MementoStorage storage, TypeHandler typeHandler) {
		return new PersistableManagerImpl(storage, typeHandler);
	}
	
	/**
	 * Creates a new builder to build a TypeHandler.
	 * @return TypeHandlerBuilder
	 */
	public static TypeHandlerBuilder newTypeHandler() {
		return new TypeHandlerBuilder();
	}
	
	/**
	 * Creates a new GenericMementoItem implementation.
	 * @param memento GenericMemento
	 * @param id Id
	 * @param type Type
	 * @return GenericMementoImplementation
	 */
	static GenericMementoItem newGenericMementoItem(GenericMemento memento, Long id, String type) {
		return new GenericMementoItemImpl(memento, id, type);
	}	
	
	/**
	 * Implementation of the GenericMemento interface.
	 * @author Martin
	 *
	 */
	private static final class GenericMementoImpl implements GenericMemento {
		ArrayList<GenericMemento> children = new ArrayList<>();
		ArrayList<MementoAttribute> attributes = new ArrayList<>();
		
		/**
		 * Adds a new MementoAttribute to the memento.
		 * @param key Key name
		 * @param value String value
		 */
		void addAttribute(String key, String value) {
			this.attributes.add(new MementoAttributeImpl(key, value));
		}
		
		/**
		 * Adds a new MementoAttribute to the memento.
		 * @param key Key name
		 * @param value Date value
		 */
		void addAttribute(String key, Date value) {
			this.attributes.add(new MementoAttributeImpl(key, value));
		}
		
		/**
		 * Adds a new MementoAttribute to the memento.
		 * @param key Key name
		 * @param value Long value
		 */
		void addAttribute(String key, Long value) {
			this.attributes.add(new MementoAttributeImpl(key, value));
		}
		
		/**
		 * Adds a new MementoAttribute to the memento. Tries to cast the Object to a
		 * String, Long (/Integer) or Date. If the value is of a different type,
		 * a JuRuntimeException is thrown.
		 * @param key Key name
		 * @param value Value
		 */
		void addAttribute(String key, Object value) {			
			if (value == null) this.addAttribute(key, (String)null); // Handle null
			else if (value instanceof String) this.addAttribute(key, (String)value);
			else if (value instanceof Long) this.addAttribute(key, (Long)value);
			else if (value instanceof Integer) this.addAttribute(key, new Long((Integer)value));
			else if (value instanceof Date) this.addAttribute(key, (Date)value);
			else throw new IllegalArgumentException("Illegal data type: " + value.getClass());
		}
		
		/**
		 * Adds a new MementoAttribute setting all types. The types may
		 * also be null.
		 * @param key Key of the attribute
		 * @param stringValue String value of the attribute
		 * @param dateValue Date value of the attribute
		 * @param longValue Long value of the attribute
		 */
		void addAttribute(String key, String stringValue, Date dateValue, Long longValue) {
			this.attributes.add(new MementoAttributeImpl(key, stringValue, dateValue, longValue));
		}
		
		/**
		 * Adds the specified attribute to the memento.
		 * @param attribute MementoAttribute
		 */
		void addAttribute(MementoAttribute attribute) {
			this.attributes.add(attribute);
		}
		
		/**
		 * Adds a child to the object.
		 * @param child Child
		 */
		void addChild(GenericMemento child) {
			this.children.add(child);
		}

		@Override
		public List<GenericMemento> getChildren() {
			return Collections.unmodifiableList(this.children);
		}
		
		@Override
		public List<MementoAttribute> getAttributes() {
			return Collections.unmodifiableList(this.attributes);
		}
		
		@Override
		public String toString() {
			return JuStringUtils.toString(this, "childrenCount", this.children.size(), "attributesCount", this.attributes.size());
		}
	}
	
	/**
	 * Implmentation of a MementoAttribute.
	 * @author Martin
	 *
	 */
	private static final class MementoAttributeImpl implements MementoAttribute {
		String key;
		String stringValue;
		Date dateValue;
		Long longValue;
		
		/**
		 * Creates a new MementoAttribute with value type String.
		 * @param key Key of the attribute
		 * @param value Value of the attribute
		 */
		MementoAttributeImpl(String key, String value) {
			this(key, value, null, null);
		}
		
		/**
		 * Creates a new MementoAttribute with value type Date.
		 * @param key Key of the attribute
		 * @param value Value of the attribute
		 */
		MementoAttributeImpl(String key, Date value) {
			this(key, null, value, null);
		}
		
		/**
		 * Creates a new MementoAttribute with value type Long.
		 * @param key Key of the attribute
		 * @param value Value of the attribute
		 */
		MementoAttributeImpl(String key, Long value) {
			this(key, null, null, value);
		}
		
		/**
		 * Creates a new MementoAttribute setting all types. The types may
		 * also be null.
		 * @param key Key of the attribute
		 * @param stringValue String value of the attribute
		 * @param dateValue Date value of the attribute
		 * @param longValue Long value of the attribute
		 */
		MementoAttributeImpl(String key, String stringValue, Date dateValue, Long longValue) {
			this.key = key;
			this.stringValue = stringValue;
			this.dateValue = dateValue;
			this.longValue = longValue;
		}
		
		@Override
		public String getKey() {
			return this.key;
		}

		@Override
		public String getStringValue() {
			return this.stringValue;
		}
		
		@Override
		public Date getDateValue() {
			return this.dateValue;
		}
		
		@Override
		public Long getLongValue() {
			return this.longValue;
		}
		
		@Override
		public String toString() {
			return JuStringUtils.toString(this, "key", this.getKey(), "stringValue", this.getStringValue(), "longValue", this.getLongValue(), "dateValue", this.getDateValue());
		}
	}

	private static final class GenericMementoItemImpl implements MementoStorage.GenericMementoItem {
		GenericMemento memento;
		Long id;
		String type;
		
		private GenericMementoItemImpl(GenericMemento memento, Long id, String type) {
			this.memento = memento;
			this.id = id;
			this.type = type;
		}
		
		@Override
		public GenericMemento getMemento() {
			return this.memento;
		}

		@Override
		public Long getId() {
			return this.id;
		}

		@Override
		public String getType() {
			return this.type;
		}
		
		@Override
		public String toString() {
			return JuStringUtils.toString(this, "id", this.getId(), "type", this.getType());
		}
	}
	
	/**
	 * A GenericMementoBuilder generates instances of GenericMemento.
	 * <p>
	 * Use GenericMementoUtils.builder() to get an instance of the builder.
	 * @author tgdmemae
	 *
	 */
	public static final class GenericMementoBuilder {
		private GenericMementoImpl genericMemento = new GenericMementoImpl();
		private GenericMementoBuilder parentBuilder;
		
		private GenericMementoBuilder(GenericMementoBuilder parentBuilder) {
			this.parentBuilder = parentBuilder;
		}
		
		/**
		 * Adds a new child to the memento. Call childDone as soon as the child is
		 * complete.
		 * @return GenericMementoBuilder to build the child
		 */
		public GenericMementoBuilder newChild() {
			return new GenericMementoBuilder(this);
		}
		
		/**
		 * Indicates that a newChild call has been completed. Returns the builder of
		 * the child's parent.
		 * @return Parent GenericMementoBuilder
		 */
		public GenericMementoBuilder childDone() {
			if (this.parentBuilder == null) {
				throw new IllegalStateException("This is the root builder");
			}
			
			this.parentBuilder.genericMemento.addChild(this.genericMemento);
			return this.parentBuilder;
		}
		
		/**
		 * Adds a string value to the momento.
		 * @param key Key of the attribute
		 * @param value String value
		 * @return This builder to allow for chaining
		 */
		public GenericMementoBuilder addString(String key, String value) {
			this.genericMemento.addAttribute(key, value);
			return this;
		}
		
		/**
		 * Adds a string value to the momento.
		 * @param key Key of the attribute
		 * @param value Long value
		 * @return This builder to allow for chaining
		 */
		public GenericMementoBuilder addLong(String key, Long value) {
			this.genericMemento.addAttribute(key, value);
			return this;
		}
		
		/**
		 * Adds a string value to the momento.
		 * @param key Key of the attribute
		 * @param value Date value
		 * @return This builder to allow for chaining
		 */
		public GenericMementoBuilder addDate(String key, Date value) {
			this.genericMemento.addAttribute(key, value);
			return this;
		}
		
		/**
		 * Adds a new MementoAttribute to the memento. Tries to cast the Object to a
		 * String, Long (/Integer) or Date. If the value is of a different type,
		 * an IllegalArgumentException is thrown.
		 * @param key Key name of the attribute
		 * @param value Value
		 * @return This builder to allow for chaining
		 */
		public GenericMementoBuilder add(String key, Object value) {
			this.genericMemento.addAttribute(key, value);
			return this;
		}
		
		/**
		 * Adds a new MementoAttribute to the memento, setting all provided data types.
		 * <p>
		 * If a type should not be set, just provide null.
		 * @param key Key name of the attribute
		 * @param sVal String value of the attribute
		 * @param dVal Date value of the attribute
		 * @param lVal Long value of the attribute
		 * @return This builder to allow for chaining
		 */
		public GenericMementoBuilder add(String key, String sVal, Date dVal, Long lVal) {
			this.genericMemento.addAttribute(key,  sVal, dVal, lVal);
			return this;
		}
		
		/**
		 * Adds the specified attribute to the memento.
		 * @param attribute MementoAttribute implementation
		 * @return This builder to allow for chaining
		 */
		public GenericMementoBuilder add(MementoAttribute attribute) {
			this.genericMemento.addAttribute(attribute);
			return this;
		}
		
		/**
		 * Adds all attributes that are defined in the specified memento.
		 * This will also recursively add all children of the
		 * memento as memento children.
		 * @param memento GenericMemento to be added
		 * @return This builder to allow for chaining
		 */
		public GenericMementoBuilder add(GenericMemento memento) {
			for (MementoAttribute attribute : memento.getAttributes()) {
				this.add(attribute);
			}
			
			for (GenericMemento child : memento.getChildren()) {
				this.newChild()
					.add(child)
				.childDone();
			}
			
			return this;
		}
		
		/**
		 * Builds the GenericMemento that has been configured using this builder.
		 * @return GenericMemento instance
		 */
		public GenericMemento build() {
			if (this.parentBuilder != null) {
				throw new IllegalStateException("Build can only be called on the root builder. "
						+ "Close all childs using childDone first.");
			}

			return this.genericMemento;
		}
	}
	
	/**
	 * Builder to create TypeHandler instances. By default, the type handler
	 * is not dynamic, thus just matches full class names to types.
	 * <p>
	 * Use the newTypeHandler method to get an instance of a builder
	 * @author tgdmemae
	 *
	 */
	public static final class TypeHandlerBuilder {
		private TypeHandlerImpl typeHandler = new TypeHandlerImpl();
		
		TypeHandlerBuilder() {			
		}
		
		/**
		 * Sets whether tye handler is dynamic.
		 * <p>
		 * If the handler is dynamic, it will try to evaluate a memento type name for
		 * every type that is evaluated by the handler (by looking for a static field
		 * MEMENTO_TYPE_NAME). If the handler is not dynamic, it will only make this lookup
		 * for explicitly added types.
		 * <p>
		 * Keep in mind that dynamic lookup might make it impossible to get an instance
		 * of a type if it should be loaded before it was safed (e.g. from a persistent
		 * storage).
		 * @param dynamic Whether the handler will use dynamic type name lookup
		 * @return This builder to allow for chaining
		 */
		public TypeHandlerBuilder dynamic(boolean dynamic) {
			this.typeHandler.dynamic = dynamic;
			return this;
		}
		
		/**
		 * Adds the specified type to the handler. The handler will look for a field
		 * MEMENTO_TYPE_NAME of the type and if it does not exist use the full
		 * class name of the type as a type name.
		 * @param clazz Type to be added
		 * @return This builder to allow for chaining
		 */
		public TypeHandlerBuilder addMapping(Class<? extends Persistable> clazz) {
			this.typeHandler.addMapping(clazz);
			return this;
		}
		
		/**
		 * Sets an explicit mapping for a type.
		 * @param clazz Type
		 * @param typeName Type name
		 * @return This builder to allow for chaining
		 */
		public TypeHandlerBuilder setMapping(Class<? extends Persistable> clazz, String typeName) {
			this.typeHandler.setMapping(clazz, typeName);
			return this;
		}
		
		/**
		 * Gets the handler that was built using this builder.
		 */
		public TypeHandler getHandler() {
			return this.typeHandler;
		}
	}
	
	/**
	 * Default implementation of a TypeHandler.
	 * @author TGDMEMAE
	 *
	 */
	private static final class TypeHandlerImpl implements TypeHandler {
		private static final String MEMENTO_TYPE_FIELD_NAME = "MEMENTO_TYPE_NAME";
		
		private BidiMap<Class<? extends Persistable>, String> typeNames = new DualHashBidiMap<>();		
		
		/**
		 * If the handler is dynamic, it will automatically look for a static field named
		 * MEMENTO_TYPE_NAME when the type name of new Persistable instances is evaluated
		 * and no explicit name has been defined yet.
		 */
		private boolean dynamic = false;
		
		void addMapping(Class<? extends Persistable> persistableClass) {
			this.setMapping(persistableClass, this.evaluateTypeName(persistableClass));
		}
		
		void setMapping(Class<? extends Persistable> persistableClass, String typeName) {
			String currentName = this.typeNames.get(persistableClass);
			
			// Make sure the typeName isn't already defined for another class
			if (currentName != null && !currentName.equals(typeName)) {
				if (this.typeNames.containsValue(typeName)) {
					throw new IllegalArgumentException(String.format("Type mapping '%s' has already been defined for %s", typeName, persistableClass));
				}
			}
			
			this.typeNames.put(persistableClass, typeName);
			
		}
		
		private String evaluateTypeName(Class<? extends Persistable> clazz) {
			return ReflectUtils.getStaticFieldValue(clazz, TypeHandlerImpl.MEMENTO_TYPE_FIELD_NAME, clazz.getName()).toString();
		}
		
		@Override
		public String getTypeName(Persistable persistable) {
			if (this.dynamic) {
				this.setMapping(persistable.getClass(), this.evaluateTypeName(persistable.getClass()));
			}
			
			if (this.typeNames.containsKey(persistable.getClass())) {
				return this.typeNames.get(persistable.getClass());
			} else {
				return persistable.getClass().getName();
			}
		}

		@Override
		public Persistable newInstance(String typeName) {
			Object obj = null;
			try {
				String className = this.typeNames.containsValue(typeName)
					? this.typeNames.getKey(typeName).getName()
					: typeName;
				
				// Use the ReflectUtils.newInstance to force instantiation for private default constructors
				obj = ReflectUtils.newInstance(Class.forName(className), true);
			} catch (Exception ex) {
				throw new IllegalArgumentException("Couldn't create instance for type " + typeName, ex);
			}
			
			if (obj instanceof Persistable) return (Persistable)obj;
			else throw new IllegalArgumentException("Class for " + typeName + " does not implement Persistable");
		}	
	}
}
