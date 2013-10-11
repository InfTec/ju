package ch.inftec.ju.ee.test;

import org.junit.Rule;

import ch.inftec.ju.ee.test.ContainerTestRunnerRule.TestRunnerType;
import ch.inftec.ju.ee.webtest.WebTest;

/**
 * Base class for test cases that run as (remote) web tests, but need a running container
 * for data loading and web request handling.
 * @author Martin
 *
 */
public class WebContainerTest extends WebTest {
	/**
	 * Rule that performs test setup and verification on the server.
	 */
	@Rule
	public ContainerTestRunnerRule testRunnerRule = new ContainerTestRunnerRule(TestRunnerType.REMOTE_TEST);
}
