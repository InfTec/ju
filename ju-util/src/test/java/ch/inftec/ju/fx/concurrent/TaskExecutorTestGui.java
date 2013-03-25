package ch.inftec.ju.fx.concurrent;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.fx.ApplicationInitializer;
import ch.inftec.ju.util.fx.JuFxUtils;
import ch.inftec.ju.util.fx.JuFxUtils.PaneInfo;

public class TaskExecutorTestGui {
	static final Logger logger = LoggerFactory.getLogger(TaskExecutorTestGui.class);
	
	@Test
	public void taskExecutorPane() {
		JuFxUtils.startApplication()
			.title("Task Executor")
			.pane(this.createExecutorPane("test"))
			.start();
	}
	
	private Pane createExecutorPane(String res) {
		PaneInfo<TaskExecutorController> paneInfo = JuFxUtils.loadPane(IOUtil.getResourceURL("TaskExecutor.fxml"), TaskExecutorController.class);
		TaskExecutorController controller = paneInfo.getController();
		MyTask task = new MyTask(res, false);
		controller.executeTask(task, new EventHandler<WorkerStateEvent>() {
			public void handle(WorkerStateEvent event) {
				logger.info("Done: " + event.getSource().getValue());
			};
		});
		
		return paneInfo.getPane();
	}
	
	@Test
	public void multiplePanes() {
		final Pane pane = new FlowPane();
		pane.setPrefSize(100, 100);
		final Button btnRunTask = new Button("Run Task");
		
		JuFxUtils.startApplication()
			.title("TaskExecutor")
			.pane(pane)
			.start(new ApplicationInitializer() {
				@Override
				public void init(final Stage primaryStage) {
					btnRunTask.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent ev) {
							Pane pane = createExecutorPane(Long.toString(System.currentTimeMillis()));
							Stage dialog = new Stage();
							dialog.initOwner(primaryStage);
							//dialog.initModality(Modality.);
							Scene scene = new Scene(pane);
							dialog.setScene(scene);
							dialog.show();
						}
					});
					pane.getChildren().add(btnRunTask);
				}
			});
	}
	
	@Test
	public void backgroundLoader() {
		final BackgroundLoader backgroundLoader = new BackgroundLoader();
		
		final BorderPane pane = new BorderPane();
		pane.setPrefSize(100, 100);
		
		final Button btnRunTask = new Button("Run Task");
		btnRunTask.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent ev) {
				MyTask task = new MyTask("Task " + System.currentTimeMillis(), System.currentTimeMillis() % 2 == 0);
				backgroundLoader.execute(task, new BackgroundLoaderCallbackAdapter() {
					@Override
					public void loadingDone(Object data) {
						logger.debug("Loading done: " + data);
					}
				});
			}
		});
		
		pane.setTop(btnRunTask);
		pane.setCenter(backgroundLoader.getNotificationNode());	
		
		JuFxUtils.startApplication()
			.title("TaskExecutor")
			.pane(pane)
			.start();
	}
}
