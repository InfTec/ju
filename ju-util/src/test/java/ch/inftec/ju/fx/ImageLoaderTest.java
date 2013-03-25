package ch.inftec.ju.fx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.ThreadUtils;

public class ImageLoaderTest {
	private final Logger logger = LoggerFactory.getLogger(ImageLoaderTest.class);
	
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
	public void loadImage_background() {
		ImageLoader imageLoader = new ImageLoader("ch/inftec/ju/fx/testImages");
		final Image image = imageLoader.loadImage("1x1_black.png", true);
		
		// Hopefully, loading is slow enough to not be complete before these assertions run
		Assert.assertTrue(image.isBackgroundLoading());
		
		Assert.assertEquals(0.0, image.getWidth());
		Assert.assertEquals(0.0, image.getHeight());

		image.progressProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				logger.info("Loading at: " + newValue);
				if (newValue.doubleValue() == 1.0) {
					logger.info("Loading complete");
					Assert.assertEquals(1.0, image.getWidth());
					Assert.assertEquals(1.0, image.getHeight());
				}
			}
		});
		
		// Wait for image to be loaded
		do {
			ThreadUtils.sleep(10);
		} while (image.progressProperty().get() < 1.0);
		
		Assert.assertEquals(1.0, image.progressProperty().get());
		Assert.assertTrue(image.isBackgroundLoading()); // Stays true...
		Assert.assertFalse(image.isError());
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
