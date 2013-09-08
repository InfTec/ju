package ch.inftec.ju.fx;


import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.fx.Log4jAppenderViewModel.LogEntry;
import ch.inftec.ju.util.ThreadUtils;
import ch.inftec.ju.util.fx.JuFxUtils;

@Ignore("TODO: Make sure GUI tests run smoothly on CI server or allow flagging of execution")
public class Log4jAppenderViewModelTest {
	@Test
	public void logEntry() {
		JuFxUtils.runAndWaitInFxThread(new Runnable() {
			@Override
			public void run() {
				final Logger l1 = LoggerFactory.getLogger("log.l1");
				
				Log4jAppenderModel model = new Log4jAppenderModel();
				model.register().addToLogger("log");
				
				final Log4jAppenderViewModel viewModel = new Log4jAppenderViewModel(model);
				
				// Log to a logger that was added
				l1.info("Test1");
				
				final LogEntry e1 = viewModel.getLogEntries().get(0);
				Assert.assertEquals("Test1", e1.getMessage());
				Assert.assertEquals("INFO", e1.getLevel());
				Assert.assertNotNull(e1.getIcon());
				Assert.assertNotNull(e1.getImageView());
				Assert.assertEquals("l1", e1.getLoggerName());
				
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						l1.info("Test2");
					}
				});
				t.start();
				ThreadUtils.join(t);
				
				// We need to run this later, otherwise it won't be visible
				JuFxUtils.runInFxThread(new Runnable() {
					@Override
					public void run() {
						Assert.assertEquals(2, viewModel.getLogEntries().size());
						LogEntry e2 = viewModel.getLogEntries().get(0);
						Assert.assertEquals("Test2", e2.getMessage());
						Assert.assertFalse(e1.getThreadName().equals(e2.getThreadName()));
					}
				}, true);
			}
		});
	}
}
