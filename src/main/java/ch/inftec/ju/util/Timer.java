package ch.inftec.ju.util;

import java.util.Date;

import org.slf4j.Logger;

/**
 * Helper class to track time, i.e. for performance debugging needs.
 * <br>
 * The toString method returns the getElapsedString text.
 * @author Martin
 *
 */
public final class Timer {
	private Date startTime;
	private long startMemory;
	
	private Date pauseTime;
	private Long pauseMemory;
	
	private final Logger logger;	
	
	/**
	 * Creates a new time stopper using the current time and outputs a start message
	 * to the Logger.
	 * @param logger Logger If null, no output will be made
	 * @param description Description that will be output through the logger. If null, no
	 * output will be made
	 */
	public Timer(Logger logger, String description) {
		this(logger, description, null);
	}
	
	/**
	 * Creates a new time stopper and outputs a start message
	 * to the Logger.
	 * @param logger Logger If null, no output will be made
	 * @param description Description that will be output through the logger. If null, no
	 * output will be made
	 * @param startTime Start time of the timer. If null, the current time is used.
	 */
	public Timer(Logger logger, String description, Date startTime) {
		this.logger = logger;
		this.startTime = startTime == null ? new Date() : startTime;
		this.start(description);
	}
	
	/**
	 * Creates a new timer with the current time as start time.
	 */
	public Timer() {
		this(null, null, null);
	}
	
	/**
	 * Creates a new Timer with the specified start time.
	 * @param startTime Start time
	 */
	public Timer(Date startTime) {
		this(null, null, startTime);
	}
	
	private void log(String msg) {
		if (logger != null) logger.info(msg);
	}
	
	private void start(String description) {
		this.log(description + ": Started");
		this.startMemory = Runtime.getRuntime().freeMemory();
	}
	
	/**
	 * (Re)starts the time stopper.
	 * @param description Description to be output (if a logger was defined)
	 */
	public void restart(String description) {
		this.startTime = new Date();
		this.start(description);
	}
	
	/**
	 * Outputs the specified description with the elapsed time through
	 * the logger.
	 * @param description Description of the event
	 */
	public void stop(String description) {
		if(logger != null) logger.info(description + ": " + this.getElapsedString());
	}
	
	/**
	 * Sets the start time of the timer. This method has package protection
	 * so it can be used for unit testing.
	 * @param startTime Start time
	 */
	void setStartTime(Date startTime) {
		// TODO: Make startTime final, remove setStartTime
		this.startTime = startTime;
	}
	
	/**
	 * Gets the start time of the timer.
	 * @return Start time
	 */
	public Date getStartTime() {
		return this.startTime;
	}
	
	/**
	 * Gets the current time of the timer. If the timer is in paused mode, it will be the time
	 * when it was paused. Otherwise, it will be the actual current time.
	 * <br>
	 * This is the time the timer will use to compute the elapsed time
	 * @return Current time of the timer
	 */
	public Date getCurrentTime() {
		return this.pauseTime != null ? this.pauseTime : new Date();
	}
	
	/**
	 * Pauses the timer. All upcoming calls will use this time to compute the elapsed time.
	 */
	public void pause() {
		this.pauseTime = new Date();
		this.pauseMemory = Runtime.getRuntime().freeMemory();
	}
	
	/**
	 * Resumes the timer, i.e. clears any pause that might have occurred. All upcoming calls will
	 * use the current time to compute the elapsed time.
	 */
	public void resume() {
		this.pauseTime = null;
		this.pauseMemory = null;
	}
	
	/**
	 * Gets the elapsed milliseconds since the start time.
	 * @return
	 */
	public long getElapsedMillis() {
		return this.getCurrentTime().getTime() - this.startTime.getTime();
	}
	
	/**
	 * Gets a string containing the elapsed time, in the format of
	 * 99h 35m 32.345s. Minutes and seconds will be padded: 99h  3m  3.349s
	 * <br>
	 * Hours and minutes will be omitted if not present. One second
	 * digit will always be present. Shortest form is: .453s
	 * @return Elapsed time as readable string
	 */
	public String getElapsedString() {
		long elapsedMillis = this.getElapsedMillis();
		long remainingMillis = elapsedMillis;
		
		long hours = remainingMillis / 1000 / 60 / 60;
		remainingMillis -= hours * 1000 * 60 * 60;
		
		long minutes = remainingMillis / 1000 / 60;
		remainingMillis -= minutes * 1000 * 60;
		
		long seconds = remainingMillis / 1000;
		remainingMillis -= seconds * 1000;
		
		long millis = remainingMillis;
		
		XString s = new XString();
		if (hours > 0) s.addText(hours, "h ");
		if (hours > 0 || minutes > 0) {
			if (minutes < 10) s.addText(" ");
			s.addText(minutes, "m ");
		}
		if (hours > 0 || minutes > 0 || seconds > 0) {
			if (seconds < 10) s.addText(" ");
			s.addText(seconds);
		}
		s.addText(".");
		if (millis < 100) s.addText("0");
		if (millis < 10) s.addText("0");
		s.addText(millis);
		s.addText("s");
		
		return s.toString();
	}

	/**
	 * Outputs the memory usage since the creation of the TimeStopper
	 * through the logger.
	 * @param description Description for the event
	 */
	public void memoryUsage(String description) {
		if (this.logger == null) return;
		
		long memory = this.pauseMemory == null ? Runtime.getRuntime().freeMemory() : this.pauseMemory;
		long usedMemory = memory - this.startMemory;
		
		String usedMemoryString = null;
		if (usedMemory > 1000000) usedMemoryString = (usedMemory / 1000000f) + " MBytes";
		else if (usedMemory > 1000) usedMemoryString = (usedMemory / 1000f) + " KBytes";
		else usedMemoryString = usedMemory + " Bytes";
		
		this.logger.info(description + ": " + usedMemoryString);
	}
	
	/**
	 * Returns the elapsed time as a String, i.e. the same that getElapsedString returns.
	 * @return Elapsed time String
	 */
	@Override
	public String toString() {
		return this.getElapsedString();
	}
}
