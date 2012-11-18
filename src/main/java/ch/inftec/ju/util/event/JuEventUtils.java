package ch.inftec.ju.util.event;

import java.util.EventListener;

import org.junit.Assert;

import ch.inftec.ju.util.JuCollectionUtils;
import ch.inftec.ju.util.WeakReferenceIterable;

/**
 * Contains utility methods for working with events.
 * @author Martin
 *
 */
public final class JuEventUtils {
	/**
	 * Creates a new EventNotifier for UpdateListener events.
	 * @return UpdateEventNotifier instance
	 */
	public static <T> UpdateEventNotifier<T> newUpdateEventNotifier() {
		return new UpdateEventNotifier<>();
	}
	
	/**
	 * Creates a new TestUpdateListener instance.
	 * <p>
	 * A TestUpdateListener can be used to test UpdateListener events.
	 * @return TestUpdateListener instance
	 */
	public static <T> TestUpdateListener<T> newTestUpdateListener() {
		return new TestUpdateListener<>();
	}
	
	/**
	 * Adds a (either stronly or weakly referenced) listener to the sourceNotifier that forwards 
	 * all update events to the specified target.
	 * <p>
	 * Hold on to the reference returned as long as the forwarding should be performed.
	 * @param sourceNotifier Source EventNotifier
	 * @param targetNotifier Target UpdateEventNotifier
	 * @param forwardingSource Source object that will be used as source of the forwarded events
	 * @param strongReference If true, the forwarding listener is added strongly referenced. If false, it is
	 * added weakly referenced, so the reference returned by the method must be kept as long as the forwarding
	 * should be in place. If it's reset, the forwarder will be subject for garbage collection.
	 * @return Reference to the forwarder. Keep if weak referenced.
	 */
	public static <T1, T2> Object forwardUpdateEvents(EventNotifier<UpdateListener<T1>> sourceNotifier, UpdateEventNotifier<T2> targetNotifier, T2 forwardingSource, boolean strongReference) {
		ForwardingUpdateListener<T1, T2> forwardingListener = new ForwardingUpdateListener<>(targetNotifier, forwardingSource);
		if (strongReference) {
			sourceNotifier.addListener(forwardingListener);
		} else {
			sourceNotifier.addWeakListener(forwardingListener);
		}
		
		return forwardingListener;
	}
	
	private static class ForwardingUpdateListener<T1, T2> implements UpdateListener<T1> {
		private final UpdateEventNotifier<T2> targetNotifier;
		private final T2 forwardingSource;		
		
		ForwardingUpdateListener(UpdateEventNotifier<T2> targetNotifier, T2 forwardingSource) {
			this.targetNotifier = targetNotifier;
			this.forwardingSource = forwardingSource; 
			
		}
		@Override
		public void updated(JuEventObject<T1> event) {
			this.targetNotifier.fireUpdateEvent(this.forwardingSource);
		}		
	}
	
	/**
	 * Abstract base class for an event notifier.
	 * <p>
	 * Handles adding and removing of listeners. Extending classes can use the
	 * getListeners method to iterate over all registered listeners.
	 * @author Martin
	 *
	 * @param <T> Type of the EventListeners
	 */
	public static abstract class AbstractEventNotifier<T extends EventListener> implements EventNotifier<T> {
		private final WeakReferenceIterable<T> listeners = JuCollectionUtils.newWeakReferenceIterable();
		
		@Override
		public final void addListener(T listener) {
			this.listeners.add(listener);
		}

		@Override
		public final void addWeakListener(T listener) {
			this.listeners.addWeak(listener);
		}

		@Override
		public final void removeListener(T listener) {
			this.listeners.remove(listener);
		}
		
		/**
		 * Gets an iterator that iterates over all registered listeners.
		 * @return Iterator
		 */
		protected final Iterable<T> getListeners() {
			return this.listeners;
		}
	}

	/**
	 * Implementation of an EventNotifier for UpdateListeners.
	 * @author Martin
	 *
	 * @param <T> Type of the sender
	 */
	public static class UpdateEventNotifier<T> extends AbstractEventNotifier<UpdateListener<T>> {
		protected UpdateEventNotifier() {			
		}
		
		/**
		 * Fires an update event notifying all registered listeners.
		 * @param sender Sender of the event
		 */
		public void fireUpdateEvent(T sender) {
			JuEventObject<T> o = new JuEventObject<>(sender);
			for (UpdateListener<T> listener : this.getListeners()) {
				listener.updated(o);
			}
		}
	}
	
	/**
	 * Implementation of the UpdateListener interface that can be used for testing.
	 * <p>
	 * Use the JuEventUtils.newTestUpdateListener method to get an instance
	 * @author tgdmemae
	 *
	 * @param <T> Source type
	 */
	public static class TestUpdateListener<T> implements UpdateListener<T> {
		private int calls = 0;
		private JuEventObject<T> lastEventObject;
		
		private TestUpdateListener() {			
		}
		
		@Override
		public void updated(JuEventObject<T> event) {
			this.calls++;
			this.lastEventObject = event;
		}
		
		/**
		 * Gets the number of calls, i.e. how often the update method was called since
		 * the last resetCalls call.
		 * @return Number of update calls
		 */
		public int getCalls() {
			return this.calls;
		}
		
		/**
		 * Returns the current calls and resets them.
		 * @return Current number of update calls
		 */
		public int resetCalls() {
			int currentCalls = this.calls;
			this.calls = 0;
			return currentCalls;
		}
		
		/**
		 * Asserts that the update method was called since the last reset, no matter how many times. Resets the counter.
		 * @return Last event object's source
		 */
		public T assertCall() {
			if (this.calls == 0) Assert.fail("Update event wasn't called");
			this.calls = 0;
			return this.getLastSource();
		}
		
		/**
		 * Asserts that the update method was called exactly once since the last reset. Resets the counter.
		 */
		public T assertOneCall() {
			if (this.calls != 1) Assert.fail("Update event wasn't called exactly once, but " + this.calls + "times.");
			this.calls = 0;
			return this.getLastSource();
		}
		
		/**
		 * Asserts that the update method wasn't called since the last reset.
		 */
		public void assertNoCall() {
			if (this.calls > 0) Assert.fail("Update event was called. Times: " + this.calls);
		}
		
		/**
		 * Gets the event object passed by the last event.
		 * @return Last event object
		 */
		public JuEventObject<T> getLastEventObject() {
			return this.lastEventObject;
		}
		
		/**
		 * Gets the source of the last event object.
		 * @return Source of the last event object
		 */
		public T getLastSource() {
			return this.getLastEventObject() == null ? null : this.getLastEventObject().getSource();
		}
	}
	
}
