package ch.inftec.ju.fx.concurrent;

import javax.swing.SwingUtilities;

/**
 * Convenience class that can be used as a BackgroundLoaderCallback when the callback
 * should run in the Swing GUI thread.
 * @author Martin
 *
 */
public abstract class BackgroundLoaderCallbackSwing implements BackgroundLoaderCallback {
	@Override
	public void loadingDone(final Object data) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				loadingDoneSwing(data);
			};
		});
	}
	
	/**
	 * Extending classes need to implement this method. It will be run in the Swing GUI thread. 
	 * @param data Data from the background loading task
	 */
	protected abstract void loadingDoneSwing(Object data);
}
