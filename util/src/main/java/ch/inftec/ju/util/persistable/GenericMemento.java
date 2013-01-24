package ch.inftec.ju.util.persistable;

import java.util.Date;
import java.util.List;

/**
 * Generic Memento, i.e. an interface that allows for a generic implementation of the
 * Memento design pattern.
 * @author Martin
 *
 */
public interface GenericMemento {
	/**
	 * Gets a list of the object's children.
	 * @return List of GenericMemento
	 */
	public List<GenericMemento> getChildren();
	
	/**
	 * Gets the attributes attached to the object.
	 * @return List of MementoAttribute instances
	 */
	public List<MementoAttribute> getAttributes();
	
	/**
	 * Key / value attribute of a MementoStorage. There are getters for different data types. A
	 * storage must be able to store all values that are returned. But an implementation of an attribute
	 * doesn't need to provide values for all getters.
	 * @author Martin
	 *
	 */
	public interface MementoAttribute {
		/**
		 * Gets the key of the attribute.
		 * @return Key
		 */
		public String getKey();
		
		/**
		 * Gets the String value of the attribute.
		 * @return String value or null if no string value is set
		 */
		public String getStringValue();
		
		/**
		 * Gets the Date value of the attribute.
		 * @return Date value or null if no date value is set
		 */
		public Date getDateValue();
		
		/**
		 * Gets the Long value of the attribute.
		 * @return Long value or null if no long value is set
		 */
		public Long getLongValue();
	}

}
