package ch.inftec.ju.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import ch.inftec.ju.util.JuObjectUtils;
import ch.inftec.ju.util.fx.JuFxUtils;

/**
 * Controller for the DetailMessage pane that can be used to display a message
 * along with a detailed message in a text box.
 * @author Martin
 *
 */
public class DetailMessageController {
	@FXML private Label lblMessage;
	@FXML private TextArea txaDetailMessage;
	
	public void setModel(DetailMessageViewModel model) {
		this.lblMessage.textProperty().bind(model.messageProperty());
		this.txaDetailMessage.textProperty().bind(model.detailedMessageProperty());
	}
	
	public void close(ActionEvent ev) {
		Node node = JuObjectUtils.as(ev.getSource(), Node.class);
		JuFxUtils.closeWindow(node);
	}
}
