package ch.inftec.ju.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class containg thread and concurrenty related helper methods.
 * @author Martin
 *
 */
public class ThreadUtils {
	private static Logger logger = LoggerFactory.getLogger(ThreadUtils.class);
	
	/**
	 * Sleeps the specified amount of milliseconds, breaking
	 * if an (interrupted) exception is thrown.
	 * @param millis Time to wait
	 */
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception ex) {
			logger.warn("Interrupted while sleeping", ex);
		}
	}
	
	/**
	 * Joins the specified thread, breaking if an (interrupted) 
	 * exception is thrown.
	 * @param t
	 */
	public static void join(Thread t) {
		try {
			t.join();
		} catch (Exception ex) {
			logger.warn("Interrupted while joining Thread", ex);
		}
	}
}
