package ch.inftec.ju.fx;

import javafx.scene.image.Image;
import junit.framework.Assert;

import org.junit.Test;

public class ImageLoaderTest {
	@Test
	public void loadImage() {
		ImageLoader imageLoader = new ImageLoader("ch/inftec/ju/fx/testImages");
		
		Image image = imageLoader.loadImage("1x1_black.png");
		
		Assert.assertEquals(1.0, image.getWidth());
		Assert.assertEquals(1.0, image.getHeight());

		// Load again, should yield the same instance
		Image image2 = imageLoader.loadImage("1x1_black.png");
		Assert.assertSame(image, image2);
	}
	
	@Test
	public void loadImage_unknown() {
		ImageLoader imageLoader = new ImageLoader();
		
		try {
			imageLoader.loadImage("unknown");
			Assert.fail("Expected exception");
		} catch (IllegalArgumentException ex) {
			// Expected
		}
	}
}
