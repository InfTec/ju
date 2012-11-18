package ch.inftec.ju.util.comparison;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;


/**
 * Default implementation of an EqualityTester and Comparator. Uses the objects
 * equals and compareTo methods to perform the tests. Includes null comparison
 * where null is considered to be smaller as any instance.
 * @author tgdmemae
 *
 * @param <T> Base type
 */
public class DefaultComparator<T> implements EqualityTester<T>, Comparator<T> {
	/**
	 * Default instance of an object comparator.
	 */
	public static DefaultComparator<Object> INSTANCE = new DefaultComparator<Object>();
	
	@Override	
	public int compare(T o1, T o2) {
		// Handle nulls (null is considered smaller than an instance
		if (o1 == null && o2 == null) return 0;
		if (o1 == null || o2 == null) return o1 == null ? -1 : 1;
		
		// Compare objects (will throw ClassCastException if object 1 doesn't
		// implement Comparable interface
		@SuppressWarnings("unchecked")
		Comparable<T> c1 = (Comparable<T>)o1;
		return c1.compareTo(o2);
	}

	@Override
	public boolean equals(T o1, T o2) {
		return ObjectUtils.equals(o1, o2);
	}
}
