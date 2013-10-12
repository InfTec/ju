package ch.inftec.ju.ee.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ejb.EJBException;

import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import ch.inftec.ju.ee.client.JndiServiceLocator;
import ch.inftec.ju.ee.client.ServiceLocatorBuilder;
import ch.inftec.ju.ee.test.TestRunnerFacade.TestRunnerContext;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.ReflectUtils;

/**
 * JUnit rule that runs a test method within the local JBoss context.
 * <p>
 * Note that this means that the method code will be run in a different JVM than the unit test itself,
 * so keep this in mind when using rules, fields and the like in your tests.
 * <p>
 * Explicitly supported features are:
 * <ul>
 *   <li>Expected exception: @Test(expected=ExpectedException.class)</li>
 * </ul>
 * @author Martin
 *
 */
public class ContainerTestRunnerRule implements TestRule {
	private final TestRunnerType type;
	
	public enum TestRunnerType {
		/**
		 * Test method code and everything else runs in the container.
		 */
		CONTAINER,
		
		/**
		 * Test method code runs remotely, setup and verification runs on the server.
		 * Can be used for web tests (e.g. Selenium)
		 */
		REMOTE_TEST;
	}
	
	/**
	 * Creates a new test runner rule.
	 * <p>
	 * Type CONTAINER will run the test code in the container. Type REMOTE_TEST will run test
	 * code in a local JVM (not the container JVM) and just run the test setup and verification code in the container.
	 * @param type Running type
	 */
	public ContainerTestRunnerRule(TestRunnerType type) {
		this.type = type;
	}
	
	@Override
	public Statement apply(Statement base, Description description) {
		Class<?> testClass = description.getTestClass();
		AssertUtil.assertNotNull("Description must contain test class", testClass);
		String testMethod = description.getMethodName();
		AssertUtil.assertNotNull("Description must contain test method name", testMethod);
		
		Method method = ReflectUtils.getMethod(testClass, testMethod, null);
		
		Statement testStatement = null;
		if (this.type == TestRunnerType.CONTAINER) {
			// Create the test statement, i.e. the statement that invokes the test method annotated
			// with @Test
			Statement containerTestRunnerStatement = new ContainerTestRunnerStatement(method);
			
			// Handle expected exception. We need to handle this explicitly as it is implemented
			// using a statement that we will discard here (as we don't use base)
			Test t = method.getAnnotation(Test.class);
			if (t != null && t.expected() != None.class) {
				testStatement = new ExpectException(containerTestRunnerStatement, t.expected());
			} else {
				testStatement = containerTestRunnerStatement;
			}
		} else {
			// With LOCAL_TEST, just run the original statement locally.
			testStatement = base;
		}
		
		
		// Wrap the testStatement in a ContainerVerifyRunnerStatement to handle
		// possible verification and post processing
		return new ContainerPreAndPostRunnerStatement(method, testStatement);
	}
	
	private static abstract class ContainerRunnerStatement extends Statement {
		protected final Method method;
		
		private ContainerRunnerStatement(Method method) {
			this.method = method;
		}
		
		@Override
		public void evaluate() throws Throwable {
			try {
				// Create test context
				TestRunnerContext context = new TestRunnerContext();
				Path localRoot = Paths.get(".").toAbsolutePath();
				context.setLocalRoot(localRoot.toString());
				
				JndiServiceLocator serviceLocator = ServiceLocatorBuilder.createRemoteByConfigurationFiles();
				
				TestRunnerFacade testRunnerFacade = serviceLocator.lookup(TestRunnerFacade.class);
				this.doEvaluation(testRunnerFacade, context);
			} catch (Throwable t) {
				throw this.getActualThrowable(t);
			}
		}
		
		protected abstract void doEvaluation(TestRunnerFacade facade, TestRunnerContext context) throws Throwable;
		
		private Throwable getActualThrowable(Throwable t) {
			if (t instanceof InvocationTargetException) {
				return t.getCause();
			} else if (t instanceof EJBException) {
				// DataVerifier contain a nested RuntimeException more
				if (t.getCause() != null && t.getCause().getClass() == RuntimeException.class) {
					return t.getCause().getCause();
				} else {
					return t.getCause();
				}
			} else if (t instanceof ExceptionInInitializerError) {
				throw new IllegalStateException("Looks like we couldn't connect to JBoss. Make sure it is running.", t);
			} else if (t.getCause() instanceof ComparisonFailure) {
				return t.getCause();
			} else {
				return t;
			}		
		}
	}
	
	private static class ContainerTestRunnerStatement extends ContainerRunnerStatement {
		private ContainerTestRunnerStatement(Method method) {
			super(method);
		}

		@Override
		protected void doEvaluation(TestRunnerFacade facade, TestRunnerContext context) throws Throwable {
			facade.runTestMethodInEjbContext(new TestRunnerAnnotationHandler(method, context));
//			facade.runTestMethodInEjbContext(method.getDeclaringClass().getName(), method.getName(), context);
		}
	}
	
	private static class ContainerPreAndPostRunnerStatement extends ContainerRunnerStatement {
		private final Statement testStatement;
		
		private ContainerPreAndPostRunnerStatement(Method method, Statement testStatement) {
			super(method);
			
			this.testStatement = testStatement;
		}
		
		@Override
		protected void doEvaluation(TestRunnerFacade facade, TestRunnerContext context) throws Throwable {
			TestRunnerAnnotationHandler handler = new TestRunnerAnnotationHandler(method, context);
			
			// Run the pre actions
			facade.runPreTestActionsInEjbContext(handler);
			
			// Run the test statement first. We'll only run the verifiers if the test statement succeeds
			testStatement.evaluate();
			
			// Run the verifiers now
			facade.runPostTestActionsInEjbContext(handler);
		}
	}
	

}