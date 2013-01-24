package ch.inftec.ju.util;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the Timer class.
 * @author Martin
 *
 */
public class TimerTest {
	final Logger log = LoggerFactory.getLogger(TimerTest.class);
	
	@Test
	public void elapsedString() {
		Timer t1 = new Timer(); t1.pause(); t1.setStartTime(new Date(t1.getCurrentTime().getTime() - 123));
		Timer t2 = new Timer(); t2.pause(); t2.setStartTime(new Date(t2.getCurrentTime().getTime() -  23 -  1*1000));
		Timer t3 = new Timer(); t3.pause(); t3.setStartTime(new Date(t3.getCurrentTime().getTime() -   3 - 32*1000 -  9*60*1000));
		Timer t4 = new Timer(); t4.pause(); t4.setStartTime(new Date(t4.getCurrentTime().getTime() - 987 -  6*1000 - 54*60*1000 - 32*60*60*1000));
		
		Assert.assertEquals(".123s", t1.getElapsedString());
		Assert.assertEquals(" 1.023s", t2.getElapsedString());
		Assert.assertEquals(" 9m 32.003s", t3.getElapsedString());
		Assert.assertEquals("32h 54m  6.987s", t4.getElapsedString());
	}
	
	/**
	 * Simple test making sure memoryUsage works on basic use cases.
	 */
	@Test	
	public void memoryUsage() {
		Timer t = new Timer(log, "memoryUsage");
		t.memoryUsage("Started");
		
		t.pause();
		t.memoryUsage("Paused");
		
		t.resume();
		t.memoryUsage("Resumed");
	}
}
