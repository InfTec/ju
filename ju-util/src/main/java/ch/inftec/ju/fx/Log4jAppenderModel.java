package ch.inftec.ju.fx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;

import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.fx.JuFxUtils;

/**
 * Log4j Appender model, i.e. a model that can be registered as a Log4J appender
 * and that allows to access the logging events in a FX thread save manner.
 * @author Martin
 *
 */
public class Log4jAppenderModel {
	private static final int INITIAL_MAX_LOG_ENTRIES = 1000;
	
	private Appender appender;
	
	private List<LoggingEvent> newLogEvents = Collections.synchronizedList(new ArrayList<LoggingEvent>());
	private boolean updatingEvents = false;
	
	private ObservableList<LoggingEvent> logEvents = FXCollections.observableArrayList();
	private IntegerProperty maxLogEntries = new SimpleIntegerProperty(Log4jAppenderModel.INITIAL_MAX_LOG_ENTRIES);
	
	public Log4jAppenderModel() {
		this.maxLogEntries.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				updateLogEvents();
			}
		});
	}
	
	public AppenderRegistrator register() {
		AssertUtil.assertNull("Log4jAppender has already been registered.", this.appender);
		
		this.appender = new Appender(this);
		return new AppenderRegistrator(this.appender);
	}
	
	/**
	 * List of log events, in order of last item added at first position.
	 * @return List of log events
	 */
	public ObservableList<LoggingEvent> getLogEvents() {
		return this.logEvents;
	}
	
	/**
	 * Gets the maximum number of log entries the log may contain.
	 * <p>
	 * Older logs will be disposed of.
	 * @return Maximum number of entries the log may contain
	 */
	public IntegerProperty maxLogEntriesProperty() {
		return this.maxLogEntries;
	}
	
	private void addLogEvent(LoggingEvent event) {
		this.newLogEvents.add(event);

		JuFxUtils.runInFxThread(new Runnable() {
			@Override
			public void run() {
				updateLogEvents();
			}
		});
	}
	
	private void updateLogEvents() {
		// We need to be careful here as we are in a multi-threaded environment.
		// Firing log events might trigger further Logger events, so we need to be careful
		// they don't overlap with out processing, causing concurrent modification exceptions
		// in event listeners.
		
		// As we only call this method in a single thread, we can safely use a flag to
		// indicate that processing is going on to avoid multiple processing (even if in the same thread).
		
		if (!this.updatingEvents) {
			this.updatingEvents = true;
			
			try {
				while (this.newLogEvents.size() > 0 || this.logEvents.size() > this.maxLogEntriesProperty().get()) {
					// Synchronize the list to copy, otherwise we might get concurrent access problems
					List<LoggingEvent> newEvents = null;
					synchronized (this.newLogEvents) {
						newEvents = new ArrayList<>(this.newLogEvents.subList(0, this.newLogEvents.size()));
					}
					
					this.logEvents.addAll(newEvents);
					this.newLogEvents.removeAll(newEvents);
					
					// Remove if we have too many entries
					if (this.logEvents.size() > this.maxLogEntriesProperty().get()) {
						this.logEvents.remove(0, this.logEvents.size() - this.maxLogEntriesProperty().get());
					}
				}
			} finally {
				this.updatingEvents = false;
			}
		}
	}
	
	/**
	 * Implementation of the AppenderSkeleton used to register for events.
	 * @author Martin
	 *
	 */
	private static class Appender extends AppenderSkeleton {
		private final Log4jAppenderModel model;
		private LoggingEvent lastEvent;
		
		public Appender(Log4jAppenderModel model) {
			this.model = model;
		}
		
		protected void append(final LoggingEvent event) {
			if (!(lastEvent == event)) {
				lastEvent = event;
				model.addLogEvent(event);				
			}
		}

		@Override
		public void close() {
		}

		@Override
		public boolean requiresLayout() {
			return false;
		};
	}
	
	public static class AppenderRegistrator {
		private final Appender appender;
		
		private AppenderRegistrator(Appender appender) {
			this.appender = appender;
		}
		
		public AppenderRegistrator setThreshold(Priority threshold) {
			this.appender.setThreshold(threshold);
			return this;
		}
		
		public AppenderRegistrator addToLogger(String name) {
			org.apache.log4j.Logger.getLogger(name).addAppender(this.appender);
			return this;
		}
		
		public AppenderRegistrator addToLogger(Logger logger) {
			return this.addToLogger(logger.getName());
		}
		
		public AppenderRegistrator addToRootLogger() {
			org.apache.log4j.Logger.getRootLogger().addAppender(this.appender);
			return this;
		}
	}
}
