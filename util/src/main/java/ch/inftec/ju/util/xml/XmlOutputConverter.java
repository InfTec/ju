package ch.inftec.ju.util.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;

import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuException;

/**
 * Helper class to load data from an OutputStream into an XML document.
 * <p>
 * Submit the OutputStream instance from getOutputStream to any method writing XML
 * data and use the getDocument afterwards to load the data to a Document.
 * @author Martin
 *
 */
public class XmlOutputConverter {
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	
	/**
	 * Gets the OutputStream that can be passed to any function writing XML data
	 * to an output stream.
	 * @return OutputStream implementation
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	/**
	 * Gets the DOM document that was written to the OutputStream.
	 * @return Document
	 * @throws JuException If the document cannot be loaded from the stream
	 */
	public Document getDocument() throws JuException {
		IOUtil.close(outputStream);
		byte[] bytes = outputStream.toByteArray();
		
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		return XmlUtils.loadXml(inputStream, null);		
	}
}
