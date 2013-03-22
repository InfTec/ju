package ch.inftec.ju.fx.concurrent;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ViewModel for Task execution tracking.
 * <p>
 * Used by {@link TaskExecutorController}.<link>
 * @author tgdmemae
 */
class TaskExecutorViewModel {
	private final Logger logger = LoggerFactory.getLogger(TaskExecutorViewModel.class);
	
	private final Task<?> task;
	
	private BooleanProperty cancelEnabledProperty = new SimpleBooleanProperty();
		
	private ObjectProperty<EventHandler<WorkerStateEvent>> onDoneProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Throwable> exceptionProperty = new SimpleObjectProperty<>();
	
	TaskExecutorViewModel(final Task<?> task) {
		this.task = task;
		this.init();
	}
	
	private void init() {
		EventHandler<WorkerStateEvent> eventHandler = new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				logger.debug("Event: " + event.getEventType());
				updateValues();
				if (task.isDone()) {
					if (onDoneProperty.get() != null) {
						onDoneProperty.get().handle(event);
					}
				}
			}
		};
		this.task.setOnRunning(eventHandler);
		this.task.setOnCancelled(eventHandler);
		this.task.setOnFailed(eventHandler);
		this.task.setOnSucceeded(eventHandler);		
		
		this.updateValues();
	}
	
	public ReadOnlyDoubleProperty progressProperty() {
		return this.task.progressProperty();
	}
	
	public ReadOnlyStringProperty titleProperty() {
		return this.task.titleProperty();
	}
	
	public ReadOnlyStringProperty messageProperty() {
		return this.task.messageProperty();
	}
	
	public ReadOnlyBooleanProperty cancelEnabledProperty() {
		return this.cancelEnabledProperty;
	}
	
	public ReadOnlyObjectProperty<Throwable> exceptionProperty() {
		return this.exceptionProperty;
	}
	
	public void setOnDone(EventHandler<WorkerStateEvent> handler) {
		this.onDoneProperty.set(handler);
	}
	
	private void updateValues() {
		if (this.task.isRunning()) {
			this.cancelEnabledProperty.set(true);
		} else {
			this.cancelEnabledProperty.set(false);
		}
		
		this.exceptionProperty.set(this.task.getException());
	}
	
	public void start() {
		if (!this.task.isRunning()) {
			Thread th = new Thread(task);
			th.setDaemon(true);
			th.start();			
		}
	}
	
	public void cancel() {
		if (!this.task.isDone() && !this.task.isCancelled()) {
			this.task.cancel();
		}
	}
}
