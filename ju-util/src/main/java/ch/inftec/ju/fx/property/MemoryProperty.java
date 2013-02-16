package ch.inftec.ju.fx.property;

import javafx.beans.value.ObservableValue;


/**
 * Extension to a (JavaFX) property to make it remember it's initial value
 * and provide a flag whether it has changed regarding to this value.
 * <p>
 * The initial value can be changed to the current value using setAsInitialValue().
 * 
 * @param <T> Type of the Property's value
 * 
 * @author Martin
 *
 */
public interface MemoryProperty<T> extends ObservableValue<T> {
	/**
	 * Resets the property to the initial value.
	 */
	public void resetToInitialValue();
	
	/**
	 * Sets the current value as the new 'initial' value.
	 */
	public void setAsInitialValue();

	/**
	 * Gets whether the value of the property has changed compared to the initial
	 * value.
	 * @return True if the value is not the initial value
	 */
	public boolean hasChanged();
}
