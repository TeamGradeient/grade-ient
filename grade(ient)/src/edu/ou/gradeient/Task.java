package edu.ou.gradeient;

import java.io.Serializable;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableInterval;
import org.joda.time.ReadableInterval;

/**
 * Represents a Task.
 * TODO work time support, verification of correct time zone support,
 * check if it's efficient enough
 * 
 * TODO CHANGE CALLING ORDER IN EDITTASKACTIVITY
 */
public class Task implements Serializable
{
	private static final long serialVersionUID = 5526917550297108374L;

	/** Generic new task ID */
	public static final long NEW_TASK_ID = -1;
	
	private static Random random = new Random();
	
	/** Unique ID of the task.
	 * TODO Longs are relatively short enough that it's apparently a bad idea
	 * to assume that there will not be collisions between randomly generated
	 * long IDs. Once database support is implemented, the Task implementation
	 * should set new tasks' ID to -1, and ID should be a primary key 
	 * autoincrement field, so the database will manage the ID sequence and
	 * keep it unique.
	 */
	private long id;
	
	/**A String containing the name of this task*/
	private String name;
	
	/**The name of the subject with which this task is associated.*/
	private String subject;
	
	/**A string containing notes about this task*/
	private String notes;
	
	/**True if the task is done, false otherwise.*/
	private boolean isDone;
	
	/**The interval for the task*/
	private final MutableInterval taskInterval;
	
	/**Comparator to compare two tasks by their due dates*/
	public static final CompareTasksByDate BY_DUE_DATE = new CompareTasksByDate();

	/**
	 * Creates a default task with the name given.
	 * @param name The name of the task. Must not be null.
	 * @throws IllegalArgumentException if name is null or if start or end
	 * is invalid
	 */
	public Task (String name, long start, long end)
	{
		if (name == null)
			throw new IllegalArgumentException("Name cannot be null.");
		
		this.name = name;
		isDone = false;
		// Get rid of seconds and milliseconds
		start -= start % 60000;
		end -= end % 60000;
		taskInterval = new MutableInterval(start, end);
		id = random.nextLong();
		if (id < 0) id = -id;
	}
	
