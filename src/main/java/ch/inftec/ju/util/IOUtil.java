package ch.inftec.ju.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.io.NewLineReader;

/**
 * Utility class containing I/O related helper methods. Methods that depend on a charset
 * or on a text Reader are not static. In this case, a new IOUtil instance must be created using either the
 * default charset or an explicit charset (as specified in the constructor).
 * <p>
 * The IOUtil class converts line endings to a single LF character ('\n'), regardless of the actual
 * line feed policy used in the source file.
 * @author tgdmemae
 *
 */
public final class IOUtil {
	static final Logger log = LoggerFactory.getLogger(IOUtil.class);
	
	/**
	 * The default charset that will be used to initialize an IOUtil instance
	 * if no explizit charset is specified.
	 */
	private static String defaultCharset = null;
	
	private static int tempFileCounter = 0;
	
	/**
	 * The Charset used by the IOUtil instance. If not submitted with the constructor, the
	 * defaultCharset will be used.
	 */
	private final String charset;
	
	/**
	 * Gets the default charset used by the IOUtil classes if no explicit
	 * charset is submited with the call. If not changed by the used,
	 * this is the system's default charset as returned by
	 * Charset.defaultCharset()
	 * @return Default charset
	 */
	public synchronized static String getDefaultCharset() {
		if (IOUtil.defaultCharset == null) {
			IOUtil.defaultCharset = Charset.defaultCharset().displayName();
		}
		return IOUtil.defaultCharset;
	}
	
	/**
	 * Sets the default charset used by the IOUtil classes if no explicit
	 * charset is submitted with the call.
	 * <p>
	 * Note that this will only change the default charset for the IOUtil class
	 * and not for the whole java runtime.
	 * @param charset Default charset
	 */
	public synchronized static void setDefaultCharset(String charset) {
		IOUtil.defaultCharset = charset;
	}
	
	/**
	 * Gets the charset that this IOUtil instance uses.
	 * @return Charset
	 */
	public String getCharset() {
		return this.charset;
	}
	
	/**
	 * Creates a new IOUtil instance using the default charset.
	 */
	public IOUtil() {
		this(IOUtil.getDefaultCharset());
	}
	
	/**
	 * Creates a new IOUtil instance with the specified charset.
	 * @param charset
	 */
	public IOUtil(String charset) {
		this.charset = charset;
	}
	
	/**
	 * Closes the specified Reader and consumes any exception that
	 * might be raised.
	 * @param reader Reader instance
	 */
	public static void close(Reader reader) {
		try {
			log.debug("Closing Reader: " + ObjectUtils.identityToString(reader));
			if (reader != null) reader.close();
		} catch (IOException ex) {
			log.warn("Could not close Reader instance: " + ex.getMessage());
		}
	}
	
	/**
	 * Closes the specified InputStream and consumes any exception that
	 * might be raised.
	 * @param stream InputStream instance
	 */
	public static void close(InputStream stream) {
		try {
			log.debug("Closing InputStream: " + ObjectUtils.identityToString(stream));
			if (stream != null) stream.close();
		} catch (IOException ex) {
			log.warn("Could not close InputStream instance: " + ex.getMessage());
		}
	}
	
	/**
	 * Closes the specified OutputStream and consumes any exception that
	 * might be raised.
	 * @param stream OutputStream instance
	 */
	public static void close(OutputStream stream) {
		try {
			log.debug("Closing OutputStream: " + ObjectUtils.identityToString(stream));
			if (stream != null) stream.close();
		} catch (IOException ex) {
			log.warn("Could not close OutputStream instance: " + ex.getMessage());
		}
	}
	
	/**
	 * Generates a String for the specified reader.
	 * @param reader Reader instance
	 * @throws JuRuntimeException If the conversion fails
	 */
	public static String toString(Reader reader) {
		try {
			return IOUtils.toString(reader);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't generate String for Reader", ex);
		}
	}

