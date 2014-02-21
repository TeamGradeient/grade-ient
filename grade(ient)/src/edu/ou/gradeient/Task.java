package edu.ou.gradeient;

import org.joda.time.DateTimeZone;
import org.joda.time.MutableInterval;
import org.joda.time.ReadableInterval;

public class Task 
{
	/**A String containing the name of this task*/
	private String name;
	
	/**The name of the subject with which this task is associated.*/
	private String subject;
	
	/**A string containing notes about this task*/
	private String notes;
	
	/**True if the task is done, false otherwise.*/
	private boolean isDone;
	
	/**The time zone in which this task was created*/
	private DateTimeZone originTimeZone;
	
	/**The interval for the task*/
	private MutableInterval taskInterval;
	
	/**Comparator to compare two tasks by their due dates*/
	public final CompareTasksByDate BY_DUE_DATE = new CompareTasksByDate();

	/**
	 * Creates a default task with the name given.
	 * @param name The name of the task
	 * @throws IllegalArgumentException if name is null
	 */
	public Task (String name, long start, long end)
	{
		if (name == null)
			throw new IllegalArgumentException("Name cannot be null.");
		
		this.name = name;
		isDone = false;
		// Set the task interval, taking advantage of the error checking in 
		// the setter methods.
		taskInterval = new MutableInterval();
		setEnd(end);
		setStart(start);
		originTimeZone = DateTimeZone.getDefault();
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
	 * Sets the subject of this task
	 * @param newSubject The subject to be set
	 */
	public void setSubject (String newSubject)
	{
		subject = newSubject;
	}
	
	public void setIsDone (boolean newIsDone)
	{
		isDone = newIsDone;
	}
	
	/**
	 * Sets the start instant of the task
	 * @param start The new start instant in milliseconds
	 * @throws IllegalArgumentException if start is < 0 or >= end
	 */
	public void setStart (long start) 
	{
		if (start < 0) {
			throw new IllegalArgumentException("Start must not be negative.");
		}
		if (taskInterval.getEndMillis() <= start) {
			throw new IllegalArgumentException("Start must be before end.");
		}
		taskInterval.setStartMillis(start);
	}
	
	/**
	 * Sets the end instant of the task
	 * @param end The new end instant in milliseconds
	 * @throws IllegalArgumentException if end is < 0 or <= start
	 */
	public void setEnd (long end)
	{
		if (end < 0) {
			throw new IllegalArgumentException("End must not be negative.");
		}
		if (taskInterval.getStartMillis() >= end) {
			throw new IllegalArgumentException("End must be after start.");
		}
		taskInterval.setEndMillis(end);
	}
	
	/**
	 * Sets the start/end interval for this task.
	 * @param interval the new interval for this task
	 * @throws IllegalArgumentException if interval is null
	 */
	public void setTaskInterval (MutableInterval interval) {
		if (interval == null) {
			throw new IllegalArgumentException("Task interval cannot be null.");
		}
		taskInterval = interval;
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
