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
import ch.inftec.ju.util.AssertUtil;

public class TaskExecutorController implements Initializable {
	@FXML private Label txtTitle;
	@FXML private Label txtMessage;
	@FXML private Button btnCancel;
	
	private Task<?> task;
	
	@Override
	public void initialize(URL url, ResourceBundle res) {
		System.err.println("Initializing");
		
		this.txtMessage.setText("xxx");
	}
	
	public void setTask(Task<?> task) {
		AssertUtil.assertNull("Controller may only be initialized once", this.task);
		this.task = task;
		
		final EventHandler<WorkerStateEvent> eh = new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent ev) {
				if (ev.getEventType() == WorkerStateEvent.WORKER_STATE_SUCCEEDED) {
					btnCancel.disableProperty().set(true);
					btnCancel.setText("Done");
				}
			}
		};
		this.task.setOnSucceeded(eh);
		
		this.txtTitle.textProperty().bind(this.task.titleProperty());
		this.txtMessage.textProperty().bind(this.task.messageProperty());
	}
		
	public void cancel(ActionEvent ev) {
		btnCancel.disableProperty().set(true);
		btnCancel.setText("Cancelling...");
	}
}
