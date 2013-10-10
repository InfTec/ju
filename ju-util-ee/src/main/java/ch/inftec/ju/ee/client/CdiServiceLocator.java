package ch.inftec.ju.ee.client;

/**
 * ServiceLocator that looks up CDI beans programmatically using
 * the BeanManager interface of a Container.
 * @author Martin
 *
 */
public interface CdiServiceLocator {
	/**
	 * Default CDI lookup of the specified type.
	 * @param clazz Desired type to get from CDI
	 * @return Instance of T
	 */
	public <T> T cdi(Class<T> clazz);
}
