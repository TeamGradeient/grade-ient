package edu.ou.gradeient;

import java.io.Serializable;
import java.util.Comparator;

import org.joda.time.DateTime;
import org.joda.time.MutableInterval;
import org.joda.time.ReadableInterval;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Represents a Task.
 * TODO work time support, correct time zone support,
 * check if it's efficient enough
 */
public class Task implements Serializable
{
	/** Schema for the Task table in the database and ContentProvider */
	public static final class Schema implements BaseColumns {
		/** URI for the Task table */
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				TaskProvider.CONTENT_URI, "tasks");
		/** MIME type for list of Tasks */
		public static final String CONTENT_TYPE = 
				ContentResolver.CURSOR_DIR_BASE_TYPE + 
				"/vnd." + TaskProvider.AUTHORITY + "_tasks";
		/** MIME type for single Task */
		public static final String CONTENT_ITEM_TYPE = 
				ContentResolver.CURSOR_ITEM_BASE_TYPE + 
				"/vnd." + TaskProvider.AUTHORITY + "_tasks";

		/* default */ static final String TABLE = "Task";
		public static final String SUBJECT_NAME = "subject_name";
		public static final String NAME = "name";
		public static final String IS_DONE = "is_done";
		public static final String START_INSTANT = "start_instant";
		public static final String END_INSTANT = "end_instant";
		public static final String NOTES = "notes";

		public static final String[] COLUMNS = { _ID, SUBJECT_NAME, NAME,
			IS_DONE, START_INSTANT, END_INSTANT, NOTES };
		
		//TODO subject object? work times?
		
		public static final String SORT_ORDER_DEFAULT = END_INSTANT + " ASC";
		
		/** ID (long not null) */
		public static final int COL_ID = 0;
		/** Subject name (string) */
		public static final int COL_SUBJECT_NAME = 1;
		/** Name (string not null) */
		public static final int COL_NAME = 2;
		/** Is done (int 0/1 not null) */
		public static final int COL_IS_DONE = 3;
		/** Start instant (long >= 0 not null) */
		public static final int COL_START_INSTANT = 4;
		/** End instant (long >= 0 not null) */
		public static final int COL_END_INSTANT = 5;
		/** Notes (string) */
		public static final int COL_NOTES = 6;
		
		/** Get a URI for the given date range (in milliseconds since epoch) */
		public static Uri getUriForRange(long start, long end) {
			return Uri.withAppendedPath(CONTENT_URI, start + "/" + end);
		}
	}
	
	private static final long serialVersionUID = -2567385792745859337L;

	/** Generic new task ID */
	public static final long NEW_TASK_ID = -1;
	
	/** Unique ID of the task. */
	private final long id;
	
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
	public static final Comparator<Task> BY_DUE_DATE = new Comparator<Task>() {
		@Override
		public int compare(Task lhs, Task rhs) {
			long ldue = lhs.getEndMillis();
			long rdue = rhs.getEndMillis();
			return ldue < rdue ? -1 : (ldue == rdue ? 0 : 1);
		}
	};

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
		subject = "";
		notes = "";
		isDone = false;
		taskInterval = new MutableInterval(start, end);
		id = NEW_TASK_ID;
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
	 * Creates a Task from a cursor. Assumes the cursor is already pointing
	 * to the correct row.
	 * TODO is this even a correct place to put this?
	 * @param cursor A cursor for the Task table in the database
	 * @throws IllegalArgumentException if a value found in the database
	 * was illegal or the cursor was null
	 * @throws NumberFormatException if a value that was supposed to be a 
	 * number was not actually a number
	 */
	public Task (Cursor cursor) {
		if (cursor == null)
			throw new IllegalArgumentException("cursor must not be null");
		
		// Get all the values as strings and parse them here, so that we can
		// do better error checking than is provided by Cursor.
		// (The implementation of getLong, getInt, etc. is in CursorWindow,
		// which calls native methods to parse the values and does unhelpful
		// things like returning 0 on error.)
		id = Long.parseLong(cursor.getString(Schema.COL_ID));
		setName(cursor.getString(Schema.COL_NAME));
		setSubject(cursor.getString(Schema.COL_SUBJECT_NAME));
		setNotes(cursor.getString(Schema.COL_NOTES));
		
		// Options for is done are 0 or 1. In the odd case that something else
		// is stored, default to false.
		String doneStr = cursor.getString(Schema.COL_IS_DONE);
		isDone = (doneStr == null ? false : doneStr.equals("1"));
		
		long start = Long.parseLong(cursor.getString(Schema.COL_START_INSTANT));
		long end = Long.parseLong(cursor.getString(Schema.COL_END_INSTANT));
		taskInterval = new MutableInterval(start, end);
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
	 * Gets the start time/date of this task.
	 */
	public DateTime getStart() {
		return taskInterval.getStart();
	}
	
	/**
	 * Gets the start time/date of this task in milliseconds since the 
	 * Unix epoch.
	 */
	public long getStartMillis() {
		return taskInterval.getStartMillis();
	}

	/**
	 * Gets the end time/date of this task.
	 */
	public DateTime getEnd() {
		return taskInterval.getEnd();
	}
	
	/**
	 * Gets the start time/date of this task in milliseconds since the 
	 * Unix epoch.
	 */
	public long getEndMillis() {
		return taskInterval.getEndMillis();
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
	 * Returns whether this task is done.
	 * @return True if this task is done, and false otherwise
	 */
	public boolean isDone ()
	{
		return isDone;
	}
	
	/**
	 * Gets the ID of this task.
	 */
	public long getId() {
		return id;
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
	 * @param month new month of year, 1-12
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
	 * Put the contents of this task (except ID) in a ContentValues object, 
	 * with keys corresponding to the column names of the database table.
	 * (The ID is not returned because it is an autoincrement primary key.
	 * Trying to insert a value for it when adding a new task causes errors,
	 * and it can't be updated from what the database sets it to.)
	 * @return The ContentValues object with the task
	 */
	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(Schema.SUBJECT_NAME, subject);
		cv.put(Schema.NAME, name);
		cv.put(Schema.IS_DONE, isDone ? 1 : 0);
		cv.put(Schema.START_INSTANT, taskInterval.getStartMillis());
		cv.put(Schema.END_INSTANT, taskInterval.getEndMillis());
		cv.put(Schema.NOTES, notes);
		return cv;
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