	/**
	 * Copy constructor
	 * @param other Task to copy
	 */
	public Task (Task other) {
		id = other.id;
		name = other.name;
		subject = other.subject;
		notes = other.notes;
		isDone = other.isDone;
		taskInterval = new MutableInterval(other.taskInterval);
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
	 * Returns the start and end interval for this task
	 * @return The start and end interval for this task
	 */
	public ReadableInterval getTaskInterval ()
	{
		return taskInterval;
	}
	
	public DateTime getStart() {
		return taskInterval.getStart();
	}

	public DateTime getEnd() {
		return taskInterval.getEnd();
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
	 * @param start The new start date/time.
	 * @param maintainDuration If this is true, the end date/time will be
	 * shifted to maintain the task's previous duration. If this is false and
	 * start is after end, end will be updated to be the same as start.
	 * @throws IllegalArgumentException if start is < 0
	 */
	public void setStart (long start, boolean maintainDuration) {
		if (maintainDuration) {
			setStartAndEnd(start, start + taskInterval.toDurationMillis());
		} else {
			if (taskInterval.isBefore(start))
				setStartAndEnd(start, start);
			else
				taskInterval.setStartMillis(start);
		}
	}
	
	/**
	 * Sets the new start time for the task.
	 * @param hour new hour of day, 0-23
	 * @param minute new minute of hour, 0-59
	 * @param maintainDuration If this is true, the end time will be shifted
	 * to maintain the task's previous duration. If this is false and setting
	 * start's time to hour and minute results in a time that is after end,
	 * end will be updated to be the same as start.
	 * @throws IllegalArgumentException if minute or hour is invalid
	 */
	public void setStartTime(int hour, int minute, boolean maintainDuration) {
		setStart(taskInterval.getStart().withTime(hour, minute, 0, 0)
				.getMillis(), maintainDuration);
	}
	
	/**
	 * Sets the new start date for the task.
	 * @param year new year, 1970-2036
	 * @param month new month of year, 0-11
	 * @param day new day of month, 1-31
	 * @param maintainDuration If this is true, the end date will be shifted
	 * to maintain the task's previous duration. If this is false and setting
	 * start's date to the given values results in a date that is after end,
	 * end will be updated to be the same as start.
	 * @throws IllegalArgumentException if day, month, or year is invalid
	 */
	public void setStartDate(int year, int month, int day,
			boolean maintainDuration) {
		if (year < 1970 || year > 2036)
			throw new IllegalArgumentException("year must be 1970-2036");
		
		setStart(taskInterval.getStart().withDate(year, month, day)
				.getMillis(), maintainDuration);
	}
	
	/** 
	 * Set the new end time/date for the task.
	 * @param end The new end time/date. If it is before start, start will be 
	 * updated to be the same as end.
	 * @throws IllegalArgumentException if end < 0
	 */
	public void setEnd (long end) {
		if (taskInterval.isAfter(end))
			setStartAndEnd(end, end);
		else
			taskInterval.setEndMillis(end);
	}
	
	/**
	 * Sets the new end time for the task.
	 * @param hour new hour, 0-23
	 * @param minute new minute, 0-59
	 * @param incDayIfEndBeforeStart If this is true and the resulting end time
	 * is before start, increment the new end time's day. If this is false and
	 * the resulting end time is before start, start will be updated to be the
	 * same as end.
	 * @throws IllegalArgumentException if hour or minute is invalid
	 */
	public void setEndTime(int hour, int minute, 
			boolean incDayIfEndBeforeStart) {
		long end = taskInterval.getEnd().withTime(hour, minute, 0, 0)
				.getMillis();
		if (incDayIfEndBeforeStart && taskInterval.isAfter(end))
			setStartAndEnd(end, end);
		else
			setEnd(end);
	}
	
	/**
	 * Sets the new end date for the task. If the resulting end time/date is 
	 * before start, start will be updated to be the same as end.
	 * @param year new year, 1970-2036
	 * @param month new month of year, 0-11
	 * @param day new day of month, 1-31
	 * @throws IllegalArgumentException if day, month, or year is invalid
	 */
	public void setEndDate(int year, int month, int day) {
		if (year < 1970 || year > 2036)
			throw new IllegalArgumentException("year must be 1970-2036");

		setEnd(taskInterval.getEnd().withDate(year, month, day).getMillis());
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
		if (taskInterval.contains(start) || taskInterval.isAfter(start)) {
			taskInterval.setStartMillis(start);
			taskInterval.setEndMillis(end);
		} else {
			taskInterval.setEndMillis(end);
			taskInterval.setStartMillis(start);
		}
	}
	
	/**
	 * Shifts all the times of the task.
	 * @param shiftBy The time by which to shift, in milliseconds.
	 */
	public void shiftTimes (long shiftBy)
	{
		//If shiftBy is 0, returns without doing anything
		if (shiftBy == 0)
		{
			return;
		}
		
		// Shift the start and end
		shiftTimeOfInterval(shiftBy, taskInterval);
	}
	
	/**
	 * Returns the name of the task.
	 */
	public String toString ()
	{
		return name;
	}
	
	/**
	 * Shifts the interval by the amount of time specified. 
	 * @param shiftBy The amount of time by which to shift, in milliseconds. 
	 * A positive time shifts the interval to a later time than the original. 
	 * @param interval The interval to be shifted
	 */
	private static void shiftTimeOfInterval (long shiftBy, 
			MutableInterval interval)
	{
		interval.setStartMillis(interval.getStartMillis() + shiftBy);
		interval.setEndMillis(interval.getEndMillis() + shiftBy);
	}
}
