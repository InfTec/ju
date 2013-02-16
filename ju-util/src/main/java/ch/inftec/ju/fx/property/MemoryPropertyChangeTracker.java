package ch.inftec.ju.fx.property;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * BooleanProperty that tracks a bunch of MemoryProperties and indicates
 * whether one of the has a changed value.
 * <p>
 * The clear() method can be used to remove all properties and to
 * deregister all changeListeners.
 * @author Martin
 *
 */
public class MemoryPropertyChangeTracker extends SimpleBooleanProperty {
	private List<MemoryProperty<?>> properties = new ArrayList<>();
	
	private ChangeListener<Object> changeListener = new ChangeListener<Object>() {
		@Override
		public void changed(ObservableValue<? extends Object> obs,
				Object oldValue, Object newValue) {
			checkAll();
		}			
	};
	
	public void addProperties(MemoryProperty<?>... properties) {
		for (MemoryProperty<?> property : properties) {
			property.addListener(this.changeListener);
			this.properties.add(property);
			if (property.hasChanged()) this.set(true);
		}
	}
	
	/**
	 * Clears all properties and removes the associated change listeners.
	 * <p>
	 * This will set this tracker property to false.
	 */
	public void clear() {
		for (MemoryProperty<?> property : this.properties) {
			property.removeListener(this.changeListener);
		}
		this.set(false);
	}
	
	private void checkAll() {
		for (MemoryProperty<?> property : this.properties) {
			if (property.hasChanged()) {
				this.set(true);
				return;
			}
		}
		this.set(false);
	}
}
