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
	
	/**
	 * Called when the loading failed with an exception.
	 * <p>
	 * The client has the choice to either handle the failed event himself (by returning null),
	 * translating the Throwable to another Throwable or returning the same Throwable to let
	 * the TaskStarter handle/display the Throwable. 
	 * @param t Throwable that caused the failure
	 * @return Null if the failure was handled, Throwable (translated or not) if it should be
	 * displayed/handled by the caller
	 */
	public Throwable loadingFailed(Throwable t);
}
