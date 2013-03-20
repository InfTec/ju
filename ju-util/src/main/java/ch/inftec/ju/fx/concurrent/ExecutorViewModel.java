package ch.inftec.ju.fx.concurrent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class ExecutorViewModel {
	private ObservableList<Task<?>> runningTasks = FXCollections.observableArrayList();
	
	public ObservableList<Task<?>> getRunningTasks() {
		return runningTasks;
	}
	
	public void runTask(Task<?> task) {
		//FIXME: Remove or adapt...
	}
}
