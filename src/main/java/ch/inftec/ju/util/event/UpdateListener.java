package ch.inftec.ju.util.event;

import java.util.EventListener;

/**
 * General purpose UpdateListener. Contains a single updated method with an event
 * containing the source of the update.
 * @author Martin
 * 
 * @param <T> Type of the source
 *
 */
public interface UpdateListener<T> extends EventListener {
	/**
	 * Called when the source of the event is updated.
	 * @param event Object containing the source of the event
	 */
	public void updated(JuEventObject<T> event);
}
