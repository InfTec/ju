package ch.inftec.ju.util.libs;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

/**
 * Tests for the java.nio package
 * @author Martin
 *
 */
public class NioTest {
	//@Test Paths may change on different machines
	public void nio_file() throws IOException {
		// Creating a new Path
		Path workingPath = Paths.get(".");
		Assert.assertEquals(".", workingPath.toString()); // Note: Path is relative!
		
		// Absolute path
		Assert.assertEquals("C:\\prog\\java\\workspaces\\test\\ch.inftec.ju\\ju-util\\.", workingPath.toAbsolutePath().toString());
		
		// URIs
		URI workingPathUri = workingPath.toUri();
		Assert.assertEquals("file:///C:/prog/java/workspaces/test/ch.inftec.ju/ju-util/./", workingPathUri.toString());
		
		// Conversions to and from java.io
		File workingPathAsFile = workingPath.toFile();
		Assert.assertEquals(workingPath, workingPathAsFile.toPath());
		
		// FileStore
		FileSystem fileSystem = workingPath.getFileSystem();
		Iterator<Path> rootDirectories = fileSystem.getRootDirectories().iterator();
		Assert.assertEquals(Paths.get("C:\\"), rootDirectories.next());
		Assert.assertEquals(Paths.get("D:\\"), rootDirectories.next());
		
		// Create path relative to other
		Path testPath = workingPath.resolve("target/testDir");
		Assert.assertEquals(".\\target\\testDir", testPath.toString()); // Note: Path is OS specific
				
		// Common file operations
		
		Assert.assertTrue(Files.exists(workingPath));
		Assert.assertTrue(Files.isDirectory(workingPath));
		
		Files.deleteIfExists(testPath); // Note: Directory must be empty!
		Files.createDirectories(testPath);
		
		Path testFile = testPath.resolve("testFile.txt");
		Files.createFile(testFile);
		
		// Write to file
		
		try (Writer writer = Files.newBufferedWriter(testFile, Charset.forName("UTF-8"), StandardOpenOption.WRITE)) {
			writer.append("Hello World!");
		}
		
		// Read from file
		List<String> lines = Files.readAllLines(testFile, Charset.forName("UTF-8"));
		Assert.assertEquals(1, lines.size());
		Assert.assertEquals("Hello World!", lines.get(0));
		
		// Delete file
		Files.delete(testFile);
	}
}
