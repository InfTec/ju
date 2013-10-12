package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to export test data after the test has succeeded.
 * <p>
 * The data is exported as soon as the test succeeds, before any verifiers are run.
 * @author Martin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DataSetExport {
	/**
	 * Name of the data set that contains the tables we should export.
	 * @return Path to the export data set XML file resource 
	 */
	String value();
}