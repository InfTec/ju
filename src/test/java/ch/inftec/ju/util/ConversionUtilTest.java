package ch.inftec.ju.util;

import java.math.BigDecimal;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test class for the ConversionUtil class.
 * @author tgdmemae
 *
 */
public class ConversionUtilTest {
	@Test
	public void toBigDecimal() {
		Assert.assertEquals(ConversionUtils.toBigDecimal(1), new BigDecimal(1));
		Assert.assertEquals(ConversionUtils.toBigDecimal(1.1), new BigDecimal(1.1));
		Assert.assertEquals(ConversionUtils.toBigDecimal(1.1f), new BigDecimal(1.1f));
	}
}
