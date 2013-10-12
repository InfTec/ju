package ch.inftec.ju.ee.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.EntityManager;

import org.jboss.logging.Logger;
import org.junit.Rule;

import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.ee.client.ServiceLocator;
import ch.inftec.ju.ee.client.ServiceLocatorBuilder;
import ch.inftec.ju.ee.test.ContainerTestRunnerRule.TestRunnerType;
import ch.inftec.ju.ee.test.TestRunnerFacade.TestRunnerContext;
import ch.inftec.ju.util.JuRuntimeException;

/**
 * Base class for container tests, i.e. integration tests that run in the application
 * server VM.
 * <p>
 * Note that any JUnit related features like @Before, @After and so on will run in the
 * client JVM and not on the server.
 * <p>
 * You can, however, use override the doInit method to perform initialization tasks and use
 * the serviceLocator to perform CDI and JNDI lookups in the container.
 * <p>
 * Note that the container will have to provide an EntityManager producer so we can inject it
 * into our test class.
 * @author Martin
 */
public class ContainerTest implements TestRunnerFacade.ContextAware, TestRunnerFacade.Initializable {
    protected Logger _log = Logger.getLogger(this.getClass());
    
    /**
     * Rule that runs the method statements on the remote JBoss container VM
     */
    @Rule
	public ContainerTestRunnerRule testRunnerRule = new ContainerTestRunnerRule(TestRunnerType.CONTAINER);
    
    private TestRunnerContext context;
    
    /**
     * EntityManager provided by the container.
     */
    protected EntityManager em;
    
    /**
     * JuEmUtil instance wrapped around the EntityManager.
     */
    protected JuEmUtil emUtil;

    /**
     * ServiceLocator instance that can be used to lookup JNDI or CDI objects on the server.
     * <p>
     * Note that the ServiceLocator is not configured to lookup remote objects
     */
    protected ServiceLocator serviceLocator;
    
	@Override
	public final void setContext(TestRunnerContext context) {
		this.context = context;		
	}

	@Override
	public final void init() {
		this.serviceLocator = ServiceLocatorBuilder.buildLocal().createServiceLocator();
		this.em = this.serviceLocator.cdi(EntityManager.class);
		this.emUtil = new JuEmUtil(this.em);
		
		this.doInit();
	}

	/**
	 * Extending classes can override this method to perform custom initialization.
	 */
	protected void doInit() {
	}
	
	/**
	 * Gets a Path instance relative to the 'local' test (rather than the JBoss server context).
	 * @param relativePath Relative path like <code>target/file.xml</code>
	 * @param createParentDirectories If true, the parent directories of the path are created if necessary
	 * @return Path relative to the unit test VM
	 */
	protected final Path getLocalPath(String relativePath, boolean createParentDirectories) {
		return this.getLocalPath(Paths.get(relativePath), createParentDirectories);
	}
	
	/**
	 * Gets a Path instance relative to the 'local' test (rather than the JBoss server context).
	 * @param relativePath Relative path like <code>target/file.xml</code>
	 * @param createParentDirectories If true, the parent directories of the path are created if necessary
	 * @return Path relative to the unit test VM
	 */
	protected final Path getLocalPath(Path relativePath, boolean createParentDirectories) {
		Path localRoot = Paths.get(this.context.getLocalRoot());
		Path localPath = localRoot.resolve(relativePath);
		
		if (createParentDirectories) {
			try {
				Files.createDirectories(localPath.getParent());
			} catch (IOException ex) {
				throw new JuRuntimeException("Couldn't create parent directories", ex);
			}
		}
		
		return localPath;
	}
}