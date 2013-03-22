package ch.inftec.ju.fx.concurrent;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.fx.JuFxUtils;

/**
 * Controller class that can be used to execute a JavaFX class providing
 * visual feedback (and cancellation option) on a Pane.
 * @author tgdmemae
 *
 */
public class TaskExecutorController implements Initializable {
	@FXML private Label txtTitle;
	@FXML private Label txtMessage;
	@FXML private ProgressIndicator piProgress;
	@FXML private Button btnCancel;
	
	private TaskExecutorViewModel model;
	
	@Override
	public void initialize(URL url, ResourceBundle res) {
		System.err.println("Initializing");
		
		this.txtMessage.setText("xxx");
	}
	
	/**
	 * Executes the specified task in this controller.
	 * @param task Task that hasn't been started yet.
	 * @param doneEventHandlerFx EventHandler that is called when the task is done (either cancelled or successfully completed).
	 * The handler will run in the FX application thread
	 */
	public void executeTask(final Task<?> task, EventHandler<WorkerStateEvent> doneEventHandler) {
		AssertUtil.assertNull("Controller supports only one task execution", this.model);
		this.initModel(task, doneEventHandler);
	}
	
	private void initModel(final Task<?> task, final EventHandler<WorkerStateEvent> doneEventHandler) {
		// Make sure the model is initialized in the FX thread, otherwise
		// the Task will complain...
		
		JuFxUtils.runInFxThread(new Runnable() {
			@Override
			public void run() {
				// Not working, results in java.lang.OutOfMemoryError: Java heap space
//				AssertUtil.assertEquals(State.READY, task.getState());
				
				model = new TaskExecutorViewModel(task);
				model.setOnDone(doneEventHandler);
				
				
				txtTitle.textProperty().bind(model.titleProperty());
				txtMessage.textProperty().bind(model.messageProperty());
				
				piProgress.progressProperty().bind(model.progressProperty());

				btnCancel.textProperty().bind(model.buttonTextProperty());
				btnCancel.disableProperty().bind(model.buttonDisabledProperty());
				
				// Run the task
				model.start();
			}
		});
	}
	
	public void cancel(ActionEvent ev) {
		model.cancel();
	}
}