package ch.inftec.ju.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingTest {
	@Test
	public void log() {
		Logger log = LoggerFactory.getLogger(LoggingTest.class);
		
		log.trace("This is a TRACE log message");
		log.debug("This is a DEBUG log message");
		log.info("This is a INFO log message");
		log.warn("This is a WARN log message");
		log.error("This is a ERROR log message");
	}
}
