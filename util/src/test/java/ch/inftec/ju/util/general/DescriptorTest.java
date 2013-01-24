package ch.inftec.ju.util.general;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.util.event.JuEventUtils;
import ch.inftec.ju.util.event.JuEventUtils.TestUpdateListener;
import ch.inftec.ju.util.general.DescriptorUtils.AbstractDescriptionEvaluator;

/**
 * Descriptor related tests.
 * @author tgdmemae
 *
 */
public class DescriptorTest {
	@Test
	public void descriptor() {
		Descriptor d = DescriptorUtils.newInstance("Name", "Description");
		Assert.assertEquals("Name", d.getName());
		Assert.assertEquals("Description", d.getDescription());
		Assert.assertEquals("Name", d.toString());
	}
	
	@Test
	public void descriptorBuilder() {
		Descriptor d = DescriptorUtils.builder("Name")
				.description("Description")
				.setObject(Integer.class, 1)
				.setObject(Long.class, 2L)
				.getDescriptor();
		
		Assert.assertEquals("Name", d.getName());
		Assert.assertEquals("Description", d.getDescription());
		Assert.assertEquals("Name", d.toString());
		Assert.assertEquals((Integer)1, d.getObject(Integer.class));
		Assert.assertEquals((Long)2L, d.getObject(Long.class));
	}
	
	private String evaluatorDescription;
	
	/**
	 * Tests update events of the descriptor.
	 */
	@Test
	public void descripionEvaluator() {
		AbstractDescriptionEvaluator descriptionEvaluator = new AbstractDescriptionEvaluator() {
			@Override
			protected String evaluateDescription() {
				return evaluatorDescription;
			}
		};
		
		Descriptor d = DescriptorUtils.builder("Name")
				.description("1")
				.evaluator(descriptionEvaluator)
				.getDescriptor();
		
		evaluatorDescription = "2";
		Assert.assertEquals("2", d.getDescription());
		
		// Make sure evaluate is only called after call to updateDescription
		evaluatorDescription = "3";
		Assert.assertEquals("2", d.getDescription());
		descriptionEvaluator.updateDescription();
		Assert.assertEquals("3", d.getDescription());
		
		// Check event
		TestUpdateListener<Descriptor> testListener = JuEventUtils.newTestUpdateListener();
		d.getUpdateNotifier().addListener(testListener);
		evaluatorDescription = "4";
		descriptionEvaluator.updateDescription();
		Assert.assertEquals("4", d.getDescription());
		testListener.assertOneCall();
	}	
	
//	@Test
//	public void descriptorBase() {
//		DescriptorBase db = new DescriptorBase();
//		Descriptor d = db;
//		
//		Assert.assertNull(d.getName());
//		Assert.assertNull(d.getDescription());
//		
//		db.setName("Test");
//		Assert.assertEquals("Test", d.getName());
//		
//		Assert.assertNull(d.getObject(Integer.class));
//		
//		db.setObject(Integer.class, 1);
//		Assert.assertEquals(new Integer(1), d.getObject(Integer.class));
//	}
	
//	@Test
//	public void descriptorDecorator() {
//		DescriptorBase db = new DescriptorBase("BaseName", "BaseDescription");
//		db.setObject(String.class, "Object");
//		
//		DescriptorDecorator dd = new DescriptorDecorator(db);
//		Assert.assertEquals("BaseName", dd.getName());
//		Assert.assertEquals("BaseDescription", dd.getDescription());
//		Assert.assertEquals("Object", dd.getObject(String.class));
//		
//		dd.setPrefix("Hello: ", "World: ");
//		Assert.assertEquals("Hello: BaseName", dd.getName());
//		Assert.assertEquals("World: BaseDescription", dd.getDescription());
//	}
}
