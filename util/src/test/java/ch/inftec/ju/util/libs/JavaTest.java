package ch.inftec.ju.util.libs;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test class containing general Java tests.
 * @author Martin
 *
 */
public class JavaTest {
	private boolean wasClosed = false;
	
	@Test
	public void instanceOf() {
		Assert.assertTrue(new Integer(1) instanceof Integer);
		Assert.assertTrue("Test" instanceof String);
		Assert.assertTrue("Test" instanceof Object);
		
		// nulls
		Assert.assertFalse(null instanceof String);
		Assert.assertFalse(null instanceof Object);
	}
	
	@Test
	public void j7try() {
		this.wasClosed = false;
		try (MyClass c = new MyClass()) {
			Assert.assertFalse(this.wasClosed);
		}
		Assert.assertTrue(this.wasClosed);
		
		this.wasClosed = false;
		boolean hadException = false;
		try (MyClass c = new MyClass()) {
			Assert.assertFalse(this.wasClosed);
			throw new Exception("Fail");
		} catch (Exception ex) {
			hadException = true;
		}
		Assert.assertTrue(this.wasClosed);
		Assert.assertTrue(hadException);
		
		this.wasClosed = false;
		hadException = false;
		try (MyClass c = new MyClass(true)) {
			Assert.fail("Shouldn't reach here");
		} catch (Exception ex) {
			hadException = true;
		}
		Assert.assertFalse(this.wasClosed);
		Assert.assertTrue(hadException);		
	}
	
	/**
	 * Tests the String.format method.
	 */
	@Test
	public void stringFormat() {
		// Padding
		Assert.assertEquals("X-00005", String.format("X-%05d", 5L));
		Assert.assertEquals("X-123456", String.format("X-%05d", 123456L));
	}
	
	@Test
	public void weakReference() {
		Long[] largeObject = new Long[100000];
		
		ReferenceQueue<Object> queue = new ReferenceQueue<>();
		WeakReference<Long[]> ref = new WeakReference<Long[]>(largeObject, queue);
		
		Assert.assertSame(largeObject, ref.get());
		
		largeObject = null;
		System.gc();
		
		Assert.assertNull(ref.get());
		
		// Doesn't work, obviously not deterministic... Assert.assertSame(ref, queue.poll());
	}
	
	private class MyClass implements AutoCloseable {
		public MyClass() {
			this(false);
		}
		
		public MyClass(boolean fail) {
			if (fail) throw new RuntimeException("Failed");
		}
		
		@Override
		public void close() {
			wasClosed = true;
		}
	}
}
