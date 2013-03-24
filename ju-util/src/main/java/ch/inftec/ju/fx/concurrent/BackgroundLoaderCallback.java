package ch.inftec.ju.fx.concurrent;

/**
 * Callback interface used by the BackgroundLoader class.
 * @author Martin
 *
 */
public interface BackgroundLoaderCallback {
	/**
	 * Called when the loading is complete. The callback is always
	 * called in the FX application thread.
	 * @param data Data loaded by the task
	 */
	public void loadingDone(Object data);
}
