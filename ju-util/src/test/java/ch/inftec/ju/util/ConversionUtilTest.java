package ch.inftec.ju.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for the ConversionUtil class.
 * @author tgdmemae
 *
 */
public class ConversionUtilTest {
	@Test
	public void toBigDecimal() {
		Assert.assertEquals(new BigDecimal(1), ConversionUtils.toBigDecimal(1));
		Assert.assertEquals(new BigDecimal(1.1), ConversionUtils.toBigDecimal(1.1));
		Assert.assertEquals(new BigDecimal(1.1f), ConversionUtils.toBigDecimal(1.1f));
		Assert.assertNull(ConversionUtils.toBigDecimal("test"));
		Assert.assertNull(ConversionUtils.toBigDecimal(null));
	}
	
	@Test
	public void canConvert_toLong() {
		Assert.assertEquals(new Long(1), ConversionUtils.toLong(1));
		Assert.assertEquals(new Long(1), ConversionUtils.toLong(1.1));
		Assert.assertEquals(new Long(1), ConversionUtils.toLong(1.1f));
		Assert.assertEquals(new Long(1), ConversionUtils.toLong(new BigInteger("1")));
		Assert.assertNull(ConversionUtils.toLong("test"));
		Assert.assertNull(ConversionUtils.toLong(null));
	}
}
