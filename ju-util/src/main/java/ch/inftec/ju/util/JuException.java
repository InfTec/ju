package ch.inftec.ju.util;

/**
 * Base class for ju specific exceptions.
 * @author Martin
 *
 */
public class JuException extends Exception {

	public JuException() {
	}

	public JuException(String arg0) {
		super(arg0);
	}

	public JuException(Throwable arg0) {
		super(arg0);
	}

	public JuException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
