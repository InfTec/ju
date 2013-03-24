package ch.inftec.ju.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuObjectUtils;
import ch.inftec.ju.util.fx.JuFxUtils;
import ch.inftec.ju.util.fx.JuFxUtils.PaneInfo;

/**
 * Controller for the DetailMessage pane that can be used to display a message
 * along with a detailed message in a text box.
 * @author Martin
 *
 */
public class DetailMessageController {
	@FXML private Label lblMessage;
	@FXML private TextArea txaDetailMessage;

	/**
	 * Load a DetailMessage pane for the specified model.
	 * @param model
	 * @return Pane for the model
	 */
	public static Pane loadPane(DetailMessageViewModel model) {
		PaneInfo<DetailMessageController> paneInfo = JuFxUtils.loadPane(IOUtil.getResourceURL("DetailMessage.fxml", DetailMessageController.class), DetailMessageController.class);
		paneInfo.getController().setModel(model);		
		return paneInfo.getPane();
	}
	
	public void setModel(DetailMessageViewModel model) {
		this.lblMessage.textProperty().bind(model.messageProperty());
		this.txaDetailMessage.textProperty().bind(model.detailedMessageProperty());
	}
	
	public void close(ActionEvent ev) {
		Node node = JuObjectUtils.as(ev.getSource(), Node.class);
		JuFxUtils.closeWindow(node);
	}
}
