package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to load test data sets before executing tests.
 * <p>
 * The annotation can be specified on both, class and method level. Processing of annotations is done
 * in the following order:
 * <ol>
 *   <li>Base class(es)</li>
 *   <li>Class</li>
 *   <li>Method (without overridden methods)</li>
 * </ol>
 * @author Martin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DataSet {
	String value();
}