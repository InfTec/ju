package ch.inftec.ju.fx.concurrent;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ch.inftec.ju.fx.Log4jAppenderController;
import ch.inftec.ju.fx.Log4jAppenderViewModel;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.fx.ApplicationInitializer;
import ch.inftec.ju.util.fx.JuFxUtils;
import ch.inftec.ju.util.fx.JuFxUtils.PaneInfo;

/**
 * Helper class that displays feeback of an FX task as long as the task
 * runs.
 * <p>
 * Automatically hides as soon as the task is finished. For instance, may be used
 * to startup an application while providing startup feedback to the user.
 * @author Martin
 *
 */
public final class TaskStarter {
	private String title = "Starting...";
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Starts the specified task and calls the callback as soon as finished.
	 * @param task Task
	 * @param callback Callback to be called when finished
	 */
	public void start(final Task<?> task, final BackgroundLoaderCallback callback) {
		// Load the Log4jAppenderViewModel first so we miss as few logs as possible
		Log4jAppenderViewModel log4model = new Log4jAppenderViewModel();
		log4model.register();
		
		final PaneInfo<TaskExecutorController> paneInfo = JuFxUtils.loadPane(IOUtil.getResourceURL("TaskExecutor.fxml", TaskExecutorController.class), TaskExecutorController.class);
		TaskExecutorController controller = paneInfo.getController();
		controller.executeTask(task, new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent ev) {
				JuFxUtils.closeWindow(paneInfo.getPane());
				
				callback.loadingDone(ev.getSource().getValue());
			}
		});
		
		BorderPane pane = new BorderPane();
		pane.setTop(paneInfo.getPane());
		
		Pane log4jPane = Log4jAppenderController.loadPane(log4model);
		pane.setCenter(log4jPane);
		
		JuFxUtils.startApplication()
			.pane(pane)
			.title(this.getTitle())
			.start(new ApplicationInitializer() {
				@Override
				public void init(Stage primaryStage) {
					primaryStage.initStyle(StageStyle.TRANSPARENT);
				}
			});
	}
}