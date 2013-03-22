package ch.inftec.ju.fx.concurrent;


import java.util.ArrayList;

import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.fx.JuFxUtils;
import ch.inftec.ju.util.fx.JuFxUtils.PaneInfo;

/**
 * Base class to execute FX tasks with GUI feedback.
 * @author Martin
 *
 */
public class BackgroundLoader {
	private Logger logger = LoggerFactory.getLogger(BackgroundLoader.class);
	private ScrollPane scrollPane;
	private VBox vBox;
	private BorderPane borderPane = new BorderPane();
	
	public Node getNotificationNode() {
		if (this.scrollPane == null) {
			this.scrollPane = new ScrollPane();
			this.scrollPane.setFitToWidth(true);
			this.scrollPane.setFitToHeight(true);
			
			this.borderPane.setCenter(this.getNodeParent());
			
			this.scrollPane.setContent(this.borderPane);
		}
		return this.scrollPane;
	}
	
	private VBox getNodeParent() {
		if (this.vBox == null) {
			this.vBox = new VBox();
			this.vBox.setFillWidth(true);
		}
		return this.vBox;
	}
	
	public void execute(final Task<?> task, final BackgroundLoaderCallback callback) {
		JuFxUtils.runInFxThread(new Runnable() {
			@Override
			public void run() {
				final PaneInfo<TaskExecutorController> paneInfo = JuFxUtils.loadPane(IOUtil.getResourceURL("TaskExecutor.fxml", TaskExecutorController.class), TaskExecutorController.class);
				TaskExecutorController controller = paneInfo.getController();
				
				controller.executeTask(task, new EventHandler<WorkerStateEvent>() {
					public void handle(WorkerStateEvent event) {
						paneInfo.getPane().setUserData(event);
						taskDone(event, callback, paneInfo.getPane());
					};
				});
				getNodeParent().getChildren().add(0, paneInfo.getPane());		
			}
		});
	}
	
	private void taskDone(WorkerStateEvent event, final BackgroundLoaderCallback callback, final Pane pane) {
		// Execute the callback (in case the data was loaded successfully)
		if (WorkerStateEvent.WORKER_STATE_SUCCEEDED == event.getEventType()) {
			logger.error("Task succeeded");
			if (callback != null) {
				callback.loadingDone(event.getSource().getValue());
			}
			this.removePane(pane);
		} else if (WorkerStateEvent.WORKER_STATE_FAILED == event.getEventType()) {
			logger.error("Task failed", event.getSource().getException());
			this.showRemoveErrorLink();
		} else if (WorkerStateEvent.WORKER_STATE_CANCELLED == event.getEventType()) {
			logger.error("Task cancelled");
			this.removePane(pane);
		}
		
		
		
		// Handle the task done event in the Swing thread
//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				// Close the background loading panel
//				logger.debug(")
//				WindowManager.getInstance().closeWindow(BackgroundLoadingPanel.this);
//				
//				// Execute the callback (in case the data was loaded successfully)
//				if (WorkerStateEvent.WORKER_STATE_SUCCEEDED == eventType) {
//					if (callback != null) {
//						callback.loadingComplete(data);
//					}
//				} else if (WorkerStateEvent.WORKER_STATE_FAILED == eventType && throwable != null) {
//					MyttsUtil.showErrorMessage("Loading failed", throwable);
//				}
//			}
//		});
	}
	
	private void showRemoveErrorLink() {
		final Hyperlink hlClearErrors = new Hyperlink("Clear Errors");
		hlClearErrors.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent ev) {
				borderPane.setTop(null);
				removeAllErrorPanes();
			}
		});
		borderPane.setTop(hlClearErrors);
	}
	
	private void removeAllErrorPanes() {
		ArrayList<Pane> panesToRemove = new ArrayList<>();
		
		for (Node node : this.getNodeParent().getChildren()) {
			if (node instanceof Pane && node.getUserData() != null) {
				panesToRemove.add((Pane)node);
			}
		}
		
		for (Pane pane : panesToRemove) {
			this.removePane(pane);
		}
	}
	
	private void removePane(final Pane pane) {
		// Nicely fade out the panes before removing them
		
		FadeTransition fade = new FadeTransition(Duration.seconds(1), pane);
		fade.setFromValue(1.0);
		fade.setToValue(0.0);
		
		// Scale not working properly now. Keeps the height of the pane...
//		ScaleTransition scale = new ScaleTransition(Duration.seconds(1), pane);
//		scale.setFromY(1.0);
//		scale.setToY(0.0);
//		
//		SequentialTransition trans = new SequentialTransition();
//		trans.getChildren().addAll(scale);//, fade, scale);
		
		fade.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				getNodeParent().getChildren().remove(pane);
			}
		});
		
		fade.play();
	}
}
