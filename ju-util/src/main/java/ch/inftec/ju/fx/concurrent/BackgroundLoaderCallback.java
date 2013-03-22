package ch.inftec.ju.fx.concurrent;

/**
 * Callback interface used by the BAckgroundLoader class.
 * @author Martin
 *
 */
public interface BackgroundLoaderCallback {
	/**
	 * Called when the loading is complete.
	 * @param data Data loaded by the task
	 */
	public void loadingDone(Object data);
}
