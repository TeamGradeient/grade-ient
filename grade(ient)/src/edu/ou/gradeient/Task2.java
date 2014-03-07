package edu.ou.gradeient;

import android.text.format.Time;

public class Task2 
{
	/**A String containing the name of this task*/
	private String name;
	
	/**The name of the subject with which this task is associated.*/
	private String subject;
	
	/**A string containing notes about this task*/
	private String notes;
	
	/**True if the task is done, false otherwise.*/
	private boolean isDone;
	
	private Time start;
	private Time end;
	
	/**The time zone in which this task was created*/
	private String originTimeZone;

	/**
	 * Creates a default task with the name given.
	 * @param name The name of the task. Must not be null.
	 * @param start The start time/date of the task. Must not be null.
	 * @param end The end time/date of the task. Must not be null.
	 * @throws IllegalArgumentException if name is null or start or end is
	 * invalid (including if one is null)
	 */
	public Task2 (String name, Time start, Time end)
	{
		this(name, start, end, null, null, false);
	}
	
	/**
	 * Creates a new task with the given parameters
	 * @param name The task's name. Must not be null.
	 * @param start The task's start time/date. Must not be null.
	 * @param end The task's end time/date. Must not be null.
	 * @param subject The task's subject. Can be null.
	 * @param notes The task's notes. Can be null.
	 * @param isDone Whether the task is done
	 * @throws IllegalArgumentException if name, start, or end is null,
	 * or if end is invalid relative to start.
	 */
	public Task2 (String name, Time start, Time end, String subject,
			String notes, boolean isDone) {
		if (name == null)
			throw new IllegalArgumentException("Name cannot be null.");
		if (start == null)
			throw new IllegalArgumentException("Start cannot be null.");
		if (end == null)
			throw new IllegalArgumentException("End cannot be null.");
		
		this.name = name;
		this.start = new Time(start);
		setEnd(end);
		this.originTimeZone = Time.getCurrentTimezone();
		setSubject(subject);
		setNotes(notes);
		this.isDone = isDone;
	}
	
	/**
	 * Returns the name of this task
	 * @return The name of this task
	 */
	public String getName ()
	{
		return name;
	}
	
	/**
	 * Returns the notes for this task
	 * @return The notes for this task
	 */
	public String getNotes ()
	{
		return notes;			
	}
	
	/**
	 * Returns a COPY of the task's start time/date.
	 */
	public Time getStart() {
		return new Time(start);
	}
	
	/**
	 * Returns the start time/date in milliseconds since the Unix epoch.
	 * @param ignoreDst See android.text.format.Time.toMillis.
	 */
	public long getStartMillis(boolean ignoreDst) {
		return start.toMillis(ignoreDst);
	}
	
	/**
	 * Returns a COPY of the task's end time/date.
	 */
	public Time getEnd() {
		return new Time(end);
	}
	
	/**
	 * Returns the end time/date in milliseconds since the Unix epoch.
	 * @param ignoreDst See android.text.format.Time.toMillis.
	 */
	public long getEndMillis(boolean ignoreDst) {
		return end.toMillis(ignoreDst);
	}
	
	/**
	 * Returns the subject name of this task
	 * @return The subject name of this task, or null if no subject is 
	 * associated with the task
	 */
	public String getSubject ()
	{
		return subject;
	}
	
	/**
	 * Returns true if this task is done, and false otherwise
	 * @return True if this task is done, and false otherwise
	 */
	public boolean isDone ()
	{
		return isDone;
	}
	
	/**
	 * Gets the time zone that the user's device was set to when this
	 * task was created.
	 * @return The time zone
	 */
	public String getOriginTimeZone () 
	{
		return originTimeZone;
	}
	
	/**
	 * Sets the name of this task.
	 * @param newName The name to set
	 * @throws IllegalArgumentException if name is null
	 */
	public void setName (String newName)
	{
		if (newName == null)
		{
			throw new IllegalArgumentException ("Task name cannot be null");
		}
		name = newName;
	}
	
	/**
	 * Sets the subject of this task.
	 * @param newSubject The subject to be set. If this is null, the empty
	 * string will be used.
	 */
	public void setSubject (String newSubject)
	{
		subject = newSubject == null ? "" : newSubject;
	}
	
	/**
	 * Sets whether the task is done
	 * @param newIsDone
	 */
	public void setIsDone (boolean newIsDone)
	{
		isDone = newIsDone;
	}
	
	/**
	 * Sets the notes for this task.
	 * @param notes The notes to set. If this is null, the empty string
	 * will be used.
	 */
	public void setNotes (String notes) {
		this.notes = notes == null ? "" : notes;
	}
	
