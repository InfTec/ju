package ch.inftec.ju.util.fx;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.fx.DetailMessageController;
import ch.inftec.ju.fx.DetailMessageViewModel;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.ThreadUtils;
import ch.inftec.ju.util.XString;

import com.sun.glass.ui.Screen;
import com.sun.javafx.stage.EmbeddedWindow;

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
		private static List<Node> nodes = new ArrayList<>();
		
		@Override
		public void start(Stage primaryStage) throws Exception {
			JuFxUtils.fxInitialized = true;
			
			if (pane == null) {
				pane = new FlowPane();
			}
			for (Node node : nodes) {
				pane.getChildren().add(node);
			}
			
			Scene scene = new Scene(pane);
			
			primaryStage.setTitle(title);
			primaryStage.setScene(scene);
			
			if (ApplicationImpl.initializer != null) {
				ApplicationImpl.initializer.init(primaryStage);
			}
			
			primaryStage.show();
		}
	}
	
	/**
	 * Creates a JFXPanel that contains the specified pane.
	 * @param pane Pane
	 * @param initializer Callback method to inizialize the pane further in the FX application thread.
	 * @return JFXPanel to be used in a Swing app
	 */
	public static JFXPanel createJFXPanel(final Pane pane, final PaneInitializer initializer) {
		JuFxUtils.fxInitialized = true;
		final JFXPanel fxPanel = new JFXPanel();
		
		if (pane.getPrefWidth() > 0 && pane.getPrefHeight() > 0) {
			fxPanel.setPreferredSize(new Dimension((int)pane.getPrefWidth(), (int)pane.getPrefHeight()));
		}
		
		/**
		 * Seems like we need to initialize the Scene later in the
		 * JavaFX thread:
		 * http://docs.oracle.com/javafx/2/swing/swing-fx-interoperability.htm#CHDIEEJE
		 */
		Platform.runLater(JuFxUtils.getFxWrapper(new Runnable() {
			@Override
			public void run() {
				if (initializer != null) {
					initializer.init(pane);
				}
				
				Scene scene = new Scene(pane);
				fxPanel.setScene(scene);
			}
		}));
								
		return fxPanel;
	}
	
	public static JFXPanel createJFXPanel(URL paneFxmlUrl, PaneInitializer initializer) {
		AssertUtil.assertNotNull("FXML URL must not be null", paneFxmlUrl);
		
		try {
			Pane pane = FXMLLoader.load(paneFxmlUrl);
			return JuFxUtils.createJFXPanel(pane, initializer);
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
		return JuFxUtils.runInFxThread(runnable, false);
	}
	
	
	/**
	 * Runs the Runnable in the FX thread.
	 * <p>
	 * If runLater is set to true, the Runnable is added to the event queue, even
	 * if we ARE currently in the JavaFX thread.
	 * @param runnable Runnable containing code to be run in the FX thread
	 * @param runLater If true, the code is always added to the event queue and executed
	 * later, even if we are currently in the FX application thread
	 * @return True if the code was run right away (we ARE in the FX thread),
	 * false it it will be run later
	 */
	public static boolean runInFxThread(Runnable runnable, boolean runLater) {
		JuFxUtils.initializeFxToolkit();
		
		if (Platform.isFxApplicationThread() && !runLater) {
			runnable.run();
			return true;
		} else {
			Platform.runLater(runnable);
			return false;
		}
	}
	
	/**
	 * Runs the specified code in the JavaFX application thread, making sure
	 * it runs through and waiting until it is done.
	 * <p>
	 * This may be helpful for testing for instance as the application thread seems
	 * to be a daemon if no stage is open.
	 * @param runnable Code to be run
	 */
	public static void runAndWaitInFxThread(final Runnable runnable) {
		final ExecutionObserver observer = new ExecutionObserver();
		
		JuFxUtils.runInFxThread(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Throwable ex) {
					observer.ex = ex;
				} finally {
					observer.finished = true;
				}
			}
		});
		
		while (!observer.finished) {
			ThreadUtils.sleep(10);
		}
		
		if (observer.ex != null) {
			if (observer.ex instanceof Error) {
				throw (Error)observer.ex;
			} else {
				throw new JuRuntimeException("Exception thrown while executing Runnable", observer.ex);
			}
		}
	}
	
	private static class ExecutionObserver {
		private Throwable ex;
		private boolean finished = false;
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
	
	/**
	 * Tries to close the window that contains the specified node.
	 * @param node Node in the window
	 * @return True if the window could be closed
	 */
	public static boolean closeWindow(Node node) {
		if (node != null && node.getScene() != null) {
			Window window = node.getScene().getWindow();
			if (window instanceof Stage) {
				((Stage) window).close();
				return true;
			} else if (window instanceof EmbeddedWindow) {
				window.getScene().getRoot().setVisible(false);
			}
		}
		return false;
	}
	
	/**
	 * Gets the Window of the specified node.
	 * @param node Node to get window of
	 * @return Window that contains the node or null if the node is in not window
	 */
	public static Window getWindow(Node node) {
		if (node != null && node.getScene() != null) {
			return node.getScene().getWindow();
		}
		return null;
	}
	
	/**
	 * Calculates the preferred size of the TextArea based on the text within, with the
	 * goal to avoid scroll bars if possible.
	 * <p>
	 * Actually, the PrefColumnCount should have the same result, but at least in JavaFx 2.2, it
	 * doesn't seem to have any effect at all.
	 * <p>
	 * Right now, the method will only change the preferred size if the new size would be bigger
	 * and leave it if it would be smaller.
	 * @param textArea
	 */
	public static void calculatePrefSize(TextArea textArea) {
		/*
		 * We'll multiply the calculated size with a constant to make sure we most certainly won't
		 * need any scroll bars.
		 */
		final double prefSizeFactor = 1.3;
		
		/*
		 * Expected line height in case we don't have a PrefHeight available.
		 */
		final double expectedLineHeight = 18; 
				
		final double expectedDefaultHeight = 200;
		
		// Initialize the PrefColumnSize to the length of the longest line in the Detail Message
		XString xs = XString.parseLines(textArea.textProperty().getValue());
		int lineCount = xs.getLineCount();
		int columnCount = xs.getLongestLineLength();
		
		double calcHeight = (double)lineCount / textArea.getPrefRowCount() * textArea.getPrefHeight() * prefSizeFactor;
		double calcWidth = (double)columnCount / textArea.getPrefColumnCount() * textArea.getPrefWidth() * prefSizeFactor;
		
		double prefHeight = calcHeight > 0 ? calcHeight : expectedLineHeight * lineCount;
		double prefWidth = calcWidth;
		
		if (textArea.getPrefHeight() < 0 && prefHeight > expectedDefaultHeight 
				|| textArea.getPrefHeight() > 0 && prefHeight > textArea.getPrefHeight()) {
			textArea.setPrefHeight(prefHeight);
		}
		if (prefWidth > textArea.getPrefWidth()) {
			textArea.setPrefWidth(prefWidth);
		}
	}
	
	public static void showDetailMessageDialog(DetailMessageViewModel model, Node parent) {
		PaneInfo<DetailMessageController> paneInfo = JuFxUtils.loadPane(IOUtil.getResourceURL("DetailMessage.fxml", DetailMessageController.class), DetailMessageController.class);
		paneInfo.getController().setModel(model);		
		
		JuFxUtils.dialog()
			.pane(model.titleProperty().get(), paneInfo.getPane())
			.showModal(parent);
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
		
		public ApplicationStarter node(Node node) {
			BorderPane borderPane = new BorderPane();
			borderPane.setCenter(node);
			
			return this.pane(borderPane);
		}
		
		public ApplicationStarter pane(URL paneFxmlUrl) {
			try {
				ApplicationImpl.pane = FXMLLoader.load(paneFxmlUrl);
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't launch JavaFX application", ex);
			}
			
			return this;
		}
		
		public ApplicationStarter button(String text, EventHandler<ActionEvent> eventHandler) {
			Button btn = new Button(text);
			btn.setOnAction(eventHandler);
			
			ApplicationImpl.nodes.add(btn);
			
			return this;
		}
		
		/**
		 * Starts the application.
		 * <p>
		 * The method will not return until the stage has been closed.
		 */
		public void start() {
			this.start(null);
		}

		/**
		 * Starts the application and runs the initializer code
		 * in the JavaFX application thread.
		 * <p>
		 * The start method won't return until the stage has been closed.
		 * @param initializer Initializer
		 */
		public void start(ApplicationInitializer initializer) {
			ApplicationImpl.initializer = initializer;
			ApplicationImpl.launch(ApplicationImpl.class);
		}
	}
	
	public static DialogHandler dialog() {
		return new DialogHandler();
	}
	
	public static class DialogHandler {
		private static Logger logger = LoggerFactory.getLogger(DialogHandler.class);
				
		private Pane pane;
		private String title;
		
		private DialogHandler() {
			// Use JuFxUtils.dialog()
		}

		/**
		 * Displays a simple message with an ok button.
		 * @param title Title of the dialog
		 * @param message Message displayed as a label
		 * @param detailedMessage Message displayed in a scrollable text area
		 * @return DialogHandler for chaining
		 */
		public DialogHandler message(String title, String message, String detailedMessage) {
			DetailMessageViewModel model = new DetailMessageViewModel();
			model.messageProperty().set(message);
			model.detailedMessageProperty().set(detailedMessage);
			
			return this.pane(title, DetailMessageController.loadPane(model));
		}
		
		/**
		 * Displays an exception. The message is displayed as
		 * the main message, along with the detailed StackTrace
		 * in a TextField.
		 * @param title
		 * @param t
		 * @return DialogHandler to allow for chaining
		 */
		public DialogHandler exception(String title, Throwable t) {
			DetailMessageViewModel model = DetailMessageViewModel.createByThrowable(t);
			
			return this.pane(title, DetailMessageController.loadPane(model));
		}
		
		/**
		 * Displays a pane.
		 * @param title Title of the dialog
		 * @param pane Pane
		 * @return DialogHandler to allow for chaining
		 */
		public DialogHandler pane(String title, Pane pane) {
			this.title = title;
			this.pane = pane;
			
			return this;
		}
		
		/**
		 * Displays a modal dialog in a JavaFX context with the specified parent.
		 * @param parent Parent of the dialog, used to center the dialog. May be null.
		 */
		public void showModal(final Node parent) {
			Stage stage = new Stage();
			stage.setScene(new Scene(pane));			
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setTitle(title);
			
			// We need to apply the size after the stage is visible, otherwise we won't get any
			// usable preferred size information.
			stage.setOnShown(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent ev) {
					sizeReasonably((Stage) ev.getSource(), parent);
				}
			});

			stage.showAndWait();
		}
		
		/**
		 * Displays a modal dialog in a Swing context with the specified parent frame.
		 * <p>
		 * The method will register a listener on the visibleProperty of the pane and
		 * displose of the dialog if the pane is set to invisible.
		 * @param parentFrame Parent of the dialog, used to center the dialog.
		 */
		public void showModalSwing(final Frame parentFrame) {
			final JDialog dialog = new JDialog(parentFrame);
			dialog.setTitle(this.title);
			dialog.setModal(true);
			dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			this.pane.visibleProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> val,
						Boolean oldVal, Boolean newVal) {
					if (Boolean.FALSE.equals(newVal)) {
						DialogHandler.logger.debug("Disposing modal dialog containing JavaFX pane");
						dialog.dispose();
					}
				}
			});
			
			JFXPanel fxPanel = JuFxUtils.createJFXPanel(this.pane, new PaneInitializer() {
				@Override
				public void init(Pane pane) {
					pane.getPrefHeight();
				}
			});

			// Add a window listener to compute the dialog size later. The sizes
			// are only available when the window is visible...
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(java.awt.event.WindowEvent e) {
					java.awt.Window w = e.getWindow();
					e.getWindow().getPreferredSize();
					
					Rectangle2D preferredBounds = new Rectangle2D(w.getX(), w.getY(), w.getPreferredSize().getWidth(), w.getPreferredSize().getHeight());
					Rectangle2D parentBounds = new Rectangle2D(parentFrame.getX(), parentFrame.getY(), parentFrame.getWidth(), parentFrame.getHeight());
					
					Rectangle2D s = getReasonableSize(preferredBounds, parentBounds);
					
					dialog.setBounds((int)s.getMinX(), (int)s.getMinY(), (int)s.getWidth(), (int)s.getHeight());
				}
			});
			
			dialog.getContentPane().add(fxPanel);
			dialog.setVisible(true); // We'll compute the bounds in the windowOpened event...
		}
		
		private Rectangle2D getReasonableSize(Rectangle2D preferredBounds, Rectangle2D parentBounds) {
			// Get the Screen to display the dialog
			Screen screen = parentBounds != null 
					? Screen.getScreenForLocation(
							(int) (parentBounds.getMinX() + parentBounds.getWidth() / 2), 
							(int) (parentBounds.getMinY() + parentBounds.getHeight() / 2))
					: null;
			if (screen == null) {
				screen = Screen.getMainScreen();
			}
			
			// Make sure the dialog doesn't exceed 70% of the screen size
			
			// Create the unpositioned rectangle for the dialog
			Rectangle2D rect = new Rectangle2D(
					screen.getX(),
					screen.getY(),
					Math.min(preferredBounds.getWidth(), screen.getWidth() * 0.7),
					Math.min(preferredBounds.getHeight(), screen.getHeight() * 0.7));
			
			// Position the rectangle (center over parent, then make sure it's contained in the screen)
			
			Rectangle2D screenBounds = new Rectangle2D(screen.getX(), screen.getY(), screen.getWidth(), screen.getHeight());
			if (parentBounds == null) {
				parentBounds = screenBounds;
			}
			
			rect = GeoFx.center(rect, parentBounds);
			rect = GeoFx.moveToBounds(rect, screenBounds);
			
			return rect;
		}
		
		/**
		 * Sizes the stage to the screen, then makes sure it is not bigger than the
		 * screen and it doesn't overlap it.
		 * <p>
		 * If possible, centers on the parent.
		 * @param stage
		 * @param parent
		 */
		private void sizeReasonably(Stage stage, Node parent) {
			Window window = JuFxUtils.getWindow(parent);
			
			// Get the preferred size of the stage
			Rectangle2D prefRect = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
			Rectangle2D parentBounds = window != null
				? new Rectangle2D(window.getX(), window.getY(), window.getWidth(), window.getHeight())
				: null;

			Rectangle2D rect = this.getReasonableSize(prefRect, parentBounds);
			
			stage.setX(rect.getMinX());
			stage.setY(rect.getMinY());
			stage.setWidth(rect.getWidth());
			stage.setHeight(rect.getHeight());
		}
	}
}
