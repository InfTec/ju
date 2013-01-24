package ch.inftec.ju.util.persistable;

import ch.inftec.ju.util.persistable.MementoStorage.GenericMementoItem;


/**
 * Implementation of the PersistableManager interface.
 * @author Martin
 *
 */
final class PersistableManagerImpl implements PersistableManager {
	/**
	 * The storage used to persist the objects.
	 */
	private MementoStorage storage;
	
	/**
	 * Type handler instance used by this manager.
	 */
	private TypeHandler typeHandler;
	
	/**
	 * Creates a new persistence manager using the specified storage.
	 * @param storage Memento storage
	 */
	public PersistableManagerImpl(MementoStorage storage, TypeHandler typeHandler) {
		this.storage = storage;
		this.typeHandler = typeHandler;
	}
	
	@Override
	public Long persist(Persistable obj) {
		String type = this.typeHandler.getTypeName(obj);
		return this.storage.persistMemento(obj.createMemento(), type);
	}
	
	@Override
	public Persistable load(Long id) {
		GenericMementoItem mementoItem = this.storage.loadMemento(id);
		
		if (mementoItem != null) {
			Persistable persistable = this.typeHandler.newInstance(mementoItem.getType());
			persistable.setMemento(mementoItem.getMemento());
			return persistable;
		} else {
			return null;
		}
	}
}
