package ch.inftec.ju.util.persistable;

/**
 * Interface that makes an object persistable, i.e. the object can persist
 * itself using the Memento design pattern.
 * <br>
 * In order to be read from the persistance storage, a Persistable object must
 * provide a non-argument constructor to create a new instance.
 * @author Martin
 *
 */
public interface Persistable {
	/**
	 * Persists the object using the specified Persistor.
	 * @param persistor Class prividing methods to persist attributes and
	 * other Persistable as children
	 */
	public GenericMemento createMemento();
	
	/**
	 * Initializes the state of the persistable object by the specified Memento.
	 * @param memento Memento containing object state
	 */
	public void setMemento(GenericMemento memento);
}
