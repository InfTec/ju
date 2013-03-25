package ch.inftec.ju.fx;

import javafx.scene.image.Image;
import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.util.JuRuntimeException;

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
		} catch (JuRuntimeException ex) {
			Assert.assertTrue(ex.getMessage().contains("unknown"));
		}
	}
	
	@Test
	public void loadImage_def() {
		ImageLoader imageLoader = new ImageLoader("just/some/prefix");
		
		Image image = imageLoader.loadImage("def:information.png");
		
		Assert.assertEquals(16.0, image.getWidth());
		Assert.assertEquals(16.0, image.getHeight());
		
		// Use the default loader directly
		Assert.assertSame(image, ImageLoader.getDefaultLoader().loadImage("def:information.png"));
		Assert.assertSame(image, ImageLoader.getDefaultLoader().loadImage("information.png"));
	}
}