	/**
	 * Sets the start date/time for the task.
	 * @param newStart The new start date/time. Seconds will be ignored.
	 * @param maintainDuration If this is true, the end date/time will be
	 * shifted to maintain the task's previous duration. If this is false and
	 * newStart is after end, end will be updated to be the same as newStart.
	 * @throws IllegalArgumentException if newStart is null
	 */
	public void setStart(Time newStart, boolean maintainDuration) {
		if (newStart == null)
			throw new IllegalArgumentException("Start must not be null.");
		
		//TODO update anything about time zone or DST?
		if (maintainDuration) {
			long newStartMillis = newStart.toMillis(true);
			newStartMillis -= newStartMillis % 60; // get rid of seconds
			long shiftBy = newStart.toMillis(true) - start.toMillis(true);
			shiftTimes(shiftBy);
		} else {
			start = new Time(newStart);
			start.second = 0;
			if (start.after(end))
				end = new Time(start);
			//TODO will need to handle cutting off work times or not
		}
	}
	
	/**
	 * Sets the new start time for the task.
	 * @param minute new minute of hour, 0-59
	 * @param hour new hour of day, 0-23
	 * @param maintainDuration If this is true, the end time will be shifted
	 * to maintain the task's previous duration. If this is false and setting
	 * start's time to hour and minute results in a time that is after end,
	 * end will be updated to be the same as start.
	 * @throws IllegalArgumentException if minute or hour is invalid
	 */
	public void setStartTime(int minute, int hour, boolean maintainDuration) {
		if (minute < 0 || minute > 59)
			throw new IllegalArgumentException("minute must be 0-59");
		if (hour < 0 || hour > 23)
			throw new IllegalArgumentException("hour must be 0-23");
		
		//TODO work times, DST?
		if (maintainDuration) {
			int minutes = end.minute - start.minute;
			int hours = end.hour - start.hour;
			updateTime(start, minute, hour);
			updateTime(end, minute + minutes, hour + hours);
		} else {
			updateTime(start, minute, hour);
			if (start.after(end))
				end = new Time(start);
		}
	}
	
	/**
	 * Sets the new start time (but not date) for the task.
	 * @param startTime The new start time. Only hours and minutes will be used.
	 * @param maintainDuration If this is true, the end time will be
	 * shifted to maintain the task's previous duration. If this is false and
	 * setting start's time to startTime results in a time that is after end,
	 * end will be updated to be the same as start.
	 * @throws IllegalArgumentException if startTime is null
	 */
	public void setStartTime(Time startTime, boolean maintainDuration) {
		if (startTime == null)
			throw new IllegalArgumentException("Start time must not be null.");
		setStartTime(startTime.minute, startTime.hour, maintainDuration);
	}
	
	/**
	 * Sets the new start date for the task. Performs normalization so that
	 * invalid month/day combinations will be fixed.
	 * @param monthDay new day of month, 1-31
	 * @param month new month of year, 0-11
	 * @param year new year, 1970-2036
	 * @param maintainDuration If this is true, the end date will be shifted
	 * to maintain the task's previous duration. If this is false and setting
	 * start's date to the given values results in a date that is after end,
	 * end will be updated to be the same as start.
	 * @throws IllegalArgumentException if monthDay, month, or year is invalid
	 * (note that this does not check validity of day/month combinations)
	 */
	public void setStartDate(int monthDay, int month, int year,
			boolean maintainDuration) {
		if (monthDay < 1 || monthDay > 31)
			throw new IllegalArgumentException("day of month must be 1-31");
		if (month < 0 || month > 11)
			throw new IllegalArgumentException("month must be 0-11");
		if (year < 1970 || year > 2036)
			throw new IllegalArgumentException("year must be 1970-2036");
		
		//TODO work times, DST
		if (maintainDuration) {
			int days = end.monthDay - start.monthDay;
			int months = end.month - start.month;
			int years = end.year - start.year;
			updateDate(start, monthDay, month, year);
			updateDate(end, monthDay + days, month + months, year + years);
		} else {
			updateDate(start, monthDay, month, year);
			if (start.after(end))
				end = new Time(start);
		}
	}
	
	/**
	 * Sets the new start date (but not time) for the task.
	 * @param startDate The new start date. Only monthDay, month, and year
	 * components will be used.
	 * @param maintainDuration If this is true, the end date will be shifted
	 * to maintain the task's previous duration. If this is false and setting
	 * start's date to startDate results in a date that is after end,
	 * end will be updated to be the same as start.
	 * @throws IllegalArgumentException if startDate is null
	 */
	public void setStartDate(Time startDate, boolean maintainDuration) {
		if (startDate == null)
			throw new IllegalArgumentException("Start date must not be null.");
		setStartDate(startDate.monthDay, startDate.month, startDate.year,
				maintainDuration);
	}
	
