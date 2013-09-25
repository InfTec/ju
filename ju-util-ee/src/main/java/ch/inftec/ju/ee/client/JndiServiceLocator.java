package ch.inftec.ju.ee.client;

/**
 * ServiceLocator that can perform JNDI lookups by a JNDI name.
 * @author Martin
 *
 */
public interface JndiServiceLocator {
	/**
	 * Looks up the specified JNDI resource by its name
	 * @param jndiName JNDI name
	 * @return Object defined by the JNDI name
	 * @throws JuRuntimeException if the lookup fails. The exception may contain a NamingException as
	 * its cause.
	 */
	public <T> T lookup(String jndiName);
	
	/**
	 * Looks up the specified JNDI resource by its type.
	 * <p>
	 * Different implementations of JndiServiceLocator may use different ways to evaluate
	 * a JNDI name from the type if necessary.
	 * @param clazz Desired type
	 * @return Type instance
	 * @throws JuRuntimeException if the lookup fails. The exception may contain a NamingException as
	 * its cause.
	 */
	public <T> T lookup(Class<T> clazz);
}