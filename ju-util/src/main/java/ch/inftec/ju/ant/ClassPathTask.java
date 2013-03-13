package ch.inftec.ju.ant;

import java.net.URL;

/**
 * Ant task to print the location of a specific class (as defined by the className attribute).
 * <p>
 * Can be used for classpath trouble debugging.
 * @author Martin
 *
 */
public class ClassPathTask {
	private String resourceName;

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public void execute() {
		URL url = this.getClass().getResource(this.getResourceName());
		System.out.println("Path Info: " + (url == null ? "NULL" : url.getPath()));
	}
}
