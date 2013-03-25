package ch.inftec.ju.fx.concurrent;

/**
 * Convenience class implementing BackgroundLoaderCallback.loadingFailed, returning the
 * same Throwable to indicate that the TaskStarter should handle/display the exception.
 * @author Martin
 *
 */
public abstract class BackgroundLoaderCallbackAdapter implements BackgroundLoaderCallback {
	@Override
	public Throwable loadingFailed(Throwable t) {
		return t;
	}
}