	/**
	 * Converts the specified String to a String containing Unix style new lines, i.e. \n
	 * @param s String to be converted
	 * @return String containing \n for line breaks
	 */
	public static String toNewLineUnix(String s) {
		NewLineReader reader = new NewLineReader(new StringReader(s), null, IOUtils.LINE_SEPARATOR_UNIX);
		return IOUtil.toString(reader);
	}
	
	/**
	 * Gets an URL to the specified resource whose path is relative to the calling class.
	 * @param resourceName Resource path
	 * @return URL instance to the resource or null if no resource can be found
	 */
	public static URL getResourceURL(String resourcePath) {
		if (resourcePath == null) return null;
		
		return IOUtil.getResourceURL(resourcePath, ReflectUtils.getCallingClass());
	}
	
	/**
	 * Gets an URL to the specified resource.
	 * @param resourceName Resource path
	 * @param relativeClass If not null, the path is used relative to this classes package. If null,
	 * the path is used relative to the calling's class' package.
	 * @return URL instance to the resource or null if no resource can be found
	 */
	public static URL getResourceURL(String resourcePath, Class<?> relativeClass) {
		if (resourcePath == null) return null;
		
		if (relativeClass == null) relativeClass = ReflectUtils.getCallingClass();
		
		URL url = relativeClass.getResource(resourcePath);
		
		return url;
	}
	
	/**
	 * Creates a new temporary file in the default temporary directory.
	 * <p>
	 * The file will be new/empty and deleted automatically when the JVM is exited.
	 * @return New temporary file
	 */
	public static synchronized File getTemporaryFile() throws JuException {
		try {
			File tempFile = File.createTempFile(String.format("%s_%s_%s", 
					IOUtil.class.getName(),
					IOUtil.tempFileCounter++,
					System.currentTimeMillis()),
					"tmp");
			
			tempFile.deleteOnExit();
			
			return tempFile;
		} catch (Exception ex) {
			throw new JuException("Couldn't create temporary file", ex);
		}
	}
	
	/**
	 * Loads the specified text resource into a string. This method uses the charset of the
	 * IOUtil instance.
	 * @param resourcePath Resource path, either absolute or relative to the calling class
	 * @return Loaded resource as a String
	 * @throws JuException If the resource cannot be loaded
	 */
	public String loadTextResource(String resourcePath) throws JuException {
		return this.loadTextResource(resourcePath, ReflectUtils.getCallingClass());
	}
	
	/**
	 * Loads the text from the specified file into a string. This method uses the charset of the
	 * IOUtil instance.
	 * @param File File to load from
	 * @param replacements Optional 'key, value' strings to replace %key% tags in the resource with the specified value
	 * @return Loaded resource as string
	 * @throws JuException If the resource cannot be loaded
	 */
	public String loadTextFromFile(File file, String... replacements) throws JuException {
		if (!file.exists()) throw new JuException("File doesn't exist: " + file);
		if (!file.isFile()) throw new JuException("Not a file: " + file);
		
		URL url = null;
		try {
			url = file.toURI().toURL();
		} catch (Exception ex) {
			throw new JuException("Couldn't convert file to URL: " + file, ex);
		}
		
		return loadTextFromUrl(url, replacements);
	}
	
	/**
	 * Loads the text from the specified file into a string. This method uses the charset of the
	 * IOUtil instance.
	 * @param filePath File path
	 * @param replacements Optional 'key, value' strings to replace %key% tags in the resource with the specified value
	 * @return Loaded resource as string
	 * @throws JuException If the resource cannot be loaded
	 */
	public String loadTextFromFile(String filePath, String... replacements) throws JuException {
		File file = new File(filePath);
		if (!file.exists()) throw new JuException("File doesn't exist: " + file);
		if (!file.isFile()) throw new JuException("Not a file: " + file);
		
		URL url = null;
		try {
			url = file.toURI().toURL();
		} catch (Exception ex) {
			throw new JuException("Couldn't convert file to URL: " + filePath, ex);
		}
		
		return loadTextFromUrl(url, replacements);
	}
	
