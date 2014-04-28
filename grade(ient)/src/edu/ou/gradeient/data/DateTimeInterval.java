package edu.ou.gradeient.data;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.MutableInterval;
import org.joda.time.ReadableInterval;

/**
 * Represents an interval with start and end date/times.
 */
public abstract class DateTimeInterval implements DateInterval, TimeInterval {

	protected MutableInterval interval;
	
	/**
	 * Gets the interval object for this interval.
	 */
	public ReadableInterval getInterval() {
		return interval;
	}
	
	/**
	 * Gets the start time/date of this interval.
	 */
	public DateTime getStart() {
		return interval.getStart();
	}
	
	/**
	 * Gets the start time/date of this interval in milliseconds since the 
	 * Unix epoch.
	 */
	public long getStartMillis() {
		return interval.getStartMillis();
	}
	
	//TODO does this provide adequately correct time zone support?
	@Override
	public LocalDate getStartDate() {
		return new LocalDate(interval.getStart());
	}
	
	@Override
	public LocalTime getStartTime() {
		return new LocalTime(interval.getStart());
	}

	/**
	 * Gets the end time/date of this interval.
	 */
	public DateTime getEnd() {
		return interval.getEnd();
	}
	
	/**
	 * Gets the end time/date of this interval in milliseconds since the 
	 * Unix epoch.
	 */
	public long getEndMillis() {
		return interval.getEndMillis();
	}
	
	@Override
	public LocalDate getEndDate() {
		return new LocalDate(interval.getEnd());
	}
	
	@Override
	public LocalTime getEndTime() {
		return new LocalTime(interval.getEnd());
	}
	
	/**
	 * Sets the start date/time for the interval.
	 * @param start The new start date/time.
	 * @param maintainDuration If this is true, the end date/time will be
	 * shifted to maintain the interval's previous duration. If this is false 
	 * and start is after end, end will be updated to be the same as start.
	 * @throws IllegalArgumentException if start is < 0
	 */
	public void setStart (long start, boolean maintainDuration) {
		if (maintainDuration) {
			setStartAndEnd(start, start + interval.toDurationMillis());
		} else {
			if (interval.isBefore(start))
				setStartAndEnd(start, start);
			else
				interval.setStartMillis(start);
		}
	}
	
	@Override
	public void setStartTime(int hour, int minute, boolean maintainDuration) {
		setStart(interval.getStart().withTime(hour, minute, 0, 0).getMillis(), 
				maintainDuration);
	}
	
	@Override
	public void setStartDate(int year, int month, int day, 
			boolean maintainDuration) {
		if (year < 1970 || year > 2036)
			throw new IllegalArgumentException("year must be 1970-2036");
		
		setStart(interval.getStart().withDate(year, month, day)
				.getMillis(), maintainDuration);
	}
	
	/** 
	 * Set the new end time/date for the interval.
	 * @param end The new end time/date. If it is before start, start will be 
	 * updated to be the same as end.
	 * @throws IllegalArgumentException if end < 0
	 */
	public void setEnd(long end) {
		if (interval.isAfter(end))
			setStartAndEnd(end, end);
		else
			interval.setEndMillis(end);
	}
	
	@Override
	public void setEndTime(int hour, int minute, boolean incDayIfEndBeforeStart) {
		long end = interval.getEnd().withTime(hour, minute, 0, 0)
				.getMillis();
		if (incDayIfEndBeforeStart && interval.isAfter(end))
			setStartAndEnd(end, end);
		else
			setEnd(end);
	}
	
	@Override
	public void setEndDate(int year, int month, int day) {
		if (year < 1970 || year > 2036)
			throw new IllegalArgumentException("year must be 1970-2036");

		setEnd(interval.getEnd().withDate(year, month, day).getMillis());
	}
	
	/**
	 * Set the new start and end date/time for the task.
	 * @param start The new start time
	 * @param end The new end time
	 * @throws IllegalArgumentException if start > end
	 */
	public void setStartAndEnd(long start, long end) {
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
	 * @param shiftBy The amount of time by which to shift, in milliseconds. 
	 * A positive time shifts the interval to a later time than the original. 
	 */
	public void shiftTimeOfInterval(long shiftBy) {
		interval.setStartMillis(interval.getStartMillis() + shiftBy);
		interval.setEndMillis(interval.getEndMillis() + shiftBy);
	}
}
