package ch.inftec.ju.ee.test.db;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.ee.db.EntityManagerProvider;

/**
 * Test class to test DB (JPA) support with arquillian and weld.
 * @author Martin
 *
 */
@RunWith(Arquillian.class)
public class ArquillianDbTest {
	private static Logger logger = LoggerFactory.getLogger(ArquillianDbTest.class);
	
	@Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
            .addClass(EntityManagerProvider.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        
        logger.debug(jar.toString(Formatters.VERBOSE));
        
        return jar;
    }
	
	@Inject
	EntityManager em;
	
	@Test
    public void entityManager_isOpen() {
		Assert.assertTrue(this.em.isOpen());
    }
}