	/** 
	 * Set the new end time/date for the task.
	 * @param end The new end time/date. Seconds will be ignored. If it is
	 * before start, start will be updated to be the same as end.
	 * @throws IllegalArgumentException if end is null
	 */
	public void setEnd(Time end) {
		if (end == null)
			throw new IllegalArgumentException("End must not be null.");
		
		//TODO work times
		this.end = new Time(end);
		this.end.second = 0;
		if (this.end.before(this.start))
			this.start = new Time(this.end);
	}
	
	/**
	 * Sets the new end time for the task.
	 * @param minute new minute, 0-59
	 * @param hour new hour, 0-23
	 * @param incDayIfEndBeforeStart If this is true and the resulting end time
	 * is before start, increment the new end time's day. If this is false and
	 * the resulting end time is before start, start will be updated to be the
	 * same as end.
	 * @throws IllegalArgumentException if hour or minute is invalid
	 */
	public void setEndTime(int minute, int hour, 
			boolean incDayIfEndBeforeStart) {
		if (minute < 0 || minute > 59)
			throw new IllegalArgumentException("minute must be 0-59");
		if (hour < 0 || hour > 23)
			throw new IllegalArgumentException("hour must be 0-23");
		
		//TODO work times, DST?
		updateTime(end, minute, hour);
		if (end.before(start)) {
			if (incDayIfEndBeforeStart) {
				end.monthDay += 1;
				end.normalize(true);
			} else {
				start = new Time(end);
			}
		}
	}
	/**
	 * Sets the new end time (but not date) for the task.
	 * @param endTime The new end time. Only hours and minutes will be used.
	 * @param incDayIfEndBeforeStart If this is true and the resulting end time
	 * is before start, increment the new end time's day. If this is false and
	 * the resulting end time is before start, start will be updated to be the
	 * same as end.
	 * @throws IllegalArgumentException if endTime is null
	 */
	public void setEndTime(Time endTime, boolean incDayIfEndBeforeStart) {
		if (endTime == null)
			throw new IllegalArgumentException("End time cannot be null.");
		setEndTime(endTime.minute, endTime.hour, incDayIfEndBeforeStart);
	}
	
	/**
	 * Sets the new end date for the task. Performs normalization so that
	 * invalid month/day combinations will be fixed. If the resulting end 
	 * time/date is before start, start will be updated to be the same as end.
	 * @param monthDay new day of month, 1-31
	 * @param month new month of year, 0-11
	 * @param year new year, 1970-2036
	 * @throws IllegalArgumentException if monthDay, month, or year is invalid
	 * (note that this does not check validity of day/month combinations)
	 */
	public void setEndDate(int monthDay, int month, int year) {
		if (monthDay < 1 || monthDay > 31)
			throw new IllegalArgumentException("day of month must be 1-31");
		if (month < 0 || month > 11)
			throw new IllegalArgumentException("month must be 0-11");
		if (year < 1970 || year > 2036)
			throw new IllegalArgumentException("year must be 1970-2036");
		
		//TODO work times
		updateDate(end, monthDay, month, year);
		if (end.before(start))
			start = new Time(end);
	}
	
	/**
	 * Set the new end date (but not time) for the task.
	 * @param endDate The new end date. Only monthDay, month, and year will 
	 * be used. If the resulting end time/date is before start, start will be 
	 * updated to be the same as end.
	 * @throws IllegalArgumentException if endDate is null
	 */
	public void setEndDate(Time endDate) {
		if (endDate == null)
			throw new IllegalArgumentException("End date cannot be null.");
		setEndDate(endDate.monthDay, endDate.month, endDate.year);
	}
	
	/**
	 * Shifts the times of all the task, including the start, end, and all
	 * the intervals in workIntervals.
	 * @param shiftBy The time by which to shift, in milliseconds.
	 */
	public void shiftTimes (long shiftBy)
	{
		//If shiftBy is 0, returns without doing anything
		if (shiftBy == 0)
			return;
		
		start.set(start.toMillis(true) + shiftBy);
		end.set(end.toMillis(true) + shiftBy);
		//TODO figure out if there are any DST-related bugs with this,
		// or if the origin time zone ever needs to be updated...
	}
	
	private void updateTime(Time time, int minute, int hour) {
		if (time == null)
			throw new IllegalArgumentException("Time cannot be null.");
		time.hour = hour;
		time.minute = minute;
		time.second = 0;
		time.normalize(true); //TODO DST? time zone?
	}
	
	private void updateDate(Time time, int monthDay, int month, int year) {
		if (time == null)
			throw new IllegalArgumentException("time cannot be null.");
		time.monthDay = monthDay;
		time.month = month;
		time.year = year;
		time.normalize(true); //TODO DST? time zone?
	}
	
	/**
	 * Returns the name of the task.
	 */
	public String toString ()
	{
		return name;
	}
}
