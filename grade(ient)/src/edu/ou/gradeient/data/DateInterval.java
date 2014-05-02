package edu.ou.gradeient.data;

import org.joda.time.LocalDate;

/**
 * Represents an interval with start and end dates, but not necessarily times.
 */
public interface DateInterval {
	/**
	 * Gets the start date of the interval.
	 */
	public LocalDate getStartDate();
	
	/**
	 * Gets the end date of the interval.
	 */
	public LocalDate getEndDate();
	
	/**
	 * Sets the new start date for the interval.
	 * @param year new year, 1970-2036
	 * @param month new month of year, 0-11
	 * @param day new day of month, 1-31
	 * @param maintainDuration If this is true, the end date will be shifted
	 * to maintain the interval's previous duration. If this is false and 
	 * setting start's date to the given values results in a date that is 
	 * after end, end will be updated to be the same as start.
	 * @throws IllegalArgumentException if day, month, or year is invalid
	 */
	public void setStartDate(int year, int month, int day, 
			boolean maintainDuration);
	
	/**
	 * Sets the new end date for the interval. If the resulting end time/date is 
	 * before start, start will be updated to be the same as end.
	 * @param year new year, 1970-2036
	 * @param month new month of year, 1-12
	 * @param day new day of month, 1-31
	 * @throws IllegalArgumentException if day, month, or year is invalid
	 */
	public void setEndDate(int year, int month, int day);
}
