package ch.inftec.ju.util.persistable;

/**
 * A TypeHandler provides methods to get a type name for a Persistable
 * instance and to create an empty instance of a Persistable
 * given its type name.
 * @author TGDMEMAE
 *
 */
public interface TypeHandler {
	/**
	 * Gets the type name for the given Persistable instance.
	 * @param persistable Persistable instance
	 * @return Type name
	 */
	public String getTypeName(Persistable persistable);
	
	/**
	 * Creates a new instance for the given type name.
	 * @param typeName Type name
	 * @return New Persistable instance for the specified type name
	 * @throws IllegalArgumentException If the type name cannot be handled
	 */
	public Persistable newInstance(String typeName);
}
