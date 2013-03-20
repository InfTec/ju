package ch.inftec.ju.fx.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.Task;

import org.junit.Test;

import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.fx.JuFxUtils;
import ch.inftec.ju.util.fx.JuFxUtils.PaneInfo;

public class TaskExecutorTestGui {
	@Test
	public void taskExecutorPane() {
		JuFxUtils.initializeFxToolkit();
		
		PaneInfo<TaskExecutorController> paneInfo = JuFxUtils.loadPane(IOUtil.getResourceURL("TaskExecutor.fxml"), TaskExecutorController.class);
		TaskExecutorController controller = paneInfo.getController();
		MyTask task = new MyTask("test");
		controller.setTask(task);
		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(task);
		
		JuFxUtils.startApplication("Task Executor", paneInfo.getPane());
	}
	
	private static class MyTask extends Task<String> {
		private final String val;
		
		private MyTask(String val) {
			this.updateTitle(val);
			this.val = val;
		}
		
		@Override
		protected String call() throws Exception {
			this.updateMessage("Computing...");
			Thread.sleep(1000);
			this.updateMessage("Almost done...");
			Thread.sleep(2000);
			return this.val;
		}
	}
}
