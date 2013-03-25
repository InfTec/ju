package ch.inftec.ju.fx.concurrent;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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
	private Log4jAppenderViewModel log4jModel;
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Sets a Log4jAppender model to be used by the TaskStarter to display log
	 * messages.
	 * @param log4jModel
	 */
	public void setLog4jModel(Log4jAppenderViewModel log4jModel) {
		this.log4jModel = log4jModel;
	}
	
	private Log4jAppenderViewModel getLog4jModel() {
		if (this.log4jModel == null) {
			this.log4jModel = new Log4jAppenderViewModel();
			this.log4jModel.register();
		}
		return this.log4jModel;
	}
	
	/**
	 * Starts the specified task and calls the callback as soon as finished.
	 * @param task Task
	 * @param callback Callback to be called when finished
	 */
	public void start(final Task<?> task, final BackgroundLoaderCallback callback) {
		// Load the Log4jAppenderViewModel first so we miss as few logs as possible
		Log4jAppenderViewModel log4model = this.getLog4jModel();
		
		final PaneInfo<TaskExecutorController> paneInfo = JuFxUtils.loadPane(IOUtil.getResourceURL("TaskExecutor.fxml", TaskExecutorController.class), TaskExecutorController.class);
		TaskExecutorController controller = paneInfo.getController();
		controller.executeTask(task, new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent ev) {
				if (WorkerStateEvent.WORKER_STATE_FAILED == ev.getEventType()) {
					// Display exception
					JuFxUtils.dialog().showException("Exception", ev.getSource().getException());
				}
				
				JuFxUtils.closeWindow(paneInfo.getPane());
				
				callback.loadingDone(ev.getSource().getValue());
			}
		});
		
		BorderPane pane = new BorderPane();
		pane.setTop(paneInfo.getPane());
		BorderPane.setMargin(paneInfo.getPane(), new Insets(10));
		
		Pane log4jPane = Log4jAppenderController.loadPane(log4model);
		
		pane.setCenter(log4jPane);
		BorderPane.setMargin(log4jPane, new Insets(10));
				
		JuFxUtils.startApplication()
			.pane(pane)
			.title(this.getTitle())
			.start(new ApplicationInitializer() {
				@Override
				public void init(Stage primaryStage) {
					primaryStage.initStyle(StageStyle.TRANSPARENT);
					
					// Make the scene semi-transparent
					primaryStage.getScene().setFill(Color.web("white", 0.5));
				}
			});
	}
}