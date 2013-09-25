package ch.inftec.ju.ee.test;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import ch.inftec.ju.ee.client.JndiServiceLocator;
import ch.inftec.ju.ee.client.ServiceLocatorBuilder;
import ch.inftec.ju.ee.test.TestRunnerFacade.TestRunnerContext;

/**
 * JUnit4 ClassRunner that runs test method within the local JBoss context.
 * <p>
 * Note that the runner currently doesn't support any of the JUnit annotations besided @Test,
 * like @Before, @BeforeClass and the like...
 * @author tgdmemae
 *
 */
public class ContainerClassRunner extends BlockJUnit4ClassRunner {
	public ContainerClassRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		Description desc = describeChild(method);
		notifier.fireTestStarted(desc);
		try {
			// Create test context
			TestRunnerContext context = new TestRunnerContext();
			Path localRoot = Paths.get(".").toAbsolutePath();
			context.setLocalRoot(localRoot.toString());
			
			JndiServiceLocator serviceLocator = ServiceLocatorBuilder.buildRemote()
				.remoteServer("localhost", 14447)
				.appName("ee-ear-ear")
				.moduleName("ee-ear-ejb")
				.createServiceLocator();
			
			TestRunnerFacade testRunnerFacade = serviceLocator.lookup(TestRunnerFacade.class);
			testRunnerFacade.runTestMethodInEjbContext(this.getTestClass().getName(), method.getName(), context);
			notifier.fireTestFinished(desc);
		} catch (Throwable t) {
			notifier.fireTestFailure(new Failure(desc, getActualThrowable(t)));
		}
	}
	
	private Throwable getActualThrowable(Throwable t) {
		if (t instanceof InvocationTargetException) {
			return t.getCause();
		} if (t instanceof ExceptionInInitializerError) {
			throw new IllegalStateException("Looks like we couldn't connect to JBoss. Make sure it is running.", t);
		} else {
			return t;
		}		
	}
}
