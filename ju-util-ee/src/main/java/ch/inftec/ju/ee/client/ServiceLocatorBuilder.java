package ch.inftec.ju.ee.client;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.JuRuntimeException;

/**
 * Builder to create ServiceLocator instances.
 * @author Martin
 *
 */
public final class ServiceLocatorBuilder {
	/**
	 * Gets a builder to create a new Remote ServiceLocator, i.e. a ServiceLocator
	 * that will perform remote JBoss lookups.
	 * <p>
	 * Uses remote-naming for the lookup.
	 * @return Builder instance
	 */
	public static RemoteServiceLocatorBuilder buildRemote() {
		return new RemoteServiceLocatorBuilder();
	}
	
	/**
	 * Helper class to build remote ServiceLocator instances
	 * @author Martin
	 *
	 */
	public static class RemoteServiceLocatorBuilder {
		private String host = "localhost";
		private int port = 4447;
		private String appName;
		private String moduleName;

		/**
		 * Sets the remote host and port for the lookup.
		 * @param host Remote host. Default is localhost
		 * @param port Remote port. Default is 4447
		 * @return This builder to allow for chaining
		 */
		public RemoteServiceLocatorBuilder remoteServer(String host, int port) {
			this.host = host;
			this.port = port;
			return this;
		}
		
		/**
		 * Sets the application name. This is usually the name of the EAR without
		 * the .ear suffix, e.g. 'test' for test.ear
		 * @param appName Application name
		 * @return This builder to allow for chaining
		 */
		public RemoteServiceLocatorBuilder appName(String appName) {
			this.appName = appName;
			return this;
		}
		
		/**
		 * Sets the module name. This is usually the name of the EJB jar that contains
		 * the EJB bean without the .jar suffix, e.g. 'test' for test.jar
		 * @param moduleName Module name
		 * @return This builder to allow for chaining
		 */
		public RemoteServiceLocatorBuilder moduleName(String moduleName) {
			this.moduleName = moduleName;
			return this;
		}
		
		/**
		 * Creates a new RemoteServiceLocator instance with the attributes specified
		 * to the builder.
		 * @return JndiServiceLocator instance
		 */
		public JndiServiceLocator createServiceLocator() {
			try {
				// Set EJB Client API properties programmatically instead of using
				// jboss-ejb-client.properties file
				Properties clientProp = new Properties();
				clientProp.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
				clientProp.put("remote.connections", "default");
				clientProp.put("remote.connection.default.port", Integer.toString(this.port)); // Not working if not a String...
				clientProp.put("remote.connection.default.host", this.host);
//				clientProp.put("remote.connection.default.username", "ejbUser");
//				clientProp.put("remote.connection.default.password", "ejbPassword");
				clientProp.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
				 
				EJBClientConfiguration cc = new PropertiesBasedEJBClientConfiguration(clientProp);
				ContextSelector<EJBClientContext> selector = new ConfigBasedEJBClientContextSelector(cc);
				EJBClientContext.setSelector(selector);
				 
				Properties props = new Properties();
				props.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
				Context ctx = new InitialContext(props);
				
//				Properties jndiProps = new Properties();
//				jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
//				jndiProps.put(Context.PROVIDER_URL, String.format("remote://%s:%d", this.host, this.port));
//				// create a context passing these properties
//				Context ctx = new InitialContext(jndiProps);
				
				return new JndiServiceLocatorImpl(ctx, this.appName, this.moduleName);
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't create ServiceLocator", ex);
			}
		}
	}
	
	private static class JndiServiceLocatorImpl implements JndiServiceLocator {
		private Logger logger = LoggerFactory.getLogger(ServiceLocatorBuilder.class);
		
		private final Context ctx;
		private final String appName;
		private final String moduleName;
		
		public JndiServiceLocatorImpl(Context ctx, String appName, String moduleName) {
			this.ctx = ctx;
			this.appName = appName;
			this.moduleName = moduleName;
		}

		@Override
		public <T> T lookup(String jndiName) {
			String lookupString = String.format("ejb:%s/%s/%s"
					, this.appName
					, this.moduleName
					, jndiName);
			
			logger.debug("JNDI lookup: " + lookupString);
			
			try {
				@SuppressWarnings("unchecked")
				T obj = (T) this.ctx.lookup(lookupString);
				
				return obj;
			} catch (Exception ex) {
				throw new JuRuntimeException(ex);
			}
		}
		
		@Override
		public <T> T lookup(Class<T> clazz) {
			String lookupString = String.format("%sBean!%s"
					, clazz.getSimpleName()
					, clazz.getName());
			
			return this.lookup(lookupString);
		}
	}
}
