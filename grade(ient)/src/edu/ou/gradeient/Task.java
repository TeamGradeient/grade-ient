package edu.ou.gradeient;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableInterval;

public class Task 
{
	/**A String containing the name of this task*/
	private String name;
	
	/**A reference to the subject with which this task is associated.*/
	private Subject subject;
	
	/**A string containing notes about this task*/
	private String notes;
	
	/**True if the task is done, false otherwise.*/
	private boolean isDone;
	
	/**The time zone in which this task was created*/
	private DateTimeZone originTimeZone;
	
	private MutableInterval taskInterval;
	
	private ArrayList<MutableInterval> workIntervals;
	
	/**Comparator to compare two tasks by their due dates*/
	public final CompareTasksByDate BY_DUE_DATE = new CompareTasksByDate();
	
	/**No-argument constructor. Creates a new task with default values for all fields.*/
	public Task ()
	{
		isDone = false;
		workIntervals = new ArrayList<MutableInterval>();
	}
	
	/**
	 * Creates a default task with the name given.
	 * @param name The name of the task
	 */
	public Task (String name)
	{
		this.name = name;
		isDone = false;
		notes = null;
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
	
	public MutableInterval getTaskInterval ()
	{
		return taskInterval;
	}
	
	/**
	 * Returns the subject of this task
	 * @return A reference to the subject of this task, or null if no subject is associated with the task
	 */
	public Subject getSubject ()
	{
		return subject;
	}
	
	/**
	 * Returns true if this task is done, and false otherwise
	 * @return True if this task is done, and false otherwise
	 */
	public boolean getIsDone ()
	{
		return isDone;
	}
	
	/**
	 * Adds an interval to the list of work times.
	 * @param interval The interval to be added
	 */
	public void addWorkInterval (MutableInterval interval)
	{
		if (interval == null)
		{
			throw new IllegalArgumentException ("Interval cannot be null");
		}
		workIntervals.add(interval);
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
	 * @throws IllegalArgumentException if subject is null
	 */
	public void setSubject (Subject newSubject)
	{
		if (newSubject == null)
		{
			throw new IllegalArgumentException("Subject cannot be null");
		}
		subject = newSubject;
	}
	
	public void setIsDone (boolean newIsDone)
	{
		isDone = newIsDone;
	}
	
	/**
	 * Replaces an interval with a new one of the same size, shifted the
	 * amount of time specified. 
	 * @param shiftBy The amount of time by which to shift, in milliseconds. 
	 * A positive time shifts the interval to a later time than the original. 
	 * @param interval The interval to be shifted
	 * @return The shifted interval
	 */
	private MutableInterval shiftTimeOfInterval (long shiftBy, MutableInterval interval)
	{
		if (shiftBy == 0)
		{
			return interval;
		}
		return new MutableInterval(interval.getStartMillis()+shiftBy, interval.getEndMillis()+shiftBy);
	}
	
	/**
	 * Shifts the times of all the intervals in workIntervals
	 * @param shiftBy The time by which to shift, in milliseconds.
	 */
	public void shiftTimes (long shiftBy)
	{
		//If shiftBy is 0, returns without doing anything
		if (shiftBy == 0)
		{
			return;
		}
		
		//Otherwise, steps through the array of workIntervals and
		//shifts each one. 
		for (int i = 0; i < workIntervals.size(); ++i)
		{
			shiftTimeOfInterval (shiftBy, workIntervals.get(i));
		}
	}
	
}
