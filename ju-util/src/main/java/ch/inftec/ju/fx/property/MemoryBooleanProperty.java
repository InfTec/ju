package ch.inftec.ju.fx.property;

import javafx.beans.property.SimpleBooleanProperty;

/**
 * Extension of the SimpleBooleanProperty that memorizes it's initial value.
 * @author Martin
 *
 */
public class MemoryBooleanProperty extends SimpleBooleanProperty implements MemoryProperty<Boolean> {
	private boolean initialValue;
	
	public MemoryBooleanProperty(boolean initialValue) {
		super(initialValue);
		
		this.initialValue = initialValue;
	}
	
	@Override
	public void resetToInitialValue() {
		set(this.initialValue);
	}
	
	@Override
	public void setAsInitialValue() {
		this.initialValue = get();
	}

	@Override
	public boolean hasChanged() {
		return this.initialValue != this.get();
	}
}
