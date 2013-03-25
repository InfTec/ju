package ch.inftec.ju.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import junit.framework.Assert;
import ch.inftec.ju.fx.Log4jAppenderViewModel.LogEntry;
import ch.inftec.ju.fx.control.ImageViewCellFactory;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.fx.JuFxUtils;
import ch.inftec.ju.util.fx.JuFxUtils.PaneInfo;

/**
 * Controller for the DetailMessage pane that can be used to display a message
 * along with a detailed message in a text box.
 * @author Martin
 *
 */
public class Log4jAppenderController {
	@FXML private TableView<LogEntry> tblLogs;
	
	@FXML private Label lblDisplayedEntries;
	@FXML private Label lblMaxEntries;
	
	@FXML private TableColumn<LogEntry, Image> colLevel;
	@FXML private TableColumn<LogEntry, String> colTime;
	@FXML private TableColumn<LogEntry, String> colThread;
	@FXML private TableColumn<LogEntry, String> colLogger;
	@FXML private TableColumn<LogEntry, String> colMessage;
	
	private Log4jAppenderViewModel model;

	/**
	 * Loads a pane for the specified model.
	 * @param model Log4jAppenderViewModel
	 * @return Pane for the specified model
	 */
	public static Pane loadPane(Log4jAppenderViewModel model) {
		PaneInfo<Log4jAppenderController> paneInfo = JuFxUtils.loadPane(IOUtil.getResourceURL("Log4jAppender.fxml"), null);
		paneInfo.getController().setModel(model);
		
		return paneInfo.getPane();
	}
	
	public void setModel(Log4jAppenderViewModel model) {
		Assert.assertNull("Model has already been set.", this.model);
		
		this.model = model;
		
		this.lblDisplayedEntries.textProperty().bind(this.model.displayedLogEntriesProperty().asString());
		this.lblMaxEntries.textProperty().bind(this.model.maxLogEntriesProperty().asString());
		
		this.tblLogs.setItems(model.getLogEntries());
		
		this.colLevel.setCellValueFactory(new PropertyValueFactory<LogEntry, Image>("icon"));
		this.colLevel.setCellFactory(new ImageViewCellFactory<LogEntry>());
		
		this.colTime.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("time"));
		
		this.colThread.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("threadName"));
		
		this.colLogger.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("loggerName"));
		
		this.colMessage.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("message"));
	}
}
