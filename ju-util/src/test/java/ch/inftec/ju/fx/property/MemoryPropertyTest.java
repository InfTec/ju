package ch.inftec.ju.fx.property;

import junit.framework.Assert;

import org.junit.Test;

public class MemoryPropertyTest {
	@Test
	public void memoryPropertyChangeTracker() {
		MemoryBooleanProperty p1 = new MemoryBooleanProperty(false);
		MemoryBooleanProperty p2 = new MemoryBooleanProperty(true);
		
		MemoryPropertyChangeTracker t = new MemoryPropertyChangeTracker();
		ObservableTestListener<Boolean> l = new ObservableTestListener<>();
		t.addListener(l);
		Assert.assertFalse(t.get());
		
		t.addProperties(p1, p2);
		Assert.assertFalse(t.get());
		
		p1.set(true);
		Assert.assertTrue(t.get());
		l.assertOneCall();
		Assert.assertTrue(t.get());
		
		p1.set(false);
		l.assertOneCall();
		Assert.assertFalse(t.get());
		
		p2.set(false);
		l.assertOneCall();
		Assert.assertTrue(t.get());
		
		t.clear();
		p2.set(true);
		l.assertOneCall();
		Assert.assertFalse(t.get());
		
		p2.set(false);
		l.assertNoCall();
		
	}
}
