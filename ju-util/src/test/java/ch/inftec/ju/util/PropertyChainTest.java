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
			.addResourcePropertyEvaluator("/ch/inftec/ju/util/PropertyChainTest-2.properties", true)
			.addResourcePropertyEvaluator("/ch/inftec/ju/util/PropertyChainTest.properties", true)
			.addSystemPropertyEvaluator()
			.getPropertyChain();
		
		String key = "ch.inftec.ju.util.PropertyChainTest.prop1";
		System.setProperty(key, "val1");
		Assert.assertEquals("val1b", chain.get("prop1"));
		Assert.assertEquals("val1", chain.get(key));
	}
}
