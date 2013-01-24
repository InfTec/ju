package ch.inftec.ju.util.event;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.util.event.JuEventUtils.TestUpdateListener;
import ch.inftec.ju.util.event.JuEventUtils.UpdateEventNotifier;

/**
 * Class to test event related functionality.
 * @author Martin
 *
 */
public class EventTest {
	private boolean weakCalled = false;
	private boolean strongCalled = false;
	
	@Test
	public void updateEventNotifier() {
		UpdateEventNotifier<EventSender> eventNotifier = JuEventUtils.newUpdateEventNotifier();
		
		// Fire event without listeners
		eventNotifier.fireUpdateEvent(new EventSender(0));
		
		// Add test listener
		TestUpdateListener<EventSender> updateListener = JuEventUtils.newTestUpdateListener();
		eventNotifier.addListener(updateListener);
		eventNotifier.fireUpdateEvent(new EventSender(1));
		updateListener.assertOneCall();
		Assert.assertEquals(1, updateListener.getLastSource().val);
		
		// Test weak listener
		UpdateListener<EventSender> weakListener = new UpdateListener<EventSender>() {
			@Override
			public void updated(JuEventObject<EventSender> event) {
				weakCalled = true;				
			}
		};
		UpdateListener<EventSender> strongListener = new UpdateListener<EventSender>() {
			@Override
			public void updated(JuEventObject<EventSender> event) {
				strongCalled = true;				
			}
		};
		
		eventNotifier.addListener(strongListener);
		eventNotifier.addWeakListener(weakListener);
		
		Assert.assertFalse(this.weakCalled);
		Assert.assertFalse(this.strongCalled);
		
		// Fire event with both references
		eventNotifier.fireUpdateEvent(new EventSender(0));
		
		Assert.assertTrue(this.weakCalled);
		Assert.assertTrue(this.strongCalled);
		
		// Remove references, call GC
		strongListener = null;
		weakListener = null;
		System.gc();
		
		// Repeat test
		this.weakCalled = this.strongCalled = false;
		eventNotifier.fireUpdateEvent(new EventSender(0));
		
		Assert.assertFalse(this.weakCalled);
		Assert.assertTrue(this.strongCalled);		
	}
	
	@Test
	public void testUpdateListener() {
		TestUpdateListener<EventSender> updateListener = JuEventUtils.newTestUpdateListener();
		
		Assert.assertNull(updateListener.getLastSource());
		Assert.assertEquals(0, updateListener.getCalls());
		
		updateListener.updated(new JuEventObject<EventSender>(new EventSender(0)));
		updateListener.updated(new JuEventObject<EventSender>(new EventSender(1)));
		Assert.assertEquals(2, updateListener.getCalls());
		
		updateListener.updated(null);
		updateListener.updated(null);
		try {
			updateListener.assertOneCall();
			Assert.fail("Was called twice");
		} catch (AssertionError ex) {
			
		}
	}
	
	/**
	 * Tests the forwarding of updating events.
	 */
	@Test
	public void testForwardUpdateEvents() {
		TestUpdateListener<String> updateListener = JuEventUtils.newTestUpdateListener();
		
		UpdateEventNotifier<EventSender> eventSource = JuEventUtils.newUpdateEventNotifier();
		UpdateEventNotifier<String> eventTarget = JuEventUtils.newUpdateEventNotifier();
		
		eventTarget.addListener(updateListener);
		eventSource.fireUpdateEvent(new EventSender(0));
		updateListener.assertNoCall();
		
		// Add a weakly and a stronly referenced forwarder
		Object forwardingRef = JuEventUtils.forwardUpdateEvents(eventSource, eventTarget, "Hello", false);
		Assert.assertNotNull(forwardingRef);
		JuEventUtils.forwardUpdateEvents(eventSource, eventTarget, "Hello", true);
		
		eventSource.fireUpdateEvent(new EventSender(0));
		Assert.assertEquals(2, updateListener.resetCalls());		
		
		System.gc();
		eventSource.fireUpdateEvent(new EventSender(0));
		Assert.assertEquals(2, updateListener.resetCalls());
		
		// Remove reference
		forwardingRef = null;
		System.gc();
		eventSource.fireUpdateEvent(new EventSender(0));
		Assert.assertEquals("Hello", updateListener.assertOneCall());
	}
	
	private static class EventSender {
		int val;
		
		EventSender(int val) {
			this.val = val;
		}
	}
}
