package ch.inftec.ju.util;

import java.awt.Dimension;


/**
 * Helper class containing math and geometry related utility classes.
 * @author tgdmemae
 *
 */
public final class MathUtils {
	/**
	 * Don't instantiate.
	 */
	private MathUtils() {
		throw new AssertionError("use only statically");
	}
	
	/**
	 * Gets a new dimension using the smaller width and smaller height value from
	 * both dimensions.
	 * @param d1 Dimension 1
	 * @param d2 Dimension 2
	 * @return New dimension with both the smaller width and height (which may come
	 * from different dimensions).
	 */
	public static Dimension min(Dimension d1, Dimension d2) {
		return new Dimension(Math.min(d1.width, d2.width), Math.min(d1.height, d2.height));
	}
	
	/**
	 * Gets a new dimension using the greater width and smaller height value from
	 * both dimensions.
	 * @param d1 Dimension 1
	 * @param d2 Dimension 2
	 * @return New dimension with both the greater width and height (which may come
	 * from different dimensions).
	 */
	public static Dimension max(Dimension d1, Dimension d2) {
		return new Dimension(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
	}
}
