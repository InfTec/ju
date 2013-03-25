package ch.inftec.ju.fx;

import java.util.HashMap;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import junit.framework.Assert;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import ch.inftec.ju.util.fx.JuFxUtils;

/**
 * ViewModel that builds on the (display neutral) Log4jAppenderModel.
 * @author Martin
 *
 */
public class Log4jAppenderViewModel {
	private Log4jAppenderModel model;
	
	private HashMap<LoggingEvent, LogEntry> loggingEventMapping = new HashMap<>();
	private ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
	
	private IntegerProperty maxLogEntries = new SimpleIntegerProperty();
	
	/**
	 * Creates a new ViewModel backed by the specified Log4jAppenderModel.
	 * @param model
	 */
	public Log4jAppenderViewModel(Log4jAppenderModel model) {
		this.setModel(model);
	}
	
	/**
	 * Sets the model that is the source of data for this view model.
	 * @param model Log4JAppenderModel
	 */
	public void setModel(final Log4jAppenderModel model) {
		Assert.assertNull("Log4jAppenderModel has already been set", this.model);
		
		this.model = model;
		this.maxLogEntries.bind(this.model.maxLogEntriesProperty());
		
		JuFxUtils.runInFxThread(new Runnable() {
			@Override
			public void run() {
				// Add a ListChangeListener to the log events list to keep the lists synchronized
				model.getLogEvents().addListener(new ListChangeListener<LoggingEvent>() {
					@Override
					public void onChanged(Change<? extends LoggingEvent> change) {
						while (change.next()) {
							// We should only have removals and adds, no permutations or updates
							
							for (LoggingEvent event : change.getRemoved()) {
								logEntries.remove(loggingEventMapping.get(event));
							}
							for (LoggingEvent event : change.getAddedSubList()) {
								LogEntry logEntry = new LogEntry(event);
								logEntries.add(0, logEntry);
								loggingEventMapping.put(event, logEntry);
							}
						}
					}
				});
			}
		});
	}
	
	/**
	 * List of log entries, in order of last item added at first position.
	 * @return List of log entries
	 */
	public ObservableList<LogEntry> getLogEntries() {
		return this.logEntries;
	}
	
	public static class LogEntry {
		private Image icon;
		private ImageView imageView;
		private String message;
		
		private LogEntry(LoggingEvent event) {
			// Set icon
			if (event.getLevel() == Level.ERROR) {
				this.icon = ImageLoader.getDefaultLoader().loadImage("exclamation.png");
			} else if (event.getLevel() == Level.WARN) {
				this.icon = ImageLoader.getDefaultLoader().loadImage("error.png");
			} else if (event.getLevel() == Level.INFO) {
				this.icon = ImageLoader.getDefaultLoader().loadImage("information.png");
			} else if (event.getLevel() == Level.DEBUG) {
				this.icon = ImageLoader.getDefaultLoader().loadImage("table.png");
			} else if (event.getLevel() == Level.TRACE) {
				this.icon = ImageLoader.getDefaultLoader().loadImage("table_add.png");
			}
			
			this.message = ObjectUtils.toString(event.getMessage());
		}
		
		public String getMessage() {
			return message;
		}
		
		public Image getIcon() {
			return this.icon;
		}
		
		public ImageView getImageView() {
			if (this.imageView == null) {
				this.imageView = new ImageView(this.icon);
			}
			return this.imageView;
		}
	}
}
