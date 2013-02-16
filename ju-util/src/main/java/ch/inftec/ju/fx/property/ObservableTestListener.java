package ch.inftec.ju.fx.property;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.junit.Assert;

/**
 * Helper class to test ChangeListener calls.
 * @author Martin
 *
 * @param <T> Event object
 */
public class ObservableTestListener<T> implements ChangeListener<T> {
	private int calls = 0;
	private ObservableValue<? extends T> lastSource;
	private T lastEventObject;
	
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
	public ObservableValue<? extends T> assertCall() {
		if (this.calls == 0) Assert.fail("Update event wasn't called");
		this.calls = 0;
		return this.getLastSource();
	}
	
	/**
	 * Asserts that the update method was called exactly once since the last reset. Resets the counter.
	 */
	public ObservableValue<? extends T> assertOneCall() {
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
	public T getLastEventObject() {
		return this.lastEventObject;
	}
	
	/**
	 * Gets the source of the last event object.
	 * @return Source of the last event object
	 */
	public ObservableValue<? extends T> getLastSource() {
		return this.lastSource;
	}

	@Override
	public void changed(ObservableValue<? extends T> src, T oldVal, T newVal) {
		this.calls++;
		this.lastSource = src;
		this.lastEventObject = newVal;
	}
}
