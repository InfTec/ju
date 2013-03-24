package ch.inftec.ju.fx.concurrent;

import ch.inftec.ju.util.fx.JuFxUtils;
import javafx.concurrent.Task;

/**
 * Test task that performs some 'computation', returns a predefined value
 * and may throw an exception if desired.
 * @author Martin
 *
 */
class MyTask extends Task<String> {
	private final String val;
	private final boolean throwException;
	
	MyTask(String val, boolean throwException) {
		JuFxUtils.initializeFxToolkit();
		
		this.updateProgress(0, 100);
		this.updateTitle(val);
		this.val = val;
		this.throwException = throwException;
	}
	
	@Override
	protected String call() throws Exception {
		TaskExecutorTestGui.logger.debug("call");
		this.updateMessage("Computing...");
		this.updateProgress(20, 100);
		Thread.sleep(1000);
		this.updateMessage("Almost done...");
		this.updateProgress(60, 100);
		Thread.sleep(2000);
		
		if (this.throwException) {
			throw new RuntimeException(val);
		}
		
		this.updateProgress(100, 100);
		
		this.updateMessage("Done!");
		
		return this.val;
	}
}