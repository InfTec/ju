package ch.inftec.ju.fx;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.fx.JuFxUtils;

public class Log4jAppenderViewModel {
	private Appender appender;
	
	private List<LogEntry> newLogEntries = new ArrayList<>();
	
	private ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
	
	public void register() {
		AssertUtil.assertNull("Log4jAppender has already been registered.", this.appender);
		this.appender = new Appender(this);
		Logger.getRootLogger().addAppender(this.appender);
	}
	
	/**
	 * List of log entries, in order of last item added at first position.
	 * @return List of log entries
	 */
	public ObservableList<LogEntry> getLogEntries() {
		return this.logEntries;
	}
	
	private void addLogEntry(LogEntry logEntry) {
		synchronized(this.newLogEntries) {
			this.newLogEntries.add(0, logEntry);
		}
		
		JuFxUtils.runInFxThread(new Runnable() {
			 @Override
			public void run() {
				updateLogEntries();
			}
		});
	}
	
	private void updateLogEntries() {
		synchronized(this.newLogEntries) {
			this.logEntries.addAll(0, this.newLogEntries);
			this.newLogEntries.clear();
		}
	}
	
	public static class LogEntry {
		private Image icon;
		private ImageView imageView;
		private String message;
		
		private LogEntry() {
			// Only created internally
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
	
	private static class Appender extends AppenderSkeleton {
		private final Log4jAppenderViewModel model;
		
		public Appender(Log4jAppenderViewModel model) {
			this.model = model;
		}
		
		protected void append(final LoggingEvent event) {
			LogEntry logEntry = new LogEntry();
			
			// Set icon
			if (event.getLevel() == Level.ERROR) {
				logEntry.icon = ImageLoader.getDefaultLoader().loadImage("exclamation.png");
			} else if (event.getLevel() == Level.WARN) {
				logEntry.icon = ImageLoader.getDefaultLoader().loadImage("error.png");
			} else if (event.getLevel() == Level.INFO) {
				logEntry.icon = ImageLoader.getDefaultLoader().loadImage("information.png");
			} else if (event.getLevel() == Level.DEBUG) {
				logEntry.icon = ImageLoader.getDefaultLoader().loadImage("table.png");
			} else if (event.getLevel() == Level.TRACE) {
				logEntry.icon = ImageLoader.getDefaultLoader().loadImage("table_add.png");
			}
			
			logEntry.message = ObjectUtils.toString(event.getMessage());
			
			model.addLogEntry(logEntry);
		}

		@Override
		public void close() {
		}

		@Override
		public boolean requiresLayout() {
			return false;
		};
	}
}
