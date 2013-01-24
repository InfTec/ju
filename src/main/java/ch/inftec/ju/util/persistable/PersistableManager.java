package ch.inftec.ju.util.persistable;


/**
 * Interface providing functionality to persist and load Persistable objects.
 * <br>
 * This interface ties all Persistable classes together to provide a
 * persistence framework.
 * @author Martin
 *
 */
public interface PersistableManager {
	/**
	 * Persists the specified object.
	 * @param obj Object to persist
	 * @return Unique ID of the persisted object
	 */
	public Long persist(Persistable obj);
	
	/**
	 * Loads the PersistedObject into a new object instance.
	 * @param id Unique ID of the persisted object
	 * @return Object instance or null if the object cannot be loaded
	 */
	public Persistable load(Long id);
}
