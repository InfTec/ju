package ch.inftec.ju.ee.test;

import javax.enterprise.context.RequestScoped;

/**
 * Bean to test RequestScope with Arquillian.
 * @author Martin
 *
 */
@RequestScoped
public class RequestScopedBean {
	public String getName() {
		return this.getClass().getSimpleName();
	}
}
