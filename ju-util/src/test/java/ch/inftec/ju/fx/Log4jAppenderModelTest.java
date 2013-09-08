package ch.inftec.ju.fx;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.fx.JuFxUtils;

@Ignore("TODO: Make sure GUI tests run smoothly on CI server or allow flagging of execution")
public class Log4jAppenderModelTest {
	private final Logger logger = LoggerFactory.getLogger(Log4jAppenderModelTest.class);
	
	@Test
	public void maxEntries() {
		JuFxUtils.runAndWaitInFxThread(new Runnable() {
			@Override
			public void run() {
				Log4jAppenderModel model = new Log4jAppenderModel();
				model.register().addToRootLogger();
				
				model.maxLogEntriesProperty().set(2);
				
				Assert.assertEquals(0, model.getLogEvents().size());
				
				logger.info("I1");
				Assert.assertEquals(1, model.getLogEvents().size());
				
				logger.info("I2");
				Assert.assertEquals(2, model.getLogEvents().size());
				Assert.assertEquals("I1", model.getLogEvents().get(0).getMessage());
				Assert.assertEquals("I2", model.getLogEvents().get(1).getMessage());
				
				logger.info("I3");
				Assert.assertEquals(2, model.getLogEvents().size());
				Assert.assertEquals("I2", model.getLogEvents().get(0).getMessage());
				Assert.assertEquals("I3", model.getLogEvents().get(1).getMessage());
				
				// Increase entries
				model.maxLogEntriesProperty().set(3);
				logger.info("I4");
				Assert.assertEquals(3, model.getLogEvents().size());
				Assert.assertEquals("I2", model.getLogEvents().get(0).getMessage());
				Assert.assertEquals("I3", model.getLogEvents().get(1).getMessage());
				Assert.assertEquals("I4", model.getLogEvents().get(2).getMessage());
				
				// Decrease entries
				model.maxLogEntriesProperty().set(2);
				Assert.assertEquals(2, model.getLogEvents().size());
				Assert.assertEquals("I3", model.getLogEvents().get(0).getMessage());
				Assert.assertEquals("I4", model.getLogEvents().get(1).getMessage());
			}
		});
	}
	
	@Test
	public void register() {
		JuFxUtils.runAndWaitInFxThread(new Runnable() {
			@Override
			public void run() {
				Logger l1 = LoggerFactory.getLogger("log.l1");
				Logger l2 = LoggerFactory.getLogger("log.l2");
				Logger l3 = LoggerFactory.getLogger("l3");
				Logger l4 = LoggerFactory.getLogger("l4");
				
				Log4jAppenderModel model = new Log4jAppenderModel();
				model.register()
					.addToLogger("log")
					.addToLogger("log.l1")
					.addToLogger("l4");
				
				// Log to a logger that was added
				int logs = 0;
				Assert.assertEquals(logs, model.getLogEvents().size());
				
				l1.info("test1");
				Assert.assertEquals(logs += 1, model.getLogEvents().size());
				
				l2.info("test2");
				Assert.assertEquals(logs += 1, model.getLogEvents().size());
				
				// l3 won't log at all
				l3.info("test3");
				Assert.assertEquals(logs += 0, model.getLogEvents().size());
				
				l4.info("test4");
				Assert.assertEquals(logs += 1, model.getLogEvents().size());
				
				Log4jAppenderModel model2 = new Log4jAppenderModel();
				model2.register()
					.addToLogger("log")
					.addToRootLogger();
				
				l1.info("test1b");
				Assert.assertEquals(1, model2.getLogEvents().size());
			}
		});
	}
}
