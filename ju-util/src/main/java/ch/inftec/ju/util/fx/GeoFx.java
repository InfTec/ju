package ch.inftec.ju.util.fx;

import javafx.geometry.Rectangle2D;

import com.sun.javafx.geom.Point2D;

/**
 * Helper class to perform geometric operations on Java FX objects like
 * rectangles and the like.
 * @author Martin
 *
 */
public class GeoFx {
	/**
	 * Centers the specified Rectangle over the specified point.
	 * @param rect
	 * @param point
	 * @return
	 */
	public static Rectangle2D center(Rectangle2D rect, Point2D point) {
		Rectangle2D r = new Rectangle2D(
				point.x - rect.getWidth() / 2,
				point.y - rect.getHeight() / 2,
				rect.getWidth(),
				rect.getHeight());
		
		return r;				
	}
	
	/**
	 * Gets the center of the specified rectangle.
	 * @param rect
	 * @return
	 */
	public static Point2D center(Rectangle2D rect) {
		Point2D c = new Point2D(
				(float) (rect.getMinX() + rect.getWidth() / 2),
				(float) (rect.getMinY() + rect.getHeight() / 2));
		
		return c;
	}
	
	/**
	 * Centers rect1 over rect2.
	 * @param rect1
	 * @param rect2
	 * @return
	 */
	public static Rectangle2D center(Rectangle2D rect1, Rectangle2D rect2) {
		Point2D c2 = GeoFx.center(rect2);
		return GeoFx.center(rect1, c2);
	}
	
	/**
	 * Moves rect1 into the bounds of rect2 (with the shortest distance possible).
	 * <p>
	 * If rect1 is larger than rect2, it is moved as little as possible to contain
	 * most area in rect2.
	 * <p>
	 * If two edges should overlap rect2, the rectangle will be centered.
	 * @param rect1
	 * @param rect2
	 * @return
	 */
	public static Rectangle2D moveToBounds(Rectangle2D rect1, Rectangle2D rect2) {
		double newX = GeoFx.moveToBounds(rect1.getMinX(), rect1.getMaxX(), rect2.getMinX(), rect2.getMaxX());
		double newY = GeoFx.moveToBounds(rect1.getMinY(), rect1.getMaxY(), rect2.getMinY(), rect2.getMaxY());
		
		return new Rectangle2D(newX, newY, rect1.getWidth(), rect1.getHeight());
	}
	
	private static double moveToBounds(double min1, double max1, double min2, double max2) {
		double deltaMin = min2 - min1;
		double xDeltaMax = max2 - max1;
		
		double newX;

		if (Math.signum(deltaMin) <= 0 && Math.signum(xDeltaMax) >= 0) {
			newX = min1; // No movement necessary 
		} else if (Math.signum(deltaMin) == Math.signum(xDeltaMax)) {
			// Same orientation, so take the smaller one
			if (Math.abs(deltaMin) < Math.abs(xDeltaMax)) {
				newX = min1 + deltaMin;
			} else {
				newX = min1 + xDeltaMax;
			}
		} else {
			// Out of bounds different directions -> center
			newX = min2 + (max2-min2)/2 - (max1-min1)/2;
		}
		
		return newX;
	}
}
