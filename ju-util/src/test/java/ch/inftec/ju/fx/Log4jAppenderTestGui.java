package ch.inftec.ju.fx;

import java.util.Random;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.ThreadUtils;
import ch.inftec.ju.util.fx.JuFxUtils;
import ch.inftec.ju.util.fx.JuFxUtils.PaneInfo;

public class Log4jAppenderTestGui {
	private Logger logger = LoggerFactory.getLogger(Log4jAppenderTestGui.class);
	
	@Test
	public void log4jAppender() {
		Log4jAppenderViewModel model = new Log4jAppenderViewModel();
		logger.info("Before adding appender");
		model.register();
		logger.info("After adding appender");
		
		PaneInfo<Log4jAppenderController> paneInfo = JuFxUtils.loadPane(IOUtil.getResourceURL("Log4jAppender.fxml"), null);
		paneInfo.getController().setModel(model);
		
		// Start a Thread that adds messages
		Thread thread = new Thread(new Runnable() {
			private Random random = new Random();
			
			@Override
			public void run() {
				while (true) {
					logger.info("Hello there " + System.currentTimeMillis());
					ThreadUtils.sleep(random.nextInt(500) + 500);						
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
		
		JuFxUtils.startApplication()
			.pane(paneInfo.getPane())
			.title("Log4jAppender Test")
			.start();
		

	}
}
