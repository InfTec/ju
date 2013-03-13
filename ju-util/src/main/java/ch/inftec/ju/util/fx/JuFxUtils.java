package ch.inftec.ju.util.fx;

import java.awt.Dimension;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ch.inftec.ju.util.AssertUtil;
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
	
	public static JFXPanel createJFXPanel(URL paneFxmlUrl) {
		AssertUtil.assertNotNull("FXML URL must not be null", paneFxmlUrl);
		
		try {
			final Pane pane = FXMLLoader.load(paneFxmlUrl);
			final JFXPanel fxPanel = new JFXPanel();
			fxPanel.setPreferredSize(new Dimension((int)pane.getPrefWidth(), (int)pane.getPrefHeight()));
			
			/**
			 * Seems like we need to initialize the Scene later in the
			 * JavaFX thread:
			 * http://docs.oracle.com/javafx/2/swing/swing-fx-interoperability.htm#CHDIEEJE
			 */
			Platform.runLater(JuFxUtils.getFxWrapper(new Runnable() {
				@Override
				public void run() {
					Scene scene = new Scene(pane);
					fxPanel.setScene(scene);
				}
			}));
									
			return fxPanel;
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't create JFXPanel", ex);
		}
	}
	
	/**
	 * Workaround method for JavaFX problem that exceptions get swallowed
	 * when using Platform.runLater.
	 * See: http://stackoverflow.com/questions/12318861/javafx-2-catching-all-runtime-exceptions
	 * <p>
	 * Note that this will only work on code directly executed in the
	 * Runnable, NOT for code happening later, e.g. through event
	 * handling...
	 * TODO: Remove as soon as Bug is fixed (JDK8?)
	 * @param r
	 * @return
	 */
	public static Runnable getFxWrapper(final Runnable r) {
	    return new Runnable() {

	        @Override
	        public void run() {
	            try {
	                r.run();
	            } catch (Throwable t) {
	                if (Thread.getDefaultUncaughtExceptionHandler() != null) {
	                	Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), t);
	                } else {
	                	throw t; // Just throw the exception, should result in JavaFX logging it to System.err
	                }
	            }
	        }
	    };
	}
}
