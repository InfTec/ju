package ch.inftec.ju.util.fx;

import javafx.geometry.Rectangle2D;

import org.junit.Assert;
import org.junit.Test;

import com.sun.javafx.geom.Point2D;

public class GeoFxTest {
	@Test
	public void centerRectangle2D() {
		Rectangle2D r1 = new Rectangle2D(0, 0, 5, 10);
		Rectangle2D r2 = GeoFx.center(r1, new Point2D(10, 10));
		
		Assert.assertEquals(new Rectangle2D(7.5, 5, 5, 10), r2);
	}
	
	@Test
	public void centerPoint2D() {
		Rectangle2D r1 = new Rectangle2D(0, 0, 5, 10);
		
		Assert.assertEquals(new Point2D(2.5f, 5f), GeoFx.center(r1));
	}
	
	@Test
	public void moveToBounds() {
		Rectangle2D rect = new Rectangle2D(0, 0, 1, 1);
		
		// Move right, up
		Rectangle2D r1 = new Rectangle2D(1, 1, 7, 9);
		Assert.assertEquals(new Rectangle2D(1, 1, 1, 1), GeoFx.moveToBounds(rect, r1));
		
		// Move left, down
		Rectangle2D r2 = new Rectangle2D(-2, -2, 2, 2);
		Assert.assertEquals(new Rectangle2D(-1, -1, 1, 1), GeoFx.moveToBounds(rect, r2));

		// Center
		Rectangle2D r3 = new Rectangle2D(0, 0, 0.5, 0.5);
		Assert.assertEquals(new Rectangle2D(-0.25, -0.25, 1, 1), GeoFx.moveToBounds(rect, r3));
		
		// No move
		Rectangle2D r4 = new Rectangle2D(-1, -1, 2, 2);
		Assert.assertEquals(rect, GeoFx.moveToBounds(rect, r4));
	}
}
