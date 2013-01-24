package ch.inftec.ju.util.persistable;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Wrapper class around a GenericMemento that provides helper methods
 * to work with mementos.
 * <p>
 * Use the GenericMementoUtils.asX method to wrap a GenericMemento.
 * @author Martin
 *
 */
public class GenericMementoX implements GenericMemento {
	private GenericMemento memento;
		
	/**
	 * Creates a new wrapper around the specified memento.
	 * @param memento Base GenericMemento implementation to work with
	 */
	GenericMementoX(GenericMemento memento) {
		this.memento = memento;
	}
	
	/**
	 * Gets the first attribute with the specified key.
	 * @param key Key of the attribute
	 * @return MementoAttribute or null if no attribute with the specified name exists
	 */
	public MementoAttribute getAttribute(String key) {
		for (MementoAttribute attribute : this.memento.getAttributes()) {
			if (ObjectUtils.equals(attribute.getKey(), key)) return attribute;
		}
		
		return null;
	}
	
	/**
	 * Gets the String value of the first attribute with the specified key.
	 * @param key Key of the attribute
	 * @return String value of the first attribute or null if no attribute with the specified name exists
	 */
	public String getStringValue(String key) {
		MementoAttribute attribute = this.getAttribute(key);
		return attribute != null ? attribute.getStringValue() : null;
	}
	
	/**
	 * Gets the Date value of the first attribute with the specified key.
	 * @param key Key of the attribute
	 * @return Date value of the first attribute or null if no attribute with the specified name exists
	 */
	public Date getDateValue(String key) {
		MementoAttribute attribute = this.getAttribute(key);
		return attribute != null ? attribute.getDateValue() : null;
	}
	
	/**
	 * Gets the Long value of the first attribute with the specified key.
	 * @param key Key of the attribute
	 * @return Long value of the first attribute or null if no attribute with the specified name exists
	 */
	public Long getLongValue(String key) {
		MementoAttribute attribute = this.getAttribute(key);
		return attribute != null ? attribute.getLongValue() : null;
	}

//	/**
//	 * Creates a new object for the memento using the MetaData.type as a class name. 
//	 * @return New object based on the memento's type
//	 */
//	public Persistable createObject() {
//		try {
//			return (Persistable)Class.forName(this.memento.getMetaData().getType()).newInstance();
//		} catch (Exception ex) {
//			throw new JuRuntimeException("Couldn't create Persistable object for type " + this.memento.getMetaData().getType(), ex);
//		}
//	}
//	
//	/**
//	 * Same as createObject, but loads the object by setting the memento as well.
//	 * @return Loaded object after setting memento
//	 */
//	public Persistable createAndLoadObject() {
//		Persistable persistable = this.createObject();
//		persistable.setMemento(this.memento);
//		return persistable;
//	}
	
	@Override
	public List<GenericMemento> getChildren() {
		return this.memento.getChildren();
	}

	@Override
	public List<MementoAttribute> getAttributes() {
		return this.memento.getAttributes();
	}
}
