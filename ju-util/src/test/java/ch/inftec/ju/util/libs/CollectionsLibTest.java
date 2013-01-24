package ch.inftec.ju.util.libs;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.ListUtils;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.junit.Test;

import ch.inftec.ju.util.JuCollectionUtils;

/**
 * Test class for testing or rather playing with commons-collections framework.
 * @author Martin
 *
 */
public class CollectionsLibTest {
	@Test
	public void unmodifiableList() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		list.add(1);
		list.add(2);

		List<Integer> l = ListUtils.unmodifiableList(list);
		try {
			l.add(3);
			Assert.fail("Could modify unmodifiable list.");
		} catch (UnsupportedOperationException ex) {
			// Expected exception
		}
	}
	
	/**
	 * Test how the DualHashBidiMap behaves. Note that the BidiMap always
	 * behaves in a way that the key-value relations are unique.
	 */
	@Test
	public void bidiMap() {
		BidiMap<Integer, String> m = new DualHashBidiMap<>();
		
		m.put(1, "one");
		m.put(2, "two");
		Assert.assertEquals("one", m.get(1));
		Assert.assertEquals("two", m.get(2));
		Assert.assertEquals(new Integer(1), m.getKey("one"));
		Assert.assertEquals(new Integer(2), m.getKey("two"));
		
		// Override a key
		m.put(1, "1");
		Assert.assertEquals("1", m.get(1));
		Assert.assertEquals(new Integer(1), m.getKey("1"));
		Assert.assertNull(m.getKey("one"));
		
		// Add a duplicate value
		m.put(3, "two");
		Assert.assertEquals(new Integer(3), m.getKey("two"));
		Assert.assertNull(m.get(2));
	}
	
	/**
	 * Tests how the array list behaves.
	 */
	@Test
	public void arrayList() {
		List<String> l = new ArrayList<>();
		
		String s1 = "TestString";
		String s2 = new StringBuilder("Test").append("String").toString();
		Assert.assertNotSame(s1, s2);
		
		l.add(s1);
		Assert.assertTrue(l.contains(s2));
		
		l.add(s2);
		Assert.assertEquals(2, l.size());
		
		l.remove(s1);
		Assert.assertEquals(1, l.size());
		
		l.add(s2);
		l.removeAll(JuCollectionUtils.arrayList(s1));
		
		Assert.assertTrue(l.isEmpty());
		
		l.add(s1);
		l.add(s1);
		l.add("bla");
		for (Iterator<String> iter = l.iterator(); iter.hasNext(); ) {
			if (iter.next().equals(s1)) iter.remove();
		}
		Assert.assertEquals(1, l.size());
		
		try {
			for (Iterator<String> iter = l.iterator(); iter.hasNext(); ) {
				if (l.size() == 1) l.add("Test");			
				iter.next();
				Assert.fail("Should throw ConcurrentModificationException");
			}
		} catch (ConcurrentModificationException ex) {
			// expected
		}
		
		l.clear();
		l.add("Test");
		l.add("Test2");
		
		// Test if remove can be called directly after hasNext
		Iterator<String> i = l.iterator();
		i.hasNext();
		i.next();
		i.hasNext();
		i.remove();
		Assert.assertEquals(1, l.size());
		Assert.assertEquals("Test2", l.get(0));
	}
}
