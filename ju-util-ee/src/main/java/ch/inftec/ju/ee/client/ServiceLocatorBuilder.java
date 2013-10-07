package ch.inftec.ju.ee.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
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
import ch.inftec.ju.util.PropertyChain;
import ch.inftec.ju.util.PropertyChainBuilder;

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
	 * Builds a remote service locator based on the values of property files on the classpath
	 * and system properties, in the following order:
	 * <ol>
	 *   <li>System property</li>
	 *   <li>Optional resource: /ju-remote_user.properties</li>
	 *   <li>Default properties: /ju-remote.properties</li>
	 * </ol>
	 * Check the /ju-remote.properties file for the possible property keys. 
	 * @return Remote service locator
	 */
	public static JndiServiceLocator createRemoteByConfigurationFiles() {
		PropertyChain pc = new PropertyChainBuilder()
			.addSystemPropertyEvaluator()
			.addResourcePropertyEvaluator("/ju-remote_user.properties", true)
			.addResourcePropertyEvaluator("/ju-remote.properties", false)
			.setDefaultThrowExceptionIfUndefined(true)
			.getPropertyChain();
		
		return buildRemote()
			.remoteServer(pc.get("ju.ee.remote.host"), pc.get("ju.ee.remote.port", Integer.class))
			.appName(pc.get("ju.ee.remote.appName"))
			.moduleName(pc.get("ju.ee.remote.moduleName"))
			.createServiceLocator();
	}
	
	/**
	 * Gets a builder to create a new Local ServiceLocator, i.e. a ServiceLocator
	 * that will perform local JNDI and CDI lookups.
	 * @return Builder instance
	 */
	public static LocalServiceLocatorBuilder buildLocal() {
		return new LocalServiceLocatorBuilder();
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
				
				return new RemoteServiceLocatorImpl(ctx, this.appName, this.moduleName);
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't create ServiceLocator", ex);
			}
		}
	}
	
	/**
	 * Helper class to build local ServiceLocator instances
	 * @author Martin
	 *
	 */
	public static class LocalServiceLocatorBuilder {
		private String moduleName;
		
		/**
		 * Sets the module name. This is usually the name of the EJB jar that contains
		 * the EJB bean without the .jar suffix, e.g. 'test' for test.jar
		 * <p>
		 * Is used to look up remote interfaces in the same (local) app.
		 * @param moduleName Module name
		 * @return This builder to allow for chaining
		 */
		public LocalServiceLocatorBuilder moduleName(String moduleName) {
			this.moduleName = moduleName;
			return this;
		}
		
		/**
		 * Creates a new LocalServiceLocatorBuilder instance with the attributes specified
		 * to the builder.
		 * @return ServiceLocator instance
		 */
		public ServiceLocator createServiceLocator() {
			return new LocalServiceLocatorImpl(this.moduleName);
		}
	}
		
	
	private static abstract class AbstractJndiServiceLocator implements JndiServiceLocator {
		protected Logger logger = LoggerFactory.getLogger(this.getClass());
		
		private final Context ctx;
		
		protected AbstractJndiServiceLocator(Context ctx) {
			this.ctx = ctx;
		}
		
		/**
		 * Gets the absolute JNDI name (i.e. the JNDI name that will actually be used
		 * for the lookup) based on the submitted relative JNDI name.
		 * <p>
		 * The default implementation just returns the same JNDI
		 * @param jndiName (Relative) JNDI name
		 * @return Absolute JNDI name used to perform the lookup
		 */
		protected String getAbsoluteJndiName(String jndiName) {
			return jndiName;
		}
		
		@Override
		public <T> T lookup(String jndiName) {
			String absoluteJndiName = this.getAbsoluteJndiName(jndiName);
			logger.debug(String.format("JNDI lookup (relative: %s, absolute: %s)", jndiName, absoluteJndiName));
			
			try {
				@SuppressWarnings("unchecked")
				T obj = (T) this.ctx.lookup(absoluteJndiName);
				
				return obj;
			} catch (Exception ex) {
				throw new JuRuntimeException(ex);
			}
		}
	}
	
	private static class RemoteServiceLocatorImpl extends AbstractJndiServiceLocator {
		private final String appName;
		private final String moduleName;
		
		public RemoteServiceLocatorImpl(Context ctx, String appName, String moduleName) {
			super(ctx);

			this.appName = appName;
			this.moduleName = moduleName;
		}

		@Override
		protected String getAbsoluteJndiName(String jndiName) {
			String absoluteJndiName = String.format("ejb:%s/%s/%s"
					, this.appName
					, this.moduleName
					, jndiName);
			
			return absoluteJndiName;
		}
		
		@Override
		public <T> T lookup(Class<T> clazz) {
			String lookupString = String.format("%sBean!%s"
					, clazz.getSimpleName()
					, clazz.getName());
			
			return this.lookup(lookupString);
		}
	}
	
	private static class LocalServiceLocatorImpl extends AbstractJndiServiceLocator implements ServiceLocator {
		private static final String JNDI_NAME_BEAN_MANAGER = "java:comp/BeanManager";
		
		private final String moduleName;
		private BeanManager bm;
		
		private LocalServiceLocatorImpl(String moduleName) {
			super(createInitialContext());

			this.moduleName = moduleName == null ? "" : moduleName;
			this.bm = this.lookup(JNDI_NAME_BEAN_MANAGER);
		}
		
		private static Context createInitialContext() {
			try {
				return new InitialContext();
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't create InitialContext", ex);
			}
		}

		@Override
		public <T> T lookup(Class<T> clazz) {
//			java:global/ee-ear-ear/ee-ear-ejb/TestLocalBean!ch.inftec.ju.ee.test.TestLocal
//			java:app/ee-ear-ejb/TestLocalBean!ch.inftec.ju.ee.test.TestLocal
//			java:module/TestLocalBean!ch.inftec.ju.ee.test.TestLocal
//			java:global/ee-ear-ear/ee-ear-ejb/TestLocalBean
//			java:app/ee-ear-ejb/TestLocalBean
//			java:module/TestLocalBean
			
			String jndiName = String.format("java:app/%s/%s!%s"
					, this.moduleName
					, clazz.getSimpleName() + "Bean"
					, clazz.getName());
			
			return this.lookup(jndiName);
		}

		@Override
		public <T> T cdi(Class<T> clazz) {
			Set<Bean<?>> beans = this.bm.getBeans(clazz);
			List<T> instances = this.getInstances(beans, clazz);
			if (instances.size() != 1) {
				throw new JuRuntimeException("Expected exactly one result for CDI lookup of " + clazz);
			} else {
				return instances.get(0);
			}
		}
		
		private <T> List<T> getInstances(Set<Bean<?>> beans, Class<T> clazz) {
			List<T> instances = new ArrayList<>();
			for (Bean<?> bean : beans) {
				CreationalContext<?> cont = this.bm.createCreationalContext(bean);
				
				@SuppressWarnings("unchecked")
				T t = (T) this.bm.getReference(bean, clazz, cont);
				
				instances.add(t);
			}
			
			return instances;
		}
	}
}
