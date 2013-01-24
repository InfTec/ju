package ch.inftec.ju.util;

/**
 * Interface to iterate over strongly and weakly referenced elements.
 * <p>
 * This can be used as an iterable list that can handle weak references. For performance
 * and implementation reasons, only Iterable is implemented (and not Collection or List).
 * <p>
 * Use the JuCollectionUtils to get an instance of a WeakCollectionIterable.
 * @author Martin
 *
 * @param <E> Element type
 */
public interface WeakReferenceIterable<E> extends Iterable<E> {
	/**
	 * Adds a strongly referenced element. The element may be null.
	 * @param element Element
	 */
	public void add(E element);
	
	/**
	 * Adds a weakly referenced element. As soon as the element is marked for
	 * garbage collection, it won't be returned by the iterator anymore.
	 * <p>
	 * Element may not be null.
	 * @param element Element
	 * @throws NullPointerException if the element is null
	 */
	public void addWeak(E element);
	
	/**
	 * Removes the specified element form the iterable.
	 * @param element Element
	 */
	public void remove(E element);
	
	/**
	 * Clear the iterable, i.e. removes all elements.
	 */
	public void clear();
}
