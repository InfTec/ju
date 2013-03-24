package ch.inftec.ju.fx;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import junit.framework.Assert;
import ch.inftec.ju.fx.Log4jAppenderViewModel.LogEntry;

/**
 * Controller for the DetailMessage pane that can be used to display a message
 * along with a detailed message in a text box.
 * @author Martin
 *
 */
public class Log4jAppenderController {
	@FXML private TableView<LogEntry> tblLogs;
	
	@FXML private TableColumn<LogEntry, String> colMessage;
	
	private Log4jAppenderViewModel model;
	
	public void setModel(Log4jAppenderViewModel model) {
		Assert.assertNull("Model has already been set.", this.model);
		
		this.tblLogs.setItems(model.getLogEntries());
		
		this.colMessage.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("message"));
		this.colMessage.setPrefWidth(300);
	}
}
