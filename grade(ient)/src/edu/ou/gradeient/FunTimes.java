package edu.ou.gradeient;

import org.joda.time.MutableInterval;

/**
 * Handling time is fun! Let's make utility methods for it!
 */
public class FunTimes {
	/**
	 * Sets the start date/time for the interval.
	 * @param interval The interval in question (will be modified).
	 * @param start The new start date/time.
	 * @param maintainDuration If this is true, the end date/time will be
	 * shifted to maintain the task's previous duration. If this is false and
	 * start is after end, end will be updated to be the same as start.
	 * @throws IllegalArgumentException if start is < 0
	 */
	public static void setStart (MutableInterval interval, long start, 
			boolean maintainDuration) {
		if (maintainDuration) {
			setStartAndEnd(interval, start, start + interval.toDurationMillis());
		} else {
			if (interval.isBefore(start))
				setStartAndEnd(interval, start, start);
			else
				interval.setStartMillis(start);
		}
	}
	
	/**
	 * Sets the new start time for the interval.
	 * @param interval The interval in question (will be modified).
	 * @param hour new hour of day, 0-23
	 * @param minute new minute of hour, 0-59
	 * @param maintainDuration If this is true, the end time will be shifted
	 * to maintain the task's previous duration. If this is false and setting
	 * start's time to hour and minute results in a time that is after end,
	 * end will be updated to be the same as start.
	 * @throws IllegalArgumentException if minute or hour is invalid
	 */
	public static void setStartTime(MutableInterval interval, int hour, 
			int minute, boolean maintainDuration) {
		setStart(interval, interval.getStart().withTime(hour, minute, 0, 0)
				.getMillis(), maintainDuration);
	}
	
	/**
	 * Sets the new start date for the interval.
	 * @param interval The interval in question (will be modified).
	 * @param year new year, 1970-2036
	 * @param month new month of year, 0-11
	 * @param day new day of month, 1-31
	 * @param maintainDuration If this is true, the end date will be shifted
	 * to maintain the task's previous duration. If this is false and setting
	 * start's date to the given values results in a date that is after end,
	 * end will be updated to be the same as start.
	 * @throws IllegalArgumentException if day, month, or year is invalid
	 */
	public static void setStartDate(MutableInterval interval, int year, 
			int month, int day, boolean maintainDuration) {
		if (year < 1970 || year > 2036)
			throw new IllegalArgumentException("year must be 1970-2036");
		
		setStart(interval, interval.getStart().withDate(year, month, day)
				.getMillis(), maintainDuration);
	}
	
	/** 
	 * Set the new end time/date for the interval.
	 * @param interval The interval in question (will be modified).
	 * @param end The new end time/date. If it is before start, start will be 
	 * updated to be the same as end.
	 * @throws IllegalArgumentException if end < 0
	 */
	public static void setEnd (MutableInterval interval, long end) {
		if (interval.isAfter(end))
			setStartAndEnd(interval, end, end);
		else
			interval.setEndMillis(end);
	}
	
	/**
	 * Sets the new end time for the interval.
	 * @param interval The interval in question (will be modified).
	 * @param hour new hour, 0-23
	 * @param minute new minute, 0-59
	 * @param incDayIfEndBeforeStart If this is true and the resulting end time
	 * is before start, increment the new end time's day. If this is false and
	 * the resulting end time is before start, start will be updated to be the
	 * same as end.
	 * @throws IllegalArgumentException if hour or minute is invalid
	 */
	public static void setEndTime(MutableInterval interval, int hour, 
			int minute, boolean incDayIfEndBeforeStart) {
		long end = interval.getEnd().withTime(hour, minute, 0, 0)
				.getMillis();
		if (incDayIfEndBeforeStart && interval.isAfter(end))
			setStartAndEnd(interval, end, end);
		else
			setEnd(interval, end);
	}
	
	/**
	 * Sets the new end date for the interval. If the resulting end time/date is 
	 * before start, start will be updated to be the same as end.
	 * @param interval The interval in question (will be modified).
	 * @param year new year, 1970-2036
	 * @param month new month of year, 1-12
	 * @param day new day of month, 1-31
	 * @throws IllegalArgumentException if day, month, or year is invalid
	 */
	public static void setEndDate(MutableInterval interval, int year, 
			int month, int day) {
		if (year < 1970 || year > 2036)
			throw new IllegalArgumentException("year must be 1970-2036");

		setEnd(interval, 
				interval.getEnd().withDate(year, month, day).getMillis());
	}
	
	/**
	 * Set the new start and end date/time for the task.
	 * @param interval The interval in question (will be modified).
	 * @param start The new start time
	 * @param end The new end time
	 * @throws IllegalArgumentException if start > end
	 */
	public static void setStartAndEnd(MutableInterval interval, long start, 
			long end) {
		if (start > end)
			throw new IllegalArgumentException("start must be before end");
		
		// Set start and end in an order that won't throw...
		if (interval.contains(start) || interval.isAfter(start)) {
			interval.setStartMillis(start);
			interval.setEndMillis(end);
		} else {
			interval.setEndMillis(end);
			interval.setStartMillis(start);
		}
	}
	
	/**
	 * Shifts the interval by the amount of time specified. 
	 * @param interval The interval to be shifted
	 * @param shiftBy The amount of time by which to shift, in milliseconds. 
	 * A positive time shifts the interval to a later time than the original. 
	 */
	public static void shiftTimeOfInterval (MutableInterval interval, 
			long shiftBy) {
		interval.setStartMillis(interval.getStartMillis() + shiftBy);
		interval.setEndMillis(interval.getEndMillis() + shiftBy);
	}

}
