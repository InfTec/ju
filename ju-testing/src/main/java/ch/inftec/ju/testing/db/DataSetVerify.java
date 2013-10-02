package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to verify data against a data set after a test has completed.
 * <p>
 * The verifying will run in a separate transaction, so the verifier
 * can be used to test transactional behavior as well.
 * <p>
 * When methods are overridden, we will call all verifiers that are defined
 * on all methods (current and overridden).
 * <p>
 * DataSetVerify will be run before any other {@link DataVerify} verifiers are run.
 * @author Martin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DataSetVerify {
	/**
	 * Resource name of the dataset XML we compare the current data to.
	 * <p>
	 * We will compare the data of all tables that are contained in the dataset.
	 * @return Dataset XML resource name
	 */
	String value();
}