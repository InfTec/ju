package ch.inftec.ju.ee.client;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ch.inftec.ju.ee.test.TestRemote;

public class RemoteServiceLocatorTest {
	@Ignore("Needs running JBoss")
	@Test
	public void canLookup_testFacadeBean() {
//		RemoteServiceLocator.getFacadeEjb(TestNoInterfaceBean.class);
		
//		14:57:28,691 INFO  [org.jboss.as.ejb3.deployment.processors.EjbJndiBindingsDeploymentUnitProcessor] (MSC service thread 1-13) JNDI bindings for session bean named TestRemoteInterfaceBean in deployment unit subdeployment "ee-ear-ejb.jar" of deployment "ee-ear-ear.ear" are as follows:
//
//			java:global/ee-ear-ear/ee-ear-ejb/TestRemoteInterfaceBean!ch.inftec.ju.ee.test.TestRemote
//			java:app/ee-ear-ejb/TestRemoteInterfaceBean!ch.inftec.ju.ee.test.TestRemote
//			java:module/TestRemoteInterfaceBean!ch.inftec.ju.ee.test.TestRemote
//			java:jboss/exported/ee-ear-ear/ee-ear-ejb/TestRemoteInterfaceBean!ch.inftec.ju.ee.test.TestRemote
//			java:global/ee-ear-ear/ee-ear-ejb/TestRemoteInterfaceBean
//			java:app/ee-ear-ejb/TestRemoteInterfaceBean
//			java:module/TestRemoteInterfaceBean
			
		TestRemote testRemote = RemoteServiceLocator.lookup("ejb:ee-ear-ear/ee-ear-ejb/TestRemoteInterfaceBean!" + TestRemote.class.getName());
		Assert.assertEquals("TestRemoteInterfaceBean says hello to Remote Test", testRemote.getGreeting("Remote Test"));
	}
}
