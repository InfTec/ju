package ch.inftec.ju.fx.concurrent;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskStarterTestGui {
	private static Logger logger = LoggerFactory.getLogger(TaskStarterTestGui.class);
	
	@Test
	public void taskStarter() {
		MyTask task = new MyTask("MyRes", false);
		
		new TaskStarter().start(task, new BackgroundLoaderCallback() {
			@Override
			public void loadingDone(Object data) {
				logger.info("Logging done: " + data);
			}
		});
	}
	
	@Test
	public void taskStarterException() {
		MyTask task = new MyTask("MyRes", true);
		
		new TaskStarter().start(task, new BackgroundLoaderCallback() {
			@Override
			public void loadingDone(Object data) {
				logger.info("Logging done: " + data);
			}
		});
	}
}
