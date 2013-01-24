package ch.inftec.ju.util.context;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.util.JuCollectionUtils;
import ch.inftec.ju.util.TestUtils;

/**
 * Test class for the GenericContext interface.
 * @author Martin
 *
 */
public class GenericContextTest {
	@Test
	public void getObject() {
		GenericContextX c = this.getContext();
		
		// Test different types
		Assert.assertEquals(new Integer(1), c.getObject(Integer.class));
		Assert.assertEquals(new Long(2), c.getObject(Long.class));
		Assert.assertEquals("Hello", c.getObject(String.class));
		Assert.assertEquals("Another String", c.getObject(Object.class));
		
		// Test null
		Assert.assertNull(c.getObject(StringBuffer.class));
	}
	
	@Test
	public void getObjects() {
		GenericContextX c = this.getContext();
		
		// Test different types
		TestUtils.assertCollectionEquals(JuCollectionUtils.arrayList(1), c.getObjects(Integer.class));
		TestUtils.assertCollectionEquals(JuCollectionUtils.arrayList(2L, 3L, 4L, 5L), c.getObjects(Long.class));
		TestUtils.assertCollectionEquals(JuCollectionUtils.arrayList("Hello", "World"), c.getObjects(String.class));
		TestUtils.assertCollectionEquals(JuCollectionUtils.arrayList((Object)"Another String"), c.getObjects(Object.class));
		
		// Test null
		Assert.assertTrue(c.getObjects(StringBuffer.class).isEmpty());
	}
	
//	@Test
//	public void clearObjects() {
//		GenericContextBase c = new GenericContextBase();
//		
//		c.addObjects(Integer.class, 1, 2, 3);
//		Assert.assertEquals(3, c.getObjects(Integer.class).size());
//		
//		c.clearObjects(Integer.class);
//		Assert.assertEquals(0, c.getObjects(Integer.class).size());
//	}
	
//	@Test
//	public void setObject() {
//		GenericContextBase c = new GenericContextBase();
//		
//		c.addObjects(Integer.class, 1, 2, 3);
//		Assert.assertEquals(3, c.getObjects(Integer.class).size());
//		
//		c.setObject(Integer.class, 7);
//		Assert.assertEquals(1, c.getObjects(Integer.class).size());
//		Assert.assertEquals(new Integer(7), c.getObjects(Integer.class).get(0));
//		
//		c.setObject(Integer.class, 9);
//		Assert.assertEquals(1, c.getObjects(Integer.class).size());
//		Assert.assertEquals(new Integer(9), c.getObjects(Integer.class).get(0));
//		
//		c.setObject(Integer.class, null);
//		Assert.assertEquals(1, c.getObjects(Integer.class).size());
//		Assert.assertNull(c.getObjects(Integer.class).get(0));
//	}
	
	@Test
	public void getObjectByEvaluator() {
		GenericContextX c = this.getContext();
		
		Assert.assertEquals(new Integer(3), c.getObject(Integer.class, "val", 3));
		Assert.assertEquals(new Long(44L), c.getObject(Long.class, "44"));
		Assert.assertEquals("Hello", c.getObject(String.class, JuCollectionUtils.stringMap("val2", "World", "val1", "Hello")));
		
		// Empty evaluator
		Assert.assertNull(c.getObject(StringBuffer.class, "bla"));
	}
	
	@Test
	public void getObjectsByEvaluator() {
		GenericContextX c = this.getContext();
		
		TestUtils.assertCollectionEquals(JuCollectionUtils.arrayList(3, 6), c.getObjects(Integer.class, "val", 3));
		TestUtils.assertCollectionEquals(JuCollectionUtils.arrayList(44L), c.getObjects(Long.class, "44"));
		TestUtils.assertCollectionEquals(JuCollectionUtils.arrayList("Hello", "World"), c.getObjects(String.class, JuCollectionUtils.stringMap("val2", "World", "val1", "Hello")));
		
		// Empty evaluator
		Assert.assertTrue(c.getObjects(StringBuffer.class, "bla").isEmpty());
	}
	
	/**
	 * Gets a test generic context.
	 * @return Generic context
	 */
	private GenericContextX getContext() {
		GenericContext c = GenericContextUtils.builder()
		
		.addObjects(Integer.class, 1)
		.addObjects(Long.class, 2L, 3L, 4L)
		.addObjects(Long.class, 5L)
		.addObjects(String.class, "Hello", "World")
		.addObjects(Object.class, "Another String")

		// Integer: Evaluator that returns the object parameter and the parameter*2 (as integers)
		.setObjectEvaluator(Integer.class, new ObjectEvaluatorAdapter<Integer>() {
			@Override
			protected List<Integer> getObjects(String key, Object value) {
				if (value instanceof Integer) {
					Integer i = (Integer)value;
					return JuCollectionUtils.arrayList(i, i*2);
				}
				
				return null;
			}
		})
		
		// Long: Evaluator that returns a parsed String
		.setObjectEvaluator(Long.class, new ObjectEvaluatorAdapter<Long>() {
			@Override
			protected List<Long> getObjects(String parameter) {
				return JuCollectionUtils.arrayList(Long.parseLong(parameter));
			}
		})
		
		// String: Evaluator that returns the values of key1 and key2
		.setObjectEvaluator(String.class, new ObjectEvaluatorAdapter<String>() {
			@Override
			protected List<String> doGetObjects(Map<String, Object> map) {
				return JuCollectionUtils.arrayList(map.get("val1").toString(), map.get("val2").toString());
			}
		})
		
		.build();
		
		return GenericContextUtils.asX(c);
	}
}
