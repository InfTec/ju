package ch.inftec.ju.util.fx;

import javafx.stage.Stage;

/**
 * Helper interface used by JuFxUtils.startApplication to perform
 * custom initialization in a JavaFX stage.
 * @author tgdmemae
 *
 */
public interface ApplicationInitializer {
	public void init(Stage primaryStage);
}
