package ch.inftec.ju.util;

import java.util.Date;

/**
 * Helper class to track time, i.e. for performance debugging needs.
 * <br>
 * The toString method returns the getElapsedString text.
 * @author Martin
 *
 */
public final class Timer {
	private Date startTime;
	private Date pauseTime;
	
	/**
	 * Creates a new timer with the current time as start time.
	 */
	public Timer() {
		this.startTime = new Date();
	}
	
	/**
	 * Creates a new Timer with the specified start time.
	 * @param startTime Start time
	 */
	public Timer(Date startTime) {
		this.startTime = new Date(startTime.getTime());
	}
	
	/**
	 * Sets the start time of the timer. This method has package protection
	 * so it can be used for unit testing.
	 * @param startTime Start time
	 */
	void setStartTime(Date startTime) {
		// TODO: Make startTime final, remote setStartTime
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
	}
	
	/**
	 * Resumes the timer, i.e. clears any pause that might have occurred. All upcoming calls will
	 * use the current time to compute the elapsed time.
	 */
	public void resume() {
		this.pauseTime = null;
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
	 * Returns the elapsed time as a String, i.e. the same that getElapsedString returns.
	 * @return Elapsed time String
	 */
	@Override
	public String toString() {
		return this.getElapsedString();
	}
}
