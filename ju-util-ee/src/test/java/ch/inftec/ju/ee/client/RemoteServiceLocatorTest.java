package ch.inftec.ju.ee.client;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb.client.ContextSelector;
import org.jboss.ejb.client.EJBClientConfiguration;
import org.jboss.ejb.client.EJBClientContext;
import org.jboss.ejb.client.PropertiesBasedEJBClientConfiguration;
import org.jboss.ejb.client.remoting.ConfigBasedEJBClientContextSelector;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ch.inftec.ju.ee.test.TestRemote;
import ch.inftec.ju.ee.test.TestRunnerFacade;

@Ignore("Needs running JBoss")
public class RemoteServiceLocatorTest {
	@Test
	public void canLookup_testFacadeBean_usingEjbClientApi() throws Exception {
		// https://docs.jboss.org/author/display/AS72/Remote+EJB+invocations+via+JNDI+-+EJB+client+API+or+remote-naming+project
		
//		14:57:28,691 INFO  [org.jboss.as.ejb3.deployment.processors.EjbJndiBindingsDeploymentUnitProcessor] (MSC service thread 1-13) JNDI bindings for session bean named TestRemoteInterfaceBean in deployment unit subdeployment "ee-ear-ejb.jar" of deployment "ee-ear-ear.ear" are as follows:
//
//			java:global/ee-ear-ear/ee-ear-ejb/TestRemoteInterfaceBean!ch.inftec.ju.ee.test.TestRemote
//			java:app/ee-ear-ejb/TestRemoteInterfaceBean!ch.inftec.ju.ee.test.TestRemote
//			java:module/TestRemoteInterfaceBean!ch.inftec.ju.ee.test.TestRemote
//			java:jboss/exported/ee-ear-ear/ee-ear-ejb/TestRemoteInterfaceBean!ch.inftec.ju.ee.test.TestRemote
//			java:global/ee-ear-ear/ee-ear-ejb/TestRemoteInterfaceBean
//			java:app/ee-ear-ejb/TestRemoteInterfaceBean
//			java:module/TestRemoteInterfaceBean
		
		// AppName     : ee-ear
		// ModuleName  : ee-ear-ejb
		// DistinctName: TestRemoteInterfaceBean
		
		final Hashtable<String, String> jndiProperties = new Hashtable<>();
//		jndiProperties.put(Context.PROVIDER_URL, "remote://localhost:14447");
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        final Context context = new InitialContext(jndiProperties);
		
        // Important: We need to use the 'ejb:' prefix...
		TestRemote testRemote = (TestRemote) context.lookup("ejb:ee-ear-ear/ee-ear-ejb/TestRemoteBean!" + TestRemote.class.getName());
		Assert.assertEquals("TestRemoteBean says hello to EjbClientApi", testRemote.getGreeting("EjbClientApi"));
	}
	
	@Test
	public void canLookup_testFacadeBean_usingEjbClientApi_withoutFile() throws Exception {
		// Set EJB Client API properties programmatically instead of using
		// jboss-ejb-client.properties file
		Properties clientProp = new Properties();
		clientProp.put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false");
		clientProp.put("remote.connections", "default");
		clientProp.put("remote.connection.default.port", "14447");
		clientProp.put("remote.connection.default.host", "localhost");
//		clientProp.put("remote.connection.default.username", "ejbUser");
//		clientProp.put("remote.connection.default.password", "ejbPassword");
		clientProp.put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false");
		 
		EJBClientConfiguration cc = new PropertiesBasedEJBClientConfiguration(clientProp);
		ContextSelector<EJBClientContext> selector = new ConfigBasedEJBClientContextSelector(cc);
		EJBClientContext.setSelector(selector);
		 
		final Hashtable<String, String> jndiProperties = new Hashtable<>();
//		jndiProperties.put(Context.PROVIDER_URL, "remote://localhost:14447");
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        final Context context = new InitialContext(jndiProperties);
		
        // Important: We need to use the 'ejb:' prefix...
		TestRemote testRemote = (TestRemote) context.lookup("ejb:ee-ear-ear/ee-ear-ejb/TestRemoteBean!" + TestRemote.class.getName());
		Assert.assertEquals("TestRemoteBean says hello to EjbClientApi", testRemote.getGreeting("EjbClientApi"));
	}
	
