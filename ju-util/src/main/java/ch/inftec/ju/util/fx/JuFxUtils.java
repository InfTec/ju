package ch.inftec.ju.util.fx;

import java.awt.Dimension;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
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
	private static boolean fxInitialized = false;
	{
		JuFxUtils.initializeFxToolkit();
	}
	
	/**
	 * Initializes the Java FX toolkit.
	 * <p>
	 * This needs to be done to use some of the Java FX functionality like
	 * concurrency classes when we haven't already got a Java FX scene or application
	 * running.
	 */
	public static void initializeFxToolkit() {
		if (!fxInitialized) {
			new JFXPanel();
		}
		fxInitialized = true;
	}
	
	/**
	 * Loads a pane from the specified URL.
	 * <p>
	 * Returns an info object that allows to access the controller and the pane.
	 * @param paneFxmlUrl FXML URL
	 * @param controllerClass Controller class
	 * @return PaneInfo instance
	 */
	public static <T> PaneInfo<T> loadPane(URL paneFxmlUrl, Class<T> controllerClass) {
		try {
			FXMLLoader loader = new FXMLLoader(paneFxmlUrl);
			Pane pane = (Pane)loader.load();
			T controller = loader.getController();
			return new PaneInfo<T>(pane, controller);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't load pane from URL " + paneFxmlUrl, ex);
		}
	}
	
	public static final class PaneInfo<T> {
		private final Pane pane;
		private final T controller;
		
		private PaneInfo(Pane pane, T controller) {
			this.pane = pane;
			this.controller = controller;
		}
		
		public T getController() {
			return controller;
		}
		
		public Pane getPane() {
			return pane;
		}
	}
	
	/**
	 * Gets a builder to configure and start a JavaFX application.
	 * @return ApplicationStarter
	 */
	public static ApplicationStarter startApplication() {
		return new ApplicationStarter();
	}
	
	public static class ApplicationImpl extends Application {
		private static Pane pane;
		private static String title;
		private static ApplicationInitializer initializer;
		
		@Override
		public void start(Stage primaryStage) throws Exception {
			Scene scene = new Scene(pane);
			
			primaryStage.setTitle(title);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			if (ApplicationImpl.initializer != null) {
				ApplicationImpl.initializer.init(primaryStage);
			}
		}
	}
	
	/**
	 * Creates a JFXPanel that contains the specified pane.
	 * @param pane Pane
	 * @return JFXPanel to be used in a Swing app
	 */
	public static JFXPanel createJFXPanel(final Pane pane) {
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
	}
	
	public static JFXPanel createJFXPanel(URL paneFxmlUrl) {
		AssertUtil.assertNotNull("FXML URL must not be null", paneFxmlUrl);
		
		try {
			Pane pane = FXMLLoader.load(paneFxmlUrl);
			return JuFxUtils.createJFXPanel(pane);
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
	
	/**
	 * Runs the Runnable in the FX thread.
	 * <p>
	 * If we already ARE in the FX thread, it is run immediately. Otherwise,
	 * it is added to the event queue and invoked later.
	 * @param runnable Runnable containing code to be run in the FX thread
	 * @return True if the code was run right away (we ARE in the FX thread),
	 * false it it will be run later
	 */
	public static boolean runInFxThread(Runnable runnable) {
		if (Platform.isFxApplicationThread()) {
			runnable.run();
			return true;
		} else {
			Platform.runLater(runnable);
			return false;
		}
	}
	
	/**
	 * Gets the root the node.
	 * @param node Node to get root of
	 * @return Stage of the node or null if it isn't displayed on a stage
	 */
	public static Parent getRoot(Node node) {
		Parent parent = node.getParent();
		while (parent.getParent() != null) {
			parent = parent.getParent();
		}
		return parent;
	}
	
	public static class ApplicationStarter {
		public ApplicationStarter title(String title) {
			ApplicationImpl.title = title;
			return this;
		}
		
		public ApplicationStarter pane(Pane pane) {
			ApplicationImpl.pane = pane;
			return this;
		}
		
		public ApplicationStarter pane(URL paneFxmlUrl) {
			try {
				ApplicationImpl.pane = FXMLLoader.load(paneFxmlUrl);
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't launch JavaFX application", ex);
			}
			
			return this;
		}
		
		public void start() {
			this.start(null);
		}

		/**
		 * Starts the application and runs the initializer code
		 * in the JavaFX application thread.
		 * @param initializer Initializer
		 */
		public void start(ApplicationInitializer initializer) {
			ApplicationImpl.initializer = initializer;
			ApplicationImpl.launch(ApplicationImpl.class);
		}
	}
}
