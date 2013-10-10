package ch.inftec.ju.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.ListUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

import ch.inftec.ju.util.comparison.DefaultComparator;
import ch.inftec.ju.util.comparison.EqualityTester;

/**
 * Contains utility methods regarding collections, maps and the like.
 * @author Martin
 *
 */
public final class JuCollectionUtils {
	/**
	 * Don't instantiate.
	 */
	private JuCollectionUtils() {
		throw new AssertionError("use only statically");
	}
	
	/**
	 * Compares two maps. The maps are considered equal if they contain the same keys
	 * and the corresponding keys have the same values (using equals on the keys
	 * and the specified EqualityTester on the values for comparison).
	 * @param m1 Map 1
	 * @param m2 Map 2
	 * @param equalityTester EqualityTester instance to test for equality. If null, a
	 * DefaultComparator will be used
	 * @return True if the maps are equal, false otherwise
	 */
	public static <K, V> boolean mapEquals(Map<K, V> m1, Map<K, V> m2, EqualityTester<V> equalityTester) {
		// Handle null
		if (m1 == null || m2 == null) return m1 == m2;
		
		if (m1.size() != m2.size()) return false;
		
		// Use DefaultComparator if tester is null
		if (equalityTester == null) equalityTester = new DefaultComparator<V>();
		
		Iterator<K> kIterator = m1.keySet().iterator();
		while (kIterator.hasNext()) {
			K k = kIterator.next();
			
			// Make sure m2 contains the key too
			if (!m2.containsKey(k)) return false;
			
			// Make sure m2 has the same value for the key
			if (!equalityTester.equals(m1.get(k), m2.get(k))) return false;
		}
		
		return true;
	}
	
	/**
	 * Compares two maps. The maps are considered equal if they contain the same keys
	 * and the corresponding keys have the same values (using equals on the keys
	 * and values for comparison).
	 * @param m1 Map 1
	 * @param m2 Map 2
	 * @return True if the maps are equal, false otherwise
	 */
	public static <K, V> boolean mapEquals(Map<K, V> m1, Map<K, V> m2) {
		return JuCollectionUtils.mapEquals(m1, m2, null);
	}
	
	/**
	 * Compares to collections. The collections are considered equal if they contain
	 * the same elements in the same order.
	 * @param c1 Collection 1
	 * @param c2 Collection 2
	 * @return True if the collections are equal, false otherwise
	 */
	public static <T> boolean collectionEquals(Collection<T> c1, Collection<T> c2) {
		return ListUtils.isEqualList(c1, c2);
	}
	
	/**
	 * Checks if two collections are equal ignoring the order of the elements, i.e. contain
	 * the same elements, regardless of their order.
	 * @param c1 Collection 1
	 * @param c2 Collection 2
	 * @return True if both collections contain the same elements in arbitrary order
	 */
	public static <T> boolean collectionEqualsIgnoreOrder(List<T> c1, List<T> c2) {
		return ListUtils.intersection(JuCollectionUtils.asList(c1), JuCollectionUtils.asList(c2)).size() == c1.size();
	}
	
