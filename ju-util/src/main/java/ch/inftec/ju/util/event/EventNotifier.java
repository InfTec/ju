package ch.inftec.ju.util.event;

import java.util.EventListener;

/**
 * Interface for an event notifier.
 * <p>
 * Handles adding and removing of listeners.
 * 
 * @author Martin
 *
 * @param <T> Type of the EventListeners
 */

public interface EventNotifier<T extends EventListener> {

	/**
	 * Adds the specified listener to this notifier.
	 * @param listener Listener to be notified
	 */
	public abstract void addListener(T listener);

	/**
	 * Adds the specified listener with a weak reference. That means that when all
	 * hard references to the listener are lost, it will be removed from the notifier
	 * automatically when the garbage collector disposes of the listener.
	 * @param listener  Listener to be added with a weak reference
	 */
	public abstract void addWeakListener(T listener);

	/**
	 * Removes the specified listener from this notifier.
	 * @param listener Listener to be removed
	 */
	public abstract void removeListener(T listener);

}