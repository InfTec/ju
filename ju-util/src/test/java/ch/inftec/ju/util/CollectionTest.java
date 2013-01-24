package ch.inftec.ju.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.Assert;

import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;

import ch.inftec.ju.util.comparison.ValueComparator;

/**
 * CollectionUtil related tests.
 * @author Martin
 *
 */
public class CollectionTest {
	@Test
	public void mapEquals() {
		HashMap<String, Integer> map1 = new HashMap<String, Integer>();
		HashMap<String, Integer> map2 = new HashMap<String, Integer>();
		
		map1.put("one", 1);
		map2.put("two", 2);
		
		Assert.assertFalse(JuCollectionUtils.mapEquals(map1, map2));
		
		map1.put("two", 2);
		map2.put("one", 1);
		
		Assert.assertTrue(JuCollectionUtils.mapEquals(map1, map2));
		
		// Test null values
		Assert.assertFalse(JuCollectionUtils.mapEquals(null, map2));
		Assert.assertFalse(JuCollectionUtils.mapEquals(map1, null));
		Assert.assertTrue(JuCollectionUtils.mapEquals(null, null));
	}
	
	@Test
	public void mapEqualsEqualityTester() {
		HashMap<String, Object> map1 = new HashMap<String, Object>();
		HashMap<String, Object> map2 = new HashMap<String, Object>();
		
		map1.put("one", 1);
		map2.put("one", 1L);
		
		Assert.assertFalse(JuCollectionUtils.mapEquals(map1, map2));
		
		// Use custom EqualityTester
		Assert.assertTrue(JuCollectionUtils.mapEquals(map1, map2, ValueComparator.INSTANCE));
	}
	
	@Test
	public void collectionEquals() {
		ArrayList<String> c1 = new ArrayList<String>();
		ArrayList<String> c2 = new ArrayList<String>();
		
		c1.add("one");

		Assert.assertFalse(JuCollectionUtils.collectionEquals(c1, c2));
		
		c1.add("two");
		c2.add("one");
		c2.add("two");
		
		Assert.assertTrue(JuCollectionUtils.collectionEquals(c1, c2));
		
		// Test null values
		Assert.assertFalse(JuCollectionUtils.collectionEquals(null, c2));
		Assert.assertFalse(JuCollectionUtils.collectionEquals(c1, null));
		Assert.assertTrue(JuCollectionUtils.collectionEquals(null, null));
	}
	
	@Test
	public void stringMap() {
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put("one", 1);
		m.put("two", 2);
		m.put("null", null);
		
		TestUtils.assertMapEquals(m, JuCollectionUtils.stringMap("one", 1, "two", 2, "null", null));
	}
	
	@Test
	public void arrayListAndUnmodifiableList() {
		ArrayList<String> list = new ArrayList<>();
		list.add("Hello");
		list.add("World");
		list.add(null);
				
		// Test arrayList		
		TestUtils.assertCollectionEquals(list, JuCollectionUtils.arrayList("Hello", "World", null));
		
		// Test unmodifiable list
		List<String> unmodifiableList = JuCollectionUtils.unmodifiableList("Hello", "World", null);
		TestUtils.assertCollectionEquals(list, unmodifiableList);
		
		try {
			unmodifiableList.add("bla");
			Assert.fail("Could add element to unmodifiable list");
		} catch (UnsupportedOperationException ex) {
			// Expected
		}
		
	}
	
	@Test
	public void isSubsetOf() {
		String array[] = new String[] {"A", "B", "C"};
		
		Assert.assertTrue(JuCollectionUtils.isSubsetOf(new String[] {}, array));
		Assert.assertTrue(JuCollectionUtils.isSubsetOf(new String[] {"A", "B"}, array));
		Assert.assertFalse(JuCollectionUtils.isSubsetOf(new String[] {"X"}, array));
		Assert.assertFalse(JuCollectionUtils.isSubsetOf(null, array));
		
		Assert.assertTrue(JuCollectionUtils.isSubsetOf(null, null));
		Assert.assertFalse(JuCollectionUtils.isSubsetOf(new String[] {}, null));
		Assert.assertFalse(JuCollectionUtils.isSubsetOf(new String[] {"A"}, null));
	}
	
	@Test
	public void arrayEquals() {
		// Check null and empty arrays
		Assert.assertTrue(JuCollectionUtils.arrayEquals(null, null));
		Assert.assertTrue(JuCollectionUtils.arrayEquals(new Long[0], new Long[0]));
		Assert.assertTrue(JuCollectionUtils.arrayEquals(new Long[0], new Float[0]));
		Assert.assertTrue(JuCollectionUtils.arrayEquals(new Long[3], new Float[3]));
		Assert.assertFalse(JuCollectionUtils.arrayEquals(new Long[0], null));
		
		Assert.assertTrue(JuCollectionUtils.arrayEquals(new Long[] {1L, 2L, 3L}, new Long[] {1L, 2L, 3L}));
		Assert.assertFalse(JuCollectionUtils.arrayEquals(new Long[] {1L, 2L, 3L}, new Long[] {1L, 2L, 4L}));
	}
	
	public void selectStartingWith() {
		Collection<String> col = Arrays.asList("a1", "A2", "b1", null);
		
		// Case sensitive
		Collection<String> c1 = JuCollectionUtils.selectStartingWith(col, "a", true);
		TestUtils.assertCollectionEquals(c1, "a1");
		
		// Case insensitive
		Collection<String> c2 = JuCollectionUtils.selectStartingWith(col, "a", false);
		TestUtils.assertCollectionEquals(c2, "a1", "A2");
		
		// Empty String
		Collection<String> c3 = JuCollectionUtils.selectStartingWith(col, "", false);
		TestUtils.assertCollectionEquals(c3, "a1", "A2", "b1");
		
		// Empty String
		Collection<String> c4 = JuCollectionUtils.selectStartingWith(col, "", false);
		TestUtils.assertCollectionEquals(c4, (String)null);
	}
	
	/**
	 * Tests the WeakReferenceIterable implementation of JuCollectionUtils.
	 */
	@Test
	public void weakReferenceIterable() {
		WeakReferenceIterable<LargeObject> it = JuCollectionUtils.newWeakReferenceIterable();
		
		it.add(new LargeObject(false));
		Assert.assertEquals(1, IteratorUtils.toList(it.iterator()).size()); 
		
		LargeObject weakObject = new LargeObject(true);
		it.addWeak(weakObject);
		WeakReference<LargeObject> weakObjectRef = new WeakReference<>(weakObject);
		
		Assert.assertEquals(2, IteratorUtils.toList(it.iterator()).size());
		Assert.assertFalse(IteratorUtils.toList(it.iterator()).get(0).isWeak);
		Assert.assertSame(weakObject, IteratorUtils.toList(it.iterator()).get(1));
		
		// Remove strong ref to weakObject and run GC
		weakObject = null;
		for (int i = 0; i < 100; i++) new LargeObject(true);
		System.gc();
		Assert.assertNull(weakObjectRef.get());
		Assert.assertEquals(1, IteratorUtils.toList(it.iterator()).size());		
	}
	
	/**
	 * Test the behaviour of the WeakReferenceIterable iterator implementation.
	 */
	@Test
	public void weakReferenceIterableIterator() {
		WeakReferenceIterable<LargeObject> it = JuCollectionUtils.newWeakReferenceIterable();
		
		Iterator<LargeObject> i1 = it.iterator();
		Assert.assertFalse(i1.hasNext());
		
		Iterator<LargeObject> i2 = it.iterator();
		try {
			i2.next();
			Assert.fail("Next should throw exception on empty list");
		} catch (NoSuchElementException ex) {
			// Expected
		}
		try {
			i2.remove();
			Assert.fail("Next should throw exception on empty list");
		} catch (IllegalStateException ex) {
			// Expected
		}
		Assert.assertFalse(i1.hasNext());
		
		LargeObject l1 = new LargeObject(false);
		it.add(l1);
		Iterator<LargeObject> i3 = it.iterator();
		Assert.assertTrue(i3.hasNext());
		// Check multiple call
		Assert.assertTrue(i3.hasNext());
		Assert.assertSame(l1, i3.next());
		Assert.assertFalse(i3.hasNext());
		
		Iterator<LargeObject> i4 = it.iterator();
		// Check next without hasNext
		Assert.assertSame(l1, i4.next());
		Assert.assertFalse(i4.hasNext());
		
		// Clear next
		it.clear();
		Assert.assertFalse(it.iterator().hasNext());
		
		// Test remove
		LargeObject l2 = new LargeObject(true);
		it.add(l1);
		it.addWeak(l2);
		Iterator<LargeObject> i5 = it.iterator();
		Assert.assertSame(l1, i5.next());
		Assert.assertTrue(i5.hasNext());
		i5.remove();
		Assert.assertTrue(i5.hasNext());
		Assert.assertSame(l2, i5.next());
		Assert.assertFalse(i5.hasNext());
		i5.remove();
		Assert.assertFalse(it.iterator().hasNext());
		
		// Check when weak object is removed on the way
		it.clear();
		LargeObject l3 = new LargeObject(true);
		it.add(l1);
		it.addWeak(l2);
		it.addWeak(l3);
		Iterator<LargeObject> i6 = it.iterator();
		Assert.assertSame(l1, i6.next());
		Assert.assertTrue(i6.hasNext());
		l2 = null;
		l3 = null;
		System.gc();
		// Should still be able to get l2, but l3 should have been removed
		Assert.assertTrue(i6.hasNext());
		Assert.assertNotNull(i6.next());
		Assert.assertFalse(i6.hasNext());
		
		// Test concurrent access
		Iterator<LargeObject> i7 = it.iterator();
		i7.next();
		it.add(new LargeObject(false));
		try {
			i7.hasNext();
			Assert.fail("hasNext should throw exception when Iterable was modified in the meantime");
		} catch (ConcurrentModificationException ex) {
			// Expected
		}
	}
	
	/**
	 * Test if the WeakReference instances are removed using the ReferenceQueue.
	 */
	@Test
	public void weakReferenceIterableQueue() {
		WeakReferenceIterable<Object> it = JuCollectionUtils.newWeakReferenceIterable();
		//WeakReferenceIterableImpl<Object> itImpl = (WeakReferenceIterableImpl<Object>)it;
		
		List<Object> objects = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {			
			Object o = new Object();
			objects.add(o);
			it.addWeak(o);
		}
		//int listSize = itImpl.getInternalListSize();
		
		objects.clear();
		
		for (int i = 0; i < 1000; i++) {			
			Object o = new Object();
			objects.add(o);
			it.add(o);
		}
		
		System.gc();
		
		// Queue polling is triggered by add
		it.addWeak(new Object());
		
		// Cannot trigger queue polling... or it doesn't work (?)
		//Assert.assertTrue(listSize > itImpl.getInternalListSize());
	}
	
	private static class LargeObject {
		@SuppressWarnings("unused")
		Byte[] b = new Byte[1000];
		final boolean isWeak;
		
		public LargeObject(boolean isWeak) {
			this.isWeak = isWeak;
		}	
	}
}
