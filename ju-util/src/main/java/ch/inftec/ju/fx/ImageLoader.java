package ch.inftec.ju.fx;

import java.util.HashMap;

import javafx.scene.image.Image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.XString;


/**
 * Helper class to load and cach Images / Icons.
 * <p>
 * The loader caches all images, i.e. once loaded, they will remain in memory until the
 * application is stopped.
 * <p>
 * The image format is FX images. The loader provides some Swing functions for backward (Swing)
 * compatibility, though.
 * <p>
 * To load JU resources, there is a default ImageLoader available. Any instance of ImageLoader
 * can load images from the default loader by using the prefix 'def:' in the (relative!)
 * image path. e.g. 'def:information.png'
 * @author Martin
 *
 */
public class ImageLoader {
	private final Logger logger = LoggerFactory.getLogger(ImageLoader.class);
	
	private final String pathPrefix;
	
	private HashMap<String, Image> images = new HashMap<>();
	
	private static final String DEFAULT_IMAGE_PREFIX = "def:"; 
	
	private static final ImageLoader defaultImageLoader = new ImageLoader("ch/inftec/ju/fx/images");
	
	/**
	 * Gets the default ImageLoader that is used to load Images for internal
	 * JU resources. Any implementation of ImageLoader will also try
	 * to load images from this loader first.
	 * @return Default ImageLoader instance
	 */
	public static ImageLoader getDefaultLoader() {
		return ImageLoader.defaultImageLoader;
	}
	
	/**
	 * Creates an new ImageLoader with an empty path prefix, i.e. all
	 * image URLs must be absolute.
	 */
	public ImageLoader() {
		this("");
	}
	
	/**
	 * Creates a new ImageLoader with the specified path prefix.
	 * <p>
	 * The prefix will be added before every image URL to load them.
	 * @param pathPrefix Path prefix for image URLs
	 */
	public ImageLoader(String pathPrefix) {
		XString prefix = new XString(pathPrefix);
		prefix.assertEmptyOrText("/");
		
		this.pathPrefix = prefix.toString();
	}
	
	private String getFullImagePath(String path) {
		return this.pathPrefix + path;
	}
	
	/**
	 * Loads the image at the specified (relative) path.
	 * @param path Path of the image, relative to the loader's pathPrefix.
	 * @return FX Image instance
	 * @throws JuRuntimeException if the image cannot be loaded
	 */
	public Image loadImage(String path) {
		// Check if the path references a default resource
		if (path.startsWith(ImageLoader.DEFAULT_IMAGE_PREFIX)) {
			String defaultPath = path.substring(ImageLoader.DEFAULT_IMAGE_PREFIX.length());
			return ImageLoader.getDefaultLoader().loadImage(defaultPath);
		} else {
			// Regular image loading
			
			String fullPath = this.getFullImagePath(path);
			if (!this.images.containsKey(fullPath)) {
				try {
					logger.debug("Loading image: " + fullPath);
					Image image = new Image(fullPath);
					this.images.put(fullPath, image);
				} catch (Exception ex) {
					logger.error("Couldn't load image: " + fullPath);
					throw new JuRuntimeException("Couldn't load image: " + fullPath, ex);
				}
			}
			
			return this.images.get(fullPath);
		}
	}
}
