package ch.inftec.ju.util.fx;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ch.inftec.ju.util.JuRuntimeException;

/**
 * Utility class containing JavaFX related functions.
 * @author tgdmemae
 *
 */
public class JuFxUtils {
	/**
	 * Starts a JavaFX application with the specified pane in the primary
	 * stage.
	 * <p>
	 * This method should only be used onces in an Application.
	 * @param paneFxmlUrl URL to pane FXML file
	 */
	public static void startApplication(final String title, URL paneFxmlUrl) {
		try {
			ApplicationImpl.pane = FXMLLoader.load(paneFxmlUrl);
			ApplicationImpl.title = title;
			ApplicationImpl.launch(ApplicationImpl.class);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't launch JavaFX application", ex);
		}
	}
	
	public static class ApplicationImpl extends Application {
		private static Pane pane;
		private static String title;
		
		@Override
		public void start(Stage primaryStage) throws Exception {
			Scene scene = new Scene(pane);
			
			primaryStage.setTitle(title);
			primaryStage.setScene(scene);
			primaryStage.show();
		}
	}
}
