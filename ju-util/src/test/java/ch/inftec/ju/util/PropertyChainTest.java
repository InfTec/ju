package ch.inftec.ju.util;


import org.junit.Assert;
import org.junit.Test;

public class PropertyChainTest {
	@Test
	public void builder_buildsSystemPropertyEvaluator() {
		PropertyChain chain = new PropertyChainBuilder()
			.addSystemPropertyEvaluator()
			.getPropertyChain();
		
		String key = "ch.inftec.ju.util.PropertyChainTest.prop1";
		System.setProperty(key, "val1");
		Assert.assertEquals("val1", chain.get(key));
	}
	
	@Test
	public void builder_buildsResourcePropertyEvaluator() {
		PropertyChain chain = new PropertyChainBuilder()
			.addResourcePropertyEvaluator("/ch/inftec/ju/util/PropertyChainTest.properties", false)
			.getPropertyChain();
		
		Assert.assertEquals("val1", chain.get("prop1"));
	}
	
	@Test
	public void resourcePropertyEvaluator_ignoresMissingResource() {
		PropertyChain chain = new PropertyChainBuilder()
			.addResourcePropertyEvaluator("xyz", true)
			.addResourcePropertyEvaluator("/ch/inftec/ju/util/PropertyChainTest.properties", true)
			.getPropertyChain();
		
		Assert.assertEquals("val1", chain.get("prop1"));
	}
	
	@Test
	public void propertyChain_chainsEvaluators() {
		PropertyChain chain = new PropertyChainBuilder()
			.addResourcePropertyEvaluator("/ch/inftec/ju/util/PropertyChainTest-2.properties", false)
			.addResourcePropertyEvaluator("/ch/inftec/ju/util/PropertyChainTest.properties", false)
			.addSystemPropertyEvaluator()
			.getPropertyChain();
		
		String key = "ch.inftec.ju.util.PropertyChainTest.prop1";
		System.setProperty(key, "val1");
		Assert.assertEquals("val1b", chain.get("prop1"));
		Assert.assertEquals("val1", chain.get(key));
	}
	
	@Test
	public void propertyChain_convertsInteger() {
		PropertyChain chain = new PropertyChainBuilder()
			.addResourcePropertyEvaluator("/ch/inftec/ju/util/PropertyChainTest_conversion.properties", false)
			.getPropertyChain();
		
		Assert.assertEquals(new Integer(1), chain.get("intProp1", Integer.class));
		Assert.assertEquals(new Integer(-1), chain.get("intProp2", Integer.class));
	}
	
	@Test
	public void propertyChain_convertsBoolean() {
		PropertyChain chain = new PropertyChainBuilder()
			.addResourcePropertyEvaluator("/ch/inftec/ju/util/PropertyChainTest_conversion.properties", false)
			.getPropertyChain();
		
		Assert.assertTrue(chain.get("booleanProp1", Boolean.class));
		Assert.assertFalse(chain.get("booleanProp2", Boolean.class));
	}
}
