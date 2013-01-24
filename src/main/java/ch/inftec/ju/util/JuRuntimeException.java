package ch.inftec.ju.util;

/**
 * Base class for ju specific runtime exceptions.
 * @author Martin
 *
 */
public class JuRuntimeException extends RuntimeException {

	public JuRuntimeException() {
		super();
	}

	public JuRuntimeException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JuRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public JuRuntimeException(String message) {
		super(message);
	}

	public JuRuntimeException(Throwable cause) {
		super(cause);
	}

}
