package ch.inftec.ju.ee.test.db;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.inftec.ju.ee.db.test.AbstractDbTestCdi;
import ch.inftec.ju.ee.test.Greeter;

/**
 * Test if we can use Arquillian with the AbstractDbTest base class.
 * @author Martin
 *
 */
@RunWith(Arquillian.class)
public class AbstractDbTestCdiTest extends AbstractDbTestCdi {
	@Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
        	.addClass(AbstractDbTestCdi.class) // We need to add this class manually, otherwise the EntityManager producer will not be caught up
            .addClass(Greeter.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        
        return jar;
    }
	
	@Inject
	private Greeter greeter;
	
	@Inject
	private EntityManager injectedEm;

	@Test
	public void entityManager_getsCreatedByRule_beforeInjected() {
		Assert.assertNotNull(this.injectedEm);
	}
	
	@Test
	public void externalInjection_worksAsExpected() {
		Assert.assertEquals("Hello, World!", this.greeter.createGreeting("World"));
	}
}
