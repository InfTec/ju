package ch.inftec.ju.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class LoggingTest {
	@Test
	public void log() {
		Log log = LogFactory.getLog(LoggingTest.class);
		
		log.trace("This is a TRACE log message");
		log.debug("This is a DEBUG log message");
		log.info("This is a INFO log message");
		log.warn("This is a WARN log message");
		log.error("This is a ERROR log message");
	}
}
