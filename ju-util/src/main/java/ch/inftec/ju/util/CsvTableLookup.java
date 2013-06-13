package ch.inftec.ju.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Helper class to lookup values in a CSV that contains one header row and one
 * key column at the start, e.g:
 * <p>
 * <code>
 *   ;H1;H2<br/>
 *   A;A1;A2<br/>
 *   B;B1;B2<br/>
 * </code>
 * <p>
 * In this example, getValue("A", "H2") would return "A1".
 * <p>
 * The value of cell 1,1 is not relevant.
 * <p>
 * Use the build() method to get a builder to create new CsvTableLookup instances.
 * <p>
 * The lookup uses ';' as a separator character.
 * @author Martin
 *
 */
public class CsvTableLookup {
	public static class CsvTableLookupBuilder {
		private URL url;
		private String defaultColumn;
		
		private CsvTableLookupBuilder() {
		}
		
		/**
		 * Sets the CSV resource URL. 
		 * @param url URL to the csv resource
		 * @return This builder to allow for chaining
		 */
		public CsvTableLookupBuilder from(URL url) {
			this.url = url;
			return this;
		}
		
		/**
		 * Sets a default column, i.e. the column that will be used
		 * to lookup a value if the actual column value is empty.
		 * @param defaultColumn Default column name
		 * @return This builder to allow for chaining
		 */
		public CsvTableLookupBuilder defaultColumn(String defaultColumn) {
			this.defaultColumn = defaultColumn;
			return this;
		}
		
		public CsvTableLookup create() {
			AssertUtil.assertNotNull("URL must be specified", this.url);
			return new CsvTableLookup(this.url, this.defaultColumn);
		}
	}
	
	/**
	 * Build a new CsvTableLookup instance.
	 * @return Builder
	 */
	public static CsvTableLookupBuilder build() {
		return new CsvTableLookupBuilder();
	}
	
	private final String defaultColumn;
	
	/**
	 * Contains the name of the headers with the index corresponding to
	 * the value in the rows value array.
	 */
	private Map<String, Integer> headerIndexes = new HashMap<>();
	/**
	 * Unmodifiable list containing all headers for reference.
	 */
	private List<String> headers;
	
	/**
	 * Contains the rows with the values. Note that the values array contains
	 * the name at position 0.
	 */
	private Map<String, String[]> rowValues = new HashMap<>();
	/**
	 * Unmodifiable list containing all keys for reference.
	 */
	private List<String> keys;
	
	private CsvTableLookup(URL url, String defaultColumn) {
		this.defaultColumn = defaultColumn;
		this.read(url);
	}
	
	private void read(URL url) {
		try (CSVReader reader = new CSVReader(new IOUtil().createReader(url), ';')) {
			List<String[]> rows = reader.readAll();
			
			if (rows.size() < 2 || rows.get(0).length < 2) {
				throw new IllegalArgumentException("File needs at least a header and one row");
			}
			
			// Read headers
			List<String> headers = new ArrayList<>();
			for (int i = 1; i < rows.get(0).length; i++) {
				String header = rows.get(0)[i];
				if (this.headerIndexes.containsKey(header)) {
					throw new IllegalArgumentException("Duplicate header: " + header);
				}
				this.headerIndexes.put(header, i);
				headers.add(header);
			}
			this.headers = Collections.unmodifiableList(headers);
			
			// Read columns
			List<String> keys = new ArrayList<>();
			for (int i = 1; i < rows.size(); i++) {
				if (rows.get(i).length < 1) {
					throw new IllegalArgumentException("Unspecified row name at position " + i);
				}
				String key = rows.get(i)[0];
				if (this.rowValues.containsKey(key)) {
					throw new IllegalArgumentException("Duplicate key: " + key);
				}
				
				this.rowValues.put(key, rows.get(i));
				keys.add(key);
			}
			this.keys = Collections.unmodifiableList(keys);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't read CSV file", ex);
		}
	}

	/**
	 * Gets the value of the cell identified by the row name and the
	 * header.
	 * @param key Row name, i.e. the value of the first column
	 * @param header Header name, i.e. the value in the first row of the column
	 * @return Value of the identified cell. If the cell is empty or undefined and a default
	 * column was specified creating the lookup, the value of this cell will be returned.
	 * If the cell does not exist, null is returned. If the cell is empty, an empty String is returned.
	 */
	public String get(String key, String header) {
		if (header == null) return null;
		
		String[] row = this.rowValues.get(key);
		if (row == null) {
			return null;
		} else {
			// Determine the index, taking the defaultColumn into account if necessary
			Integer index = this.headerIndexes.get(header);
			String headerVal = (index != null && index < row.length)
					? (row[index] == null ? "" : row[index])
					: null;
			
			if (StringUtils.isEmpty(headerVal) && !header.equals(this.defaultColumn)) {
				// Try to get the default column value
				String defaultColumnVal = this.get(key, this.defaultColumn);
				if (!StringUtils.isEmpty(defaultColumnVal)) return defaultColumnVal;
			}
			
			return headerVal;
		}
	}
	
	/**
	 * Gets the value of the specified cell as a Boolean. Same rules apply as to get().
	 * @param key
	 * @param header
	 * @return True if the value equals (ignoring case) "true" 
	 */
	public Boolean getBoolean(String key, String header) {
		String val = this.get(key, header);
		return val == null ? null : Boolean.parseBoolean(val);
	}
	
	/**
	 * Gets the value of the specified cell as an Integer. Same rules apply as to get().
	 * @param key
	 * @param header
	 * @return Integer value of the cell 
	 */
	public Integer getInteger(String key, String header) {
		String val = this.get(key, header);
		return val == null ? null : Integer.parseInt(val);
	}
	
	/**
	 * Gets the value of the specified cell as a Long. Same rules apply as to get().
	 * @param key
	 * @param header
	 * @return Long value of the cell 
	 */
	public Long getLong(String key, String header) {
		String val = this.get(key, header);
		return val == null ? null : Long.parseLong(val);
	}
	
	/**
	 * Gets a list of headers, in the order they are specified in the file.
	 * @return List of headers
	 */
	public List<String> getHeaders() {
		return this.headers;
	}
	
	/**
	 * Gets a list of keys, in the order they are specified in the file.
	 */
	public List<String> getKeys() {
		return this.keys;
	}
}
