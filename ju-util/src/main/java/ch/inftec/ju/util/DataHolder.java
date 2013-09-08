package ch.inftec.ju.util;

/**
 * Simple data holder class that can hold a value. Can be used
 * to store values from an anonymous inner class for instance (declare an instance
 * as final and use setValue to return the value).
 * @author Martin
 *
 * @param <T>
 */
public class DataHolder<T> {
	private T t;
	
	public T getValue() {
		return this.t; 
	}
	
	public void setValue(T val) {
		this.t = val;
	}
}