	/**
	 * Loads the specified text resource into a string. This method uses the charset of the
	 * IOUtil instance.
	 * @param resourceName Resource path
	 * @param relativeClass If not null, the path is used relative to this classes package. If null,
	 * the path is used relative to the calling's class' package.
	 * @param replacements Optional 'key, value' strings to replace %key% tags in the resource with the specified value
	 * @return Loaded resource as string
	 * @throws JuException If the resource cannot be loaded
	 */
	public String loadTextResource(String resourcePath, Class<?> relativeClass, String... replacements) throws JuException {
		URL url = IOUtil.getResourceURL(resourcePath, relativeClass == null ? ReflectUtils.getCallingClass() : relativeClass);
		return this.loadTextFromUrl(url, replacements);
	}
	
	/**
	 * Loads the specified URL resource into a string. This method uses the charset of the
	 * IOUtil instance.
	 * @param url URL to resource
	 * @param replacements Optional 'key, value' strings to replace %key% tags in the resource with the specified value
	 * @return Loaded resource as string
	 * @throws JuException If the resource cannot be loaded
	 */
	public String loadTextFromUrl(URL url, String... replacements) throws JuException {
		try {
			if (url == null) {
				throw new JuException("Resource not found: " + url);
			}
			
			try (BufferedReader reader = new BufferedReader(
					new NewLineReader(
							new InputStreamReader(url.openStream(), this.charset)
							, null, NewLineReader.LF))) {
			
				StringBuilder sb = new StringBuilder();
				char[] buff = new char[1024];
				int read;
				while ((read = reader.read(buff)) > 0) {
					sb.append(buff, 0, read);
				}
				
				return JuStringUtils.replaceAll(sb.toString(), replacements);
			}
		} catch (Exception ex) {
			throw new JuException("Couldn't load text from URL", ex);
		}
	}

	/**
	 * Loads Properties from the specified resource.
	 * @param resourcePath Resource path, either absolute or relative to the calling class
	 * return Properties instance
	 * @throws JuException If the resource cannot be loaded
	 */
	public Properties loadPropertiesFromResource(String resourcePath) throws JuException {
		return this.loadPropertiesFromResource(resourcePath, ReflectUtils.getCallingClass());
	}
	
	/**
	 * Loads Properties from the specified resource.	
	 * @param resourcePath Resource path
	 * @param relativeClass If not null, the path is used relative to this classes package. If null,
	 * the path is used relative to the calling's class' package.
	 * @return Properties instance
	 * @throws JuException If the properties cannot be loaded
	 */
	public Properties loadPropertiesFromResource(String resourcePath, Class<?> relativeClass) throws JuException {
		URL url = IOUtil.getResourceURL(resourcePath, relativeClass == null ? ReflectUtils.getCallingClass() : relativeClass);
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), this.charset))) {
			Properties props = new Properties();
			props.load(reader);
			
			return props;
		} catch (Exception ex) {
			throw new JuException("Couldn't load properties from resource: " + resourcePath);
		}		
	}
	
	/**
	 * Writes the specified text to a file.
	 * @param text Text
	 * @param file File to write to
	 * @param overwrite If true, an existing file will be overwritten. If false, it will be preserved.
	 * @throws JuException If the file cannot be written
	 */
	public void writeTextToFile(String text, File file, boolean overwrite) throws JuException {
		try {
			if (file.exists()) {
				if (file.isDirectory()) throw new JuException("Directory with file name exists: " + file);
				else if (!overwrite) throw new JuException("File exists: " + file);
			}		
			
			FileUtils.writeStringToFile(file, text, charset);
		} catch (JuException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new JuException("Couldn't write text to file: " + file, ex);
		}
	}
	
	/**
	 * Writes the specified text to a file.
	 * @param text Text
	 * @param filePath File path
	 * @param overwrite If true, an existing file will be overwritten. If false, it will be preserved.
	 * @throws JuException If the file cannot be written
	 */
	public void writeTextToFile(String text, String filePath, boolean overwrite) throws JuException {
		this.writeTextToFile(text, new File(filePath), overwrite);
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(this, "charset", this.charset);
	}
}
