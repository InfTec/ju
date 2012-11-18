package ch.inftec.ju.util.comparison;

import java.math.BigDecimal;
import java.util.Comparator;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.util.comparison.DefaultComparator;
import ch.inftec.ju.util.comparison.EqualityTester;
import ch.inftec.ju.util.comparison.ValueComparator;

/**
 * Test class for comparison classes.
 * @author tgdmemae
 *
 */
public class ComparisonTest {
	@Test
	public void valueComparatorEquals() {
		ValueComparator<Object> cObj = ValueComparator.INSTANCE;
		
		// Nulls
		Assert.assertTrue(cObj.equals(null, null));
		Assert.assertFalse(cObj.equals(null, "something"));
		Assert.assertFalse(cObj.equals("something", null));
				
		// Integers
		Assert.assertTrue(cObj.equals(new BigDecimal(8), 8));
		Assert.assertTrue(cObj.equals(new BigDecimal(8), 8L));
		Assert.assertTrue(cObj.equals(8, new BigDecimal(8)));
		Assert.assertTrue(cObj.equals(8, 8L));
		
		// Floats
		Assert.assertTrue(cObj.equals(8.2, 8.2f));
		
		// Strings
		Assert.assertTrue(cObj.equals("Test", "Test"));
	}
	
	@Test
	public void defaultComparatorEquals() {
		EqualityTester<Object> cObj = new DefaultComparator<Object>();
		
		// Nulls
		Assert.assertTrue(cObj.equals(null, null));
		Assert.assertFalse(cObj.equals(null, "something"));
		Assert.assertFalse(cObj.equals("something", null));
		
		// Instances
		Assert.assertTrue(cObj.equals(1, 1));
		Assert.assertFalse(cObj.equals(1, 1L));
		Assert.assertFalse(cObj.equals(1, "test"));
		Assert.assertFalse(cObj.equals(new Object(), new Object()));
	}
	
	@Test
	public void defaultComparatorCompareTo() {
		Comparator<Object> cObj = new DefaultComparator<Object>();
		
		// Nulls
		Assert.assertTrue(cObj.compare(null, null) == 0);
		Assert.assertTrue(cObj.compare(null, "something") == -1);
		Assert.assertTrue(cObj.compare("something", null) == 1);
				
		// Instances
		Assert.assertTrue(cObj.compare(1, 1) == 0);
		
		// Make sure incompatible types cannot be compared
		try {
			cObj.compare(1L, 1);
			Assert.fail("Expected Class-Cast-Exception");
		} catch (ClassCastException ex) {
			// Expected
		}
	}
	
	@Test
	public void compare() {
//		ValueComparator<Object> cObject = new ValueComparator<Object>();
//		
//		// Nulls
//		Assert.assertEquals(cObject.compare(null, null), 0);
//		Assert.assertEquals(cObject.compare(null, "something"), -1);
//		Assert.assertEquals(cObject.compare("something", null), 1);
//		
//		
//		
//		// Nulls
//		assertTrue(DrUtil.valEquals(null, null));
//		assertFalse(DrUtil.valEquals(null, "something"));
//		assertFalse(DrUtil.valEquals("something", null));
//				
//		// Integers
//		assertTrue(DrUtil.valEquals(new BigDecimal(8), 8));
//		assertTrue(DrUtil.valEquals(new BigDecimal(8), 8L));
//		assertTrue(DrUtil.valEquals(8, new BigDecimal(8)));
//		assertTrue(DrUtil.valEquals(8, 8L));
//		
//		// Floats
//		assertTrue(DrUtil.valEquals(8.2, 8.2f));
//		
//		// Strings
//		assertTrue(DrUtil.valEquals("Test", "Test"));
	}
}
