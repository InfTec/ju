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
}
