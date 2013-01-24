package ch.inftec.ju.util.persistable;

import java.util.List;

/**
 * Implementation of a storage that can persist and load GenericMementos.
 * @author Martin
 *
 */
public interface MementoStorage {
	/**
	 * Creates a new object in the storage.
	 * @param memento Memento to be persisted
	 * @param type Type of the memento. Will be returned by the load methods.
	 * @return Unique identifier for the persisted Memento. The ID is also updated in the
	 * memento's MetaData.
	 */
	public Long persistMemento(GenericMemento memento, String type);
		
	/**
	 * Retrieves a Memento from the storage. If the object with the specified ID
	 * does not exist, null is returned.
	 * @param id Unique identifier of the object as returned by persistMemento
	 * @return GenericMementoItem or null if the memento does not exist
	 */
	public GenericMementoItem loadMemento(Long id);
	
	/**
	 * Gets a list of the most recently persisted mementos, latest Momento
	 * first.
	 * @param maxCount Maximum count of Mementos to be returned.
	 * @return List of GenericMementos limited to the specified maximum count. If the
	 * storage contains fewer mementos, only those are returned.
	 */
	public List<GenericMementoItem> loadMementos(int maxCount);
	
	/**
	 * Return type for the loadMemento and loadMementos methods of MementoStorage.
	 * @author TGDMEMAE
	 *
	 */
	public interface GenericMementoItem {
		/**
		 * Gets the GenericMemento of the item.
		 * @return GenericMemento instance
		 */
		public GenericMemento getMemento();
		
		/**
		 * Gets the internal ID of the GenericMemento.
		 * @return Internal ID of the memento
		 */
		public Long getId();
		
		/**
		 * Gets the type of the memento, as specified when persisting.
		 * @return Type of the memento
		 */
		public String getType();
	}
}
