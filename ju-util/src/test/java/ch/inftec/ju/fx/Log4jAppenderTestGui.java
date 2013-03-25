package ch.inftec.ju.fx;

import java.util.Random;

import javafx.scene.layout.Pane;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.ThreadUtils;
import ch.inftec.ju.util.fx.JuFxUtils;

public class Log4jAppenderTestGui {
	private Logger logger = LoggerFactory.getLogger(Log4jAppenderTestGui.class);
	
	@Test
	public void log4jAppender() {
		Log4jAppenderViewModel model = new Log4jAppenderViewModel();
		logger.info("Before adding appender");
		model.register();
		logger.info("After adding appender");
		
		Pane pane = Log4jAppenderController.loadPane(model);
		
		// Start a Thread that adds info messages
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
		
		// Start a Thread that adds random messages
		Thread thread2 = new Thread(new Runnable() {
			private Random random = new Random();
			
			@Override
			public void run() {
				while (true) {
					int type = random.nextInt(5);
					switch (type) {
					case 0: logger.error("Aaaaaaaaaaaaa Error"); break;
					case 1: logger.warn("Ooops Warn"); break;
					case 2: logger.info("Oooo Info"); break;
					case 3: logger.debug("Iiii Debug"); break;
					case 4: logger.trace("Uuuu Trace"); break;
					}
					ThreadUtils.sleep(random.nextInt(500) + 200);						
				}
			}
		});
		thread2.setDaemon(true);
		thread2.start();
		
		logger.error("ERR");
		logger.warn("WARN");
		logger.info("INFO");
		logger.debug("DEBUG");
		logger.trace("TRACE");
		
		JuFxUtils.startApplication()
			.pane(pane)
			.title("Log4jAppender Test")
			.start();
		

	}
}
