package edu.ou.gradeient.data;

import org.joda.time.LocalTime;

/**
 * Represents an interval with start and end times, but not necessarily dates.
 */
public interface TimeInterval {
	/**
	 * Gets the start time of the interval.
	 */
	public LocalTime getStartTime();
	
	/**
	 * Gets the end time of the interval.
	 */
	public LocalTime getEndTime();
	
	/**
	 * Sets the new start time for the interval.
	 * @param hour new hour of day, 0-23
	 * @param minute new minute of hour, 0-59
	 * @param maintainDuration If this is true, the end time will be shifted
	 * to maintain the interval's previous duration. If this is false and 
	 * setting start's time to hour and minute results in a time that is 
	 * after end, end will be updated to be the same as start.
	 * @throws IllegalArgumentException if minute or hour is invalid
	 */
	public void setStartTime(int hour, int minute, boolean maintainDuration);

	/**
	 * Sets the new end time for the interval.
	 * @param hour new hour, 0-23
	 * @param minute new minute, 0-59
	 * @param incDayIfEndBeforeStart If this is true and the resulting end time
	 * is before start, increment the new end time's day. If this is false and
	 * the resulting end time is before start, start will be updated to be the
	 * same as end.
	 * @throws IllegalArgumentException if hour or minute is invalid
	 */
	public void setEndTime(int hour, int minute, boolean incDayIfEndBeforeStart);
}
