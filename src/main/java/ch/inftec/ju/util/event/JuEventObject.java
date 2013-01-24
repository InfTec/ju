package ch.inftec.ju.util.event;

import java.util.EventObject;

/**
 * Generic extension of the EventObject class.
 * @author Martin
 *
 * @param <T> Type of the source
 */
public class JuEventObject<T> extends EventObject {
	public JuEventObject(T source) {
		super(source);
	}
	
	@Override
	public T getSource() {
		@SuppressWarnings("unchecked")
		T source = (T)super.getSource();
		return source;
	}
}