	/**
	 * Checks if all specified values are part of the specified collection.
	 * <p>
	 * The collection may contain more than the specified values
	 * @param cCollection Collection
	 * @param values Values the collection must contain, in arbitrary order
	 */
	@SafeVarargs
	public static <T> boolean collectionContains(Collection<T> cCollection, T... values) {
		for (T val : values) {
			if (!cCollection.contains(val)) return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if all specified values are part of the specified collection. Comparison
	 * is done case insensitively.
	 * <p>
	 * The collection may contain more than the specified values.
	 * @param cCollection Collection
	 * @param values Values the collection must contain, in arbitrary order
	 */
	@SafeVarargs
	public static boolean collectionContainsIgnoreCase(Collection<String> cCollection, String... values) {
		for (String val : values) {
			boolean containsVal = false;
			for (String colVal : cCollection) {
				if (colVal.equalsIgnoreCase(val)) {
					containsVal = true;
					break;
				}
			}
			if (!containsVal) return false;
		}
		
		return true;
	}
	
	/**
	 * Returns a map using the provided key (String or Object.toString) and
	 * values (Object) pairs.
	 * @param keyValuePairs Key value pairs
	 * @return HashMap<String, Object> instance
	 */
	public static HashMap<String, Object> stringMap(Object... keyValuePairs) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		for (int i = 1; i < keyValuePairs.length; i+=2) {
			Object obj = keyValuePairs[i-1];
			String key = obj instanceof String ? (String)obj : obj.toString();
			map.put(key, keyValuePairs[i]);
		}
		
		return map;
	}
	
	/**
	 * Returns a new ArrayList containing the specified elements.
	 * <br>
	 * This method is annotated with SafeVarargs to indicate that no faulty
	 * type conversions are performed.
	 * @param elements Elements
	 * @return ArrayList containing the specified elements
	 */
	@SafeVarargs
	public static <T> ArrayList<T> arrayList(T... elements) {
		ArrayList<T> list = new ArrayList<>();
		Collections.addAll(list, elements);
		
		return list;
	}
	
	/**
	 * Returns an unmodifiable list with the specified elements.
	 * <br>
	 * This method is annotated with SafeVarargs to indicate that no faulty
	 * type conversions are performed.
	 * @param elements Elements
	 * @return Unmodifiable list containing the specified elements
	 */
	@SafeVarargs
	public static <T> List<T> unmodifiableList(T... elements) {
		return Collections.unmodifiableList(JuCollectionUtils.arrayList(elements));
	}
	
	/**
	 * Returns an empty collection if the specified collection is null,
	 * otherwise the same reference is returned.
	 * <p>
	 * Can be used to avoid null pointer issues.
	 * @param col Collection
	 * @return Empty collection is specified collection is null, the same collection otherwise
	 */
	public static <T> Collection<T> emptyForNull(Collection<T> col) {
		@SuppressWarnings("unchecked")
		Collection<T> emptyList = Collections.EMPTY_LIST;
		
		return col == null ? emptyList : col;
	}
	
	/**
	 * Checks if the source array is a subset of the destination array, i.e. all
	 * elements of the source array are contained in the destination array.
	 * @param srcArray Source array
	 * @param dstArray Destination array
	 * @return True if the source array is a subset of the destination array
	 */
	public static boolean isSubsetOf(Object[] srcArray, Object[] dstArray) {
		if (dstArray == null || srcArray == null) {
			return dstArray == srcArray;
		}
		
		for (Object o : srcArray) {
			if (!ArrayUtils.contains(dstArray, o)) return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if the the to arrays are equal, i.e. contain the same elements.
	 * Note that the type of the array is not compared, so an empty Long array will be equal to an empty Float
	 * array...
	 * @param a1 Array 1
	 * @param a2 Array 2
	 * @return True if the array are equal, false otherwise
	 */
	public static boolean arrayEquals(Object[] a1, Object[] a2) {
		if (a1 == null || a2 == null) return a1 == a2;
		
		// If the array have the same dimension and type, compare all elements within
		if (a1.length == a2.length) {
			for (int i = 0; i < a1.length; i++) {
				if (!ObjectUtils.equals(a1[i], a2[i])) return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Selects all items of the collection that start with the specified start string and returns them as a new collection.
	 * @param inputCollection Input collection
	 * @param startString Start string. If null, null elements are returned
	 * @param caseSensitive Whether the comparison should be case sensitive
	 * @return Collection of strings starting with the specified start strings
	 */
	public static Collection<String> selectStartingWith(Collection<String> inputCollection, String startString, final boolean caseSensitive) {
		final String startStringConv = startString == null ? null : (caseSensitive ? startString.toUpperCase() : startString);
		
		return CollectionUtils.select(inputCollection, new Predicate<String>() {
			@Override
			public boolean evaluate(String object) {
				if (object == null || startStringConv == null) return object == startStringConv;
				
				String str = caseSensitive ? object.toUpperCase() : object;
				return str.startsWith(startStringConv);
			}
		});
	}
	
	/**
	 * Converts the specified collection to a list.
	 * <p>
	 * If the collection already is a list, it is casted. Otherwise, a new ArrayList is created.
	 * @param collection Collection
	 * @return Collection as List
	 */
	public static <T> List<T> asList(Collection<T> collection) {
		if (collection instanceof List) return (List<T>)collection;
		else return new ArrayList<T>(collection);
	}
	
	/**
	 * Converts the specified collection to an ArrayList.
	 * <p>
	 * If the collection already is an ArrayList, it is casted. Otherwise, a new ArrayList is created.
	 * @param collection Collection
	 * @return Collection as List
	 */
	public static <T> ArrayList<T> asArrayList(Collection<T> collection) {
		if (collection instanceof ArrayList) return (ArrayList<T>)collection;
		else return new ArrayList<T>(collection);
	}
	
	/**
	 * Converts the specified values to an ArrayList.
	 * @param values List of values / array
	 * @return ArrayList
	 */
	@SafeVarargs
	public static <T> ArrayList<T> asArrayList(T... values) {
		ArrayList<T> list = new ArrayList<>();
		for (T value : values) list.add(value);
		
		return list;
	}
	
	/**
	 * Converts the specified values to an ArrayList of the specified type.
	 * @param type Explicit type
	 * @param values List of values / array
	 * @return ArrayList
	 */
	@SafeVarargs
	public static <T, S extends T> ArrayList<T> asTypedArrayList(Class<T> type, S... values) {
		ArrayList<T> list = new ArrayList<>();
		for (T value : values) list.add(value);
		
		return list;
	}
	
	/**
	 * Returns a sorted set with all distinct values of the specified collection in their
	 * natural order
	 * @param collection Collection containing elements
	 * @return Sorted set
	 */
	public static <T> Set<T> asSortedSet(Collection<T> collection) {
		Set<T> set = new TreeSet<>(collection);
		return set;
	}
	
	/**
	 * Returns a sorted set with all distinct values of the specified collection in the order
	 * they first appear.
	 * @param collection Collection containing elements
	 * @return Sorted set
	 */
	public static <T> Set<T> asSameOrderSet(Collection<T> collection) {
		Set<T> set = new LinkedHashSet<>(collection);
		return set;
	}
	
	/**
	 * Creates a new instance of a WeakReferenceIterable.
	 * @return
	 */
	public static <E> WeakReferenceIterable<E> newWeakReferenceIterable() {
		return new WeakReferenceIterableImpl<E>();
	}

	static final class WeakReferenceIterableImpl<E> implements WeakReferenceIterable<E> {
		private final List<ReferenceWrapper<E>> list = new ArrayList<>();
		private final ReferenceQueue<E> queue = new ReferenceQueue<>();
		
		private int listVersion = 0;
		
		private WeakReferenceIterableImpl() {			
		}
		
		/**
		 * Gets the size of the internal list. This is package protected to allow unit tests
		 * to check whether empty reference objects are disposed correctly.
		 * @return Size of internal list (which may contain empty references that are not
		 * returned by the iterator)
		 */
		int getInternalListSize() {
			return this.list.size();
		}
		
		@Override
		public Iterator<E> iterator() {
			return new Iter();
		}

		@Override
		public void add(E element) {
			this.list.add(new ReferenceWrapper<>(element, false, this.queue, this.list));
			this.listVersion++;
		}

		@Override
		public void addWeak(E element) {
			if (element == null) throw new NullPointerException("Null cannot be weak referenced");
			this.list.add(new ReferenceWrapper<>(element, true, this.queue, this.list));
			this.listVersion++;
		}

		@Override
		public void remove(E element) {
			for (Iterator<ReferenceWrapper<E>> iter = this.list.iterator(); iter.hasNext(); ) {
				ReferenceWrapper<E> wrapper = iter.next();
				
				if (ObjectUtils.equals(element, wrapper.get())) {
					iter.remove();
					break;
				}
			}
			
			if (this.list.size() > 0) this.list.get(0).removeDeadReferences();
			this.listVersion++;
		}

		@Override
		public void clear() {
			this.list.clear();
			this.listVersion++;
		}
		
		private final static class ReferenceWrapper<T> {
			private final T strongRef;
			private final WeakReference<T> weakRef;
			private final ReferenceQueue<T> queue;
			private final List<ReferenceWrapper<T>> list;
			
			ReferenceWrapper(T ref, boolean weak, ReferenceQueue<T> queue, List<ReferenceWrapper<T>> list) {
				if (weak) {
					this.weakRef = new WeakReference<T>(ref, queue);
					this.strongRef = null;
				} else {
					this.strongRef = ref;
					this.weakRef = null;
				}
				this.queue = queue;
				this.list = list;
				
				this.removeDeadReferences();
			}
			
			public T get() {
				return this.weakRef != null ? this.weakRef.get() : this.strongRef;
			}
			
			private void removeDeadReferences() {
				Reference<? extends T> ref;
				while ((ref = this.queue.poll()) != null) {
					this.list.remove(ref);
				}
			}			
		}
		
		private final class Iter implements Iterator<E> {
			/**
			 * Index of the current element, i.e. index the remove method will use.
			 * If -1, next hasn't been called yet.
			 */
			private int index = -1;
			
			/**
			 * If null, hasNext hasn't been called yet. If -1, there is no
			 * next element. Otherwise, the index of the next element.
			 */
			private Integer nextIndex = null;
			
			/**
			 * Index of the last element that was removed.
			 */
			private Integer removedIndex = null;
			
			/**
			 * List version to detect concurrent modification problems
			 */
			private int lastListVersion = listVersion;
			
			/**
			 * Strong reference to the next element to make sure it doesn't get garbage collected.
			 */
			private E nextElement; 
			
			@Override
			public boolean hasNext() {
				this.checkConcurrentUpdate();
				
				if (this.nextIndex == null || this.index == this.nextIndex) {
					// Try to get next element, i.e. set the nextIndex and nextElement
					for (int i = this.index + 1; i < list.size(); i++) {
						ReferenceWrapper<E> wrapper = list.get(i);
						this.nextElement = wrapper.get();
						if (this.nextElement == null && wrapper.weakRef != null) {
							// WeakRef that is null, so skip						
						} else {
							// Found next element. Set index
							this.nextIndex = i;
							return true;
						}
					}
					
					// No next element found
					this.nextIndex = -1;
				}
				
				return this.nextIndex >= 0;
			}

			@Override
			public E next() {
				this.checkConcurrentUpdate();
				
				if (this.nextIndex == null || this.index == this.nextIndex) {
					this.hasNext();
				}
				if (this.nextIndex == -1) {
					throw new NoSuchElementException("No more elements in Iterable");
				}
				this.index = this.nextIndex;
				return list.get(this.index).get();
			}

			@Override
			public void remove() {
				this.checkConcurrentUpdate();
				
				if (this.removedIndex != null && this.removedIndex == this.index) {
					throw new IllegalStateException("Element was already removed");
				}
				if (this.index < 0) {
					throw new IllegalStateException("Next hasn't been called yet");
				}
				list.remove(this.index);
				this.index--;
				this.removedIndex = index;
				this.nextIndex--;
				
				this.lastListVersion = listVersion;
			}
			
			private void checkConcurrentUpdate() {
				if (this.lastListVersion != listVersion) {
					throw new ConcurrentModificationException("WeakReferenceIterable has been modified oustide of Iterator");
				}
			}
			
		}
	}	
}
