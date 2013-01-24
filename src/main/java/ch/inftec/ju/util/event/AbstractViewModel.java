package ch.inftec.ju.util.event;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Base class for ViewModels. Provides functionality for property change listeners.
 * @author tgdmemae
 *
 */
public abstract class AbstractViewModel {
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
		
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(propertyName,
				listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		this.pcs.firePropertyChange(propertyName, oldValue,
				newValue);
	}
}
