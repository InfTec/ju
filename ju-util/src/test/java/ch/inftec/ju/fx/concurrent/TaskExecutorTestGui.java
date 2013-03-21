package ch.inftec.ju.fx.concurrent;

import javafx.concurrent.Task;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.fx.JuFxUtils;
import ch.inftec.ju.util.fx.JuFxUtils.PaneInfo;

public class TaskExecutorTestGui {
	private static final Logger logger = LoggerFactory.getLogger(TaskExecutorTestGui.class);
	
	@Test
	public void taskExecutorPane() {
		JuFxUtils.initializeFxToolkit();
		
		PaneInfo<TaskExecutorController> paneInfo = JuFxUtils.loadPane(IOUtil.getResourceURL("TaskExecutor.fxml"), TaskExecutorController.class);
		final TaskExecutorController controller = paneInfo.getController();
		final MyTask task = new MyTask("test");
		
		JuFxUtils.runInFxThread(new Runnable() {
			public void run() {
				TaskExecutorViewModel model = new TaskExecutorViewModel(task);
				controller.setModel(model);
			};
		});
		
//		ExecutorService executor = Executors.newFixedThreadPool(1);
//		executor.submit(task);
		
		JuFxUtils.startApplication("Task Executor", paneInfo.getPane());
	}
	
	private static class MyTask extends Task<String> {
		private final String val;
		
		private MyTask(String val) {
			this.updateProgress(0, 100);
			this.updateTitle(val);
			this.val = val;
		}
		
		@Override
		protected String call() throws Exception {
			logger.debug("call");
			this.updateMessage("Computing...");
			this.updateProgress(20, 100);
			Thread.sleep(1000);
			this.updateMessage("Almost done...");
			this.updateProgress(60, 100);
			Thread.sleep(2000);
			this.updateProgress(100, 100);
			return this.val;
		}
	}
}
