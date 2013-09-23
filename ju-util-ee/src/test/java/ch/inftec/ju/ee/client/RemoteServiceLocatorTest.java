package ch.inftec.ju.ee.client;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.ee.test.TestRemote;

public class RemoteServiceLocatorTest {
//	@Ignore("Needs running JBoss")
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
		jndiProperties.put(Context.PROVIDER_URL, "remote://localhost:14447");
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        final Context context = new InitialContext(jndiProperties);
		
        // Important: We need to use the 'ejb:' prefix...
		TestRemote testRemote = (TestRemote) context.lookup("ejb:ee-ear-ear/ee-ear-ejb/TestRemoteInterfaceBean!" + TestRemote.class.getName());
		Assert.assertEquals("TestRemoteInterfaceBean says hello to EjbClientApi", testRemote.getGreeting("EjbClientApi"));
	}
	
	@Test
	public void canLookup_testFacadeBean_usingRemoteNaming() throws Exception {
		// https://docs.jboss.org/author/display/AS72/Remote+EJB+invocations+via+JNDI+-+EJB+client+API+or+remote-naming+project
		
		Properties jndiProps = new Properties();
		jndiProps.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
		jndiProps.put(Context.PROVIDER_URL,"remote://localhost:14447");
		// create a context passing these properties
		Context ctx = new InitialContext(jndiProps);

		// Important: We can either use 'java:' as prefix or none 
		TestRemote testRemote = (TestRemote) ctx.lookup("java:ee-ear-ear/ee-ear-ejb/TestRemoteInterfaceBean!" + TestRemote.class.getName());
		Assert.assertEquals("TestRemoteInterfaceBean says hello to RemoteNaming", testRemote.getGreeting("RemoteNaming"));
		
		testRemote = (TestRemote) ctx.lookup("ee-ear-ear/ee-ear-ejb/TestRemoteInterfaceBean!" + TestRemote.class.getName());
		Assert.assertEquals("TestRemoteInterfaceBean says hello to RemoteNamingWithoutPrefix", testRemote.getGreeting("RemoteNamingWithoutPrefix"));		
	}
}
