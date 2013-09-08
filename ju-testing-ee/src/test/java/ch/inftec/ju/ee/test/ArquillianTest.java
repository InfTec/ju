package ch.inftec.ju.ee.test;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ArquillianTest {
	@Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClass(Greeter.class)
            .addClass(RequestScopedBean.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
	
	@Inject
	Greeter greeter;
	
	@Inject
	RequestScopedBean requestScopedBean;
	
	@Test
    public void should_create_greeting() {
		Assert.assertEquals(
			"Hello, Earthling!",
		    greeter.createGreeting("Earthling"));
		
		greeter.greet(System.out, "Earthling");
    }
	
	@Test
	public void canInject_requestScopedBean() {
		Assert.assertEquals("RequestScopedBean", this.requestScopedBean.getName());
	}
}
