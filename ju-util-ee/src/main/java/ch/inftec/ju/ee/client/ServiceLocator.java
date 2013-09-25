package ch.inftec.ju.ee.client;

/**
 * ServiceLocator implementing both CdiServiceLocator and JndiServiceLocator interfaces.
 * <p>
 * Can be used to lookup JNDI and CDI objects running in a container.
 * @author Martin
 *
 */
public interface ServiceLocator extends CdiServiceLocator, JndiServiceLocator {
}