	/**
	 * Note that the remote naming tests were not really using the properties submitted to them
	 * by the Properties instance. Instead, the file jboss-ejb-client.properties was read, same as
	 * with the EJB Client API...
	 * @throws Exception
	 */
	@Test
	public void canLookup_testFacadeBean_usingRemoteNaming() throws Exception {
		// https://docs.jboss.org/author/display/AS72/Remote+EJB+invocations+via+JNDI+-+EJB+client+API+or+remote-naming+project
		
		Properties jndiProps = new Properties();
		jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
		jndiProps.put(Context.PROVIDER_URL,"remote://localhost:14447");
		jndiProps.put("remote.connection.default.port", "14447");
		// create a context passing these properties
		Context ctx = new InitialContext(jndiProps);

		// Important: We can either use 'java:' as prefix or none
		TestRemote testRemote = (TestRemote) ctx.lookup("ee-ear-ear/ee-ear-ejb/TestRemoteBean!" + TestRemote.class.getName());
		Assert.assertEquals("TestRemoteBean says hello to RemoteNamingWithoutPrefix", testRemote.getGreeting("RemoteNamingWithoutPrefix"));
		
		testRemote = (TestRemote) ctx.lookup("java:ee-ear-ear/ee-ear-ejb/TestRemoteBean!" + TestRemote.class.getName());
		Assert.assertEquals("TestRemoteBean says hello to RemoteNaming", testRemote.getGreeting("RemoteNaming"));
	}
	
	@Ignore("Will fail with exception as bli doesn't exist")
	@Test
	public void canLookup_testRunnerFacadeBean_usingRemoteNaming() throws Exception {
		// https://docs.jboss.org/author/display/AS72/Remote+EJB+invocations+via+JNDI+-+EJB+client+API+or+remote-naming+project
		
		Properties jndiProps = new Properties();
		jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
		jndiProps.put(Context.PROVIDER_URL,"remote://localhost:14447");
		// create a context passing these properties
		Context ctx = new InitialContext(jndiProps);

		TestRunnerFacade testRunnerFacade = (TestRunnerFacade) ctx.lookup("ee-ear-ear/ee-ear-ejb/TestRunnerFacadeBean!" + TestRunnerFacade.class.getName());
		testRunnerFacade.runTestMethodInEjbContext("bli", "bla", null);
	}

	@Test
	public void canLookup_testFacadeBean_usingServiceLocatorBuilder() throws Exception {
		JndiServiceLocator loc = ServiceLocatorBuilder.buildRemote()
			.remoteServer("localhost", 14447)
			.appName("ee-ear-ear")
			.moduleName("ee-ear-ejb")
			.createServiceLocator();
		
		TestRemote testRemote = loc.lookup(TestRemote.class);
		Assert.assertEquals("TestRemoteBean says hello to ServiceLocatorBuilder", testRemote.getGreeting("ServiceLocatorBuilder"));
	}
	
	@Ignore("Will fail with exception as bli doesn't exist")
	@Test
	public void canLookup_testRunnerFacadeBean_usingServiceLocatorBuilder() throws Exception {
		JndiServiceLocator loc = ServiceLocatorBuilder.buildRemote()
			.remoteServer("localhost", 14447)
			.appName("ee-ear-ear")
			.moduleName("ee-ear-ejb")
			.createServiceLocator();
		
		TestRunnerFacade testRunnerFacade = loc.lookup(TestRunnerFacade.class);
		testRunnerFacade.runTestMethodInEjbContext("bli", "bla", null);
	}
	
}
