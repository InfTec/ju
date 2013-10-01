package ch.inftec.ju.ee.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJBException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import ch.inftec.ju.ee.client.JndiServiceLocator;
import ch.inftec.ju.ee.client.ServiceLocatorBuilder;
import ch.inftec.ju.ee.test.TestRunnerFacade.TestRunnerContext;
import ch.inftec.ju.testing.db.DataVerifier;
import ch.inftec.ju.testing.db.DataVerify;
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
	@Override
	public Statement apply(Statement base, Description description) {
		Class<?> testClass = description.getTestClass();
		AssertUtil.assertNotNull("Description must contain test class", testClass);
		String testMethod = description.getMethodName();
		AssertUtil.assertNotNull("Description must contain test method name", testMethod);
		
		Method method = ReflectUtils.getMethod(testClass, testMethod, null);

		// Create the test statement, i.e. the statement that invokes the test method annotated
		// with @Test
		Statement containerTestRunnerStatement = new ContainerTestRunnerStatement(method);
		Statement testStatement = null;
		
		// Handle expected exception. We need to handle this explicitly as it is implemented
		// using a statement that we will discard here (as we don't use base)
		Test t = method.getAnnotation(Test.class);
		if (t != null && t.expected() != None.class) {
			testStatement = new ExpectException(containerTestRunnerStatement, t.expected());
		} else {
			testStatement = containerTestRunnerStatement;
		}
		
		// Check if we have DataVerifiers that need to be run after the test statement has succeeded
		// without errors (including throwing expected exceptions)
		List<DataVerify> verifiers = ReflectUtils.getAnnotations(method, DataVerify.class, true, false, false);
		List<String> verifierClassNames = new ArrayList<>();
		for (DataVerify verify : verifiers) {
			Class<?> verifierClass = null;
			if (verify.value() == DataVerify.DEFAULT_DATA_VERIFIER.class) {
				String verifierName = StringUtils.capitalize(testMethod);
				Class<?> defaultVerifier = ReflectUtils.getInnerClass(testClass, verifierName);
				AssertUtil.assertNotNull(String.format("Couldn't find Verifier %s as inner class of %s. Make sure it exists and is public static."
							, verifierName, testClass)
						, defaultVerifier);
				
				verifierClass = defaultVerifier;
			} else {
				verifierClass = verify.value();
			}
			
			AssertUtil.assertTrue("Verifier must be of type DataVerifier: " + verifierClass.getName(), DataVerifier.class.isAssignableFrom(verifierClass));
			verifierClassNames.add(verifierClass.getName());
		}
		
		if (verifierClassNames.size() > 0) {
			return new ContainerVerifyRunnerStatement(testStatement, verifierClassNames);
		} else {
			return testStatement;
		}
	}
	
	private static abstract class ContainerRunnerStatement extends Statement {
		@Override
		public void evaluate() throws Throwable {
			try {
				// Create test context
				TestRunnerContext context = new TestRunnerContext();
				Path localRoot = Paths.get(".").toAbsolutePath();
				context.setLocalRoot(localRoot.toString());
				
				// TODO: Allow to define properties dynamically
				JndiServiceLocator serviceLocator = ServiceLocatorBuilder.buildRemote()
					.remoteServer("localhost", 14447)
					.appName("ee-ear-ear")
					.moduleName("ee-ear-ejb")
					.createServiceLocator();
				
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
				if (t.getCause() != null && t.getCause() instanceof RuntimeException) {
					return t.getCause().getCause();
				} else {
					return t.getCause();
				}
			} else if (t instanceof ExceptionInInitializerError) {
				throw new IllegalStateException("Looks like we couldn't connect to JBoss. Make sure it is running.", t);
			} else {
				return t;
			}		
		}
	}
	
	private static class ContainerTestRunnerStatement extends ContainerRunnerStatement {
		private final Method method;
		
		private ContainerTestRunnerStatement(Method method) {
			this.method = method;
		}

		@Override
		protected void doEvaluation(TestRunnerFacade facade, TestRunnerContext context) throws Throwable {
			facade.runTestMethodInEjbContext(method.getDeclaringClass().getName(), method.getName(), context);
		}
	}
	
	private static class ContainerVerifyRunnerStatement extends ContainerRunnerStatement {
		private final Statement testStatement;
		private final List<String> verifierClassNames;
		
		private ContainerVerifyRunnerStatement(Statement testStatement, List<String> verifierClassNames) {
			this.testStatement = testStatement;
			this.verifierClassNames = verifierClassNames;
		}
		
		@Override
		protected void doEvaluation(TestRunnerFacade facade, TestRunnerContext context) throws Throwable {
			// Run the test statement first. We'll only run the verifiers if the test statement succeeds
			testStatement.evaluate();
			
			// Run the verifiers now
			facade.runDataVerifierInEjbContext(this.verifierClassNames.toArray(new String[0]));
		}
	}
}