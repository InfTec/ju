package ch.inftec.ju.fx;

import javafx.fxml.FXML;
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
	
	@FXML private TableColumn<LogEntry, Image> colLevel;
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
		
		this.tblLogs.setItems(model.getLogEntries());
		
		this.colLevel.setCellValueFactory(new PropertyValueFactory<LogEntry, Image>("icon"));
		this.colLevel.setCellFactory(new ImageViewCellFactory<LogEntry>());
		this.colLevel.setPrefWidth(50);
		
		this.colMessage.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("message"));
		this.colMessage.setPrefWidth(300);
	}
}
