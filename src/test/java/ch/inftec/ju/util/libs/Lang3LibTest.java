package ch.inftec.ju.util.libs;

import java.util.HashMap;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;

import ch.inftec.ju.util.TestUtils;

/**
 * Tests for the commons-lang3 library.
 * @author Martin
 *
 */
public class Lang3LibTest {
	@Test
	public void toStringBuilder() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("one", 1);
		map.put("two", 2);
		
		// Test the builder with a hashmap
		
		// e.g. ch.inftec.ju.util.libs.Lang3LibTest@f27cdc[{two=2, one=1}]
		TestUtils.assertRegexEquals("ch\\.inftec\\.ju\\.util\\.libs\\.Lang3LibTest@.*\\[\\{two=2, one=1\\}\\]",
				new ToStringBuilder(this).append(map).toString());		
		
		Assert.assertEquals("Lang3LibTest[map={two=2, one=1}]", 
				new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("map", map).toString());
		
		Assert.assertEquals("{two=2, one=1}", 
				new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("map", map).toString());
	}
	
	@Test
	public void equalsBuilder() {
		HashMap<String, Integer> map1 = new HashMap<String, Integer>();
		HashMap<String, Integer> map2 = new HashMap<String, Integer>();
		
		map1.put("one", 1);
		map2.put("one", 1);
		
		Assert.assertTrue(new EqualsBuilder().append(map1, map2).isEquals());
	}
	
	@Test
	public void stringUtils() {
		String s1 = "hello, world,test ,bla";
		
		TestUtils.assertArrayEquals(new String[] {"hello", " world", "test ", "bla"}, StringUtils.split(s1, ','));
		
		TestUtils.assertArrayEquals(new String[] {"hello", "world", "test", "bla"}, StringUtils.stripAll(StringUtils.split(s1, ',')));
	}
}
