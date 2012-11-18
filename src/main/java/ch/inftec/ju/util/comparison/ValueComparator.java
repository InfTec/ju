package ch.inftec.ju.util.comparison;

import java.math.BigDecimal;
import java.util.Comparator;

import ch.inftec.ju.util.ConversionUtils;

/**
 * Implements the EqualityTester and the Comparator interface.
 * 
 * This comparator tries to compare values of instances, using type conversion where possible.
 * For instance, it will try to convert numbers to BigDecimal instances prior
 * to compare them.
 * @author tgdmemae
 *
 * @param <T> Base type
 */
public class ValueComparator<T extends Object> implements EqualityTester<T>, Comparator<T> {
	/**
	 * Default instance for an object comparator.
	 */
	public static ValueComparator<Object> INSTANCE = new ValueComparator<Object>();
	
	@Override
	public int compare(T o1, T o2) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	@Override
	public boolean equals(T o1, T o2) {
		// Handle null cases:
		if (o1 == null || o2 == null) {
			return o1 == null && o2 == null;
		} else if (o1.equals(o2)) {
			// Equal, so return true
			return true;
		} else {			
			// Not equal, but might be after conversion...
			BigDecimal bd1 = ConversionUtils.toBigDecimal(o1);
			if (bd1 != null) {
				BigDecimal bd2 = ConversionUtils.toBigDecimal(o2);
				if (bd2 != null) {
					if (bd1.equals(bd2)) return true;
					else if (o1 instanceof Float || o2 instanceof Float) {
						// If we compare floats do doubles, decimal fractions might be different due
						// to precision and conversion
						return bd1.floatValue() == bd2.floatValue();
					}
				}
			}
			
			// Not equal after conversion
			return false;
		}
	}
}
