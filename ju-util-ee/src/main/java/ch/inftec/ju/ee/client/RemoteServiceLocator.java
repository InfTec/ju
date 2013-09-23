package ch.inftec.ju.ee.client;


import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import ch.inftec.ju.util.JuRuntimeException;

/**
 * ServiceLocator to perform remote JNDI lookups.
 * @author tgdmemae
 *
 */
public final class RemoteServiceLocator {
	private Context context;
	private static RemoteServiceLocator serviceLocator = new RemoteServiceLocator();
	
	private RemoteServiceLocator() {
		try {
			// https://docs.jboss.org/author/display/AS72/EJB+invocations+from+a+remote+client+using+JNDI
//			Using a different file for setting up EJB client context
//			The EJB client code will by default look for jboss-ejb-client.properties in the classpath. However, you can specify a different file of your choice by setting the "jboss.ejb.client.properties.file.path" system property which points to a properties file on your filesystem, containing the client context configurations. An example for that would be "-Djboss.ejb.client.properties.file.path=/home/me/my-client/custom-jboss-ejb-client.properties"


			final Hashtable jndiProperties = new Hashtable();
			jndiProperties.put(Context.PROVIDER_URL, "remote://localhost:14447");
//			jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
	        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
	        final Context context = new InitialContext(jndiProperties);
	        this.context = context;
	        if (1 == 1) return;
	        

	        // The JNDI lookup name for a stateful session bean has the syntax of:
	        // ejb:<appName>/<moduleName>/<distinctName>/<beanName>!<viewClassName>?stateful
	        //
	        // <appName> The application name is the name of the EAR that the EJB is deployed in
	        // (without the .ear). If the EJB JAR is not deployed in an EAR then this is
	        // blank. The app name can also be specified in the EAR's application.xml
	        //
	        // <moduleName> By the default the module name is the name of the EJB JAR file (without the
	        // .jar suffix). The module name might be overridden in the ejb-jar.xml
	        //
	        // <distinctName> : AS7 allows each deployment to have an (optional) distinct name.
	        // This example does not use this so leave it blank.
	        //
	        // <beanName> : The name of the session been to be invoked.
	        //
	        // <viewClassName>: The fully qualified classname of the remote interface. Must include
	        // the whole package name.
	        
			
			Properties properties = new Properties();
			InputStream s = this.getClass().getResourceAsStream("jboss-ejb-client.properties");
			properties.load(s);
			s.close();
			
			// TODO: Use better way to overwrite provider url
			String providerUrl = System.getProperty("java.naming.provider.url");
			if (providerUrl != null) {
				properties.setProperty("java.naming.provider.url", providerUrl);
			}
			
			this.context = new InitialContext(properties);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't initialize RemoteServiceLocator", ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T doLookup(String jndiName) {
		try {
			return (T)this.context.lookup(jndiName);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't lookup JNDI name " + jndiName, ex);
		}
	}
	
	/**
	 * Performs a JNDI lookup using the specified name.
	 * @param jndiName JNDI name
	 * @return Typed object we lookup up
	 */
	public static <T> T lookup(String jndiName) {
		return serviceLocator.doLookup(jndiName);
	}
	
	/**
	 * Gets the specified facade from the esw-ejb3 module.
	 * @param pFacadeClass
	 * @return
	 */
    public static <T> T getFacadeEjb3(Class<T> pFacadeClass) {
    	try {
	    	// Build the JNDI name of form esw-ear/esw-ejb3/TestRunnerFacadeBean!com.itservices.servicedb.ejb3.test.TestRunnerFacadeRemote
	    	String jndiName = RemoteServiceLocator.getJndiName("esw-ejb3", pFacadeClass);
	    	return serviceLocator.doLookup(jndiName);
    	} catch (Exception ex) {
    		throw new JuRuntimeException("Couldn't lookup EJB3 facade " + pFacadeClass.getName(), ex);
    	}
    }
    
    /**
     * Gets the specified facade from the esw-ejb module.
     * @param pFacadeClass
     * @return
     */
    public static <T> T getFacadeEjb(Class<T> pFacadeClass) {
    	try {
	    	// Build the JNDI name of form esw-ear/esw-ejb3/TestRunnerFacadeBean!com.itservices.servicedb.ejb3.test.TestRunnerFacadeRemote
    		String jndiName = RemoteServiceLocator.getJndiName("esw-ejb", pFacadeClass);
	    	return serviceLocator.doLookup(jndiName);
    	} catch (Exception ex) {
    		throw new JuRuntimeException("Couldn't lookup EJB facade " + pFacadeClass.getName(), ex);
    	}
    }
    
    private static String getJndiName(String module, Class<?> facadeClass) {
    	// Build the JNDI name of form esw-ear/esw-ejb3/TestRunnerFacadeBean!com.itservices.servicedb.ejb3.test.TestRunnerFacadeRemote
    	// For Remote interfaces, we'll assume that the interface ends in Remote, but the Bean class doesn't contain the Remote
    	String beanName = facadeClass.getSimpleName();
    	if (beanName.endsWith("Remote")) beanName = beanName.substring(0, beanName.lastIndexOf("Remote"));
    	
    	return String.format("ejb:/esw-ear/%s/%sBean!%s"
    			, module
    			, beanName
    			, facadeClass.getName());
    }
}
