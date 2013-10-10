package ch.inftec.ju.testing.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to verify data after a test has completed.
 * <p>
 * The verifying will run in a separate transaction, so the verifier
 * can be used to test transactional behavior as well.
 * <p>
 * When methods are overridden, we will call all verifiers that are defined
 * on all methods (current and overridden).
 * @author Martin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DataVerify {
	/**
	 * Helper class to set as default value (we cannot set null nor void.class
	 * as default.
	 * @author Martin0
	 *
	 */
	public static final class DEFAULT_DATA_VERIFIER extends DataVerifier {
		@Override
		public void verify() {
			// Do nothing
		}
	}
	
	/**
	 * Sub type of DataVerifier that will be used to perform the data
	 * verifying.
	 * <p>
	 * Default value is DEFAULT_DATA_VERIFIER. In this case, we will look for
	 * a static inner class of the test class that has the same name as the test method
	 * annotated with DataVerify, but starts with a capital letter.
	 * @return Sub type of DataVerifier
	 */
	Class<? extends DataVerifier> value() default DEFAULT_DATA_VERIFIER.class;
}