package ch.inftec.ju.fx.concurrent;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.fx.JuFxUtils;

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
	
	public void setModel(TaskExecutorViewModel model) {
		AssertUtil.assertNull("Model may only be initialized once", this.model);
		this.model = model;
		
		// Make sure the model is initialized in the FX thread, otherwise
		// the Task will complain...
		JuFxUtils.runInFxThread(new Runnable() {
			@Override
			public void run() {
				initModel();
			}
		});
	}
	
	private void initModel() {
		this.txtTitle.textProperty().bind(model.titleProperty());
		this.txtMessage.textProperty().bind(model.messageProperty());
		
		this.piProgress.progressProperty().bind(model.progressProperty());
		
		this.btnCancel.textProperty().bind(model.buttonTextProperty());
		this.btnCancel.disableProperty().bind(model.buttonDisabledProperty());
	}
	
	public void clicked(ActionEvent ev) {
		this.model.performAction();
	}
}
