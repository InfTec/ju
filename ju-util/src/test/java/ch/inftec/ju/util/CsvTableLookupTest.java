package ch.inftec.ju.util;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.util.CsvTableLookup.CsvTableLookupBuilder;

public class CsvTableLookupTest {
	@Test
	public void csvTableLookup_canReadCsvFile() {
		CsvTableLookup l = this.tableLookupBuilder().create();
		
		Assert.assertEquals("V11", l.get("R1", "H1"));
	}
	
	@Test
	public void csvTableLookup_forEmptyCell_returnsEmpty() {
		CsvTableLookup l = this.tableLookupBuilder().create();
		Assert.assertEquals("", l.get("R1", "H3"));
		Assert.assertEquals("", l.get("R1", "H4"));
	}
	
	@Test
	public void csvTableLookup_forInvalidCell_returnsNull() {
		CsvTableLookup l = this.tableLookupBuilder().create();
		Assert.assertNull(l.get("R1", "H5"));
	}
	
	@Test
	public void get_ifDefaultColumnSet_returnsDefaultColumnValue() {
		CsvTableLookup l = this.tableLookupBuilder().defaultColumn("H1").create();
		Assert.assertEquals("V11", l.get("R1", "H4"));
	}
	
	@Test
	public void get_ifDefaultColumnSetAndInvalidColumn_returnsDefaultValue() {
		CsvTableLookup l = this.tableLookupBuilder().defaultColumn("H1").create();
		Assert.assertEquals("V11", l.get("R1", "H4"));
	}
	
	@Test
	public void csvTableLookup_returnsAllHeaders() {
		CsvTableLookup l = this.tableLookupBuilder().create();
		Assert.assertTrue(JuCollectionUtils.collectionEquals(
				Arrays.asList("H1", "H2", "H3", "H4"),
				l.getHeaders()));
	}
	
	@Test
	public void csvTableLookup_returnsAllKeys() {
		CsvTableLookup l = this.tableLookupBuilder().create();
		Assert.assertTrue(JuCollectionUtils.collectionEquals(
				Arrays.asList("R1", "R2", "R3", "R4"),
				l.getKeys()));
	}
	
	@Test
	public void csvTableLookup_returnsBoolean() {
		CsvTableLookup l = this.tableLookupBuilder().create();
		Assert.assertEquals(Boolean.TRUE, l.getBoolean("R4", "H1"));
	}
	
	@Test
	public void csvTableLookup_returnsInteger() {
		CsvTableLookup l = this.tableLookupBuilder().create();
		Assert.assertEquals(new Integer(7), l.getInteger("R4", "H2"));
	}
	
	@Test
	public void csvTableLookup_returnsLong() {
		CsvTableLookup l = this.tableLookupBuilder().create();
		Assert.assertEquals(new Long(7L), l.getLong("R4", "H2"));
	}
	
	@Test
	public void csvTableLookup_forNullKey_returnsNull() {
		CsvTableLookup l = this.tableLookupBuilder().create();
		Assert.assertNull(l.get(null, "H1"));
	}
	
	private CsvTableLookupBuilder tableLookupBuilder() {
		return CsvTableLookup.build()
				.from(IOUtil.getResourceURL("data.csv"));
	}
}
