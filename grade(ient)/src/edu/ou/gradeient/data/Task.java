package edu.ou.gradeient.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.MutableInterval;
import org.joda.time.ReadableInterval;

import edu.ou.gradeient.TimeUtils;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Represents a Task.
 * TODO ADD EVERYTHING ABOUT WORK TIMES. EEEEEEEVERYTHIIIIIIIIIIIIIING.
 * TODO work time support, correct time zone support,
 * check if it's efficient enough
 */
public class Task implements Comparable<Task>, Serializable
{
	/** Schema for the Task table in the database and ContentProvider */
	public static final class Schema implements BaseColumns {
		/** 
		 * URI for the Task table. Valid query URIs: <ul>
		 * <li><code>CONTENT_URI</code>: returns all tasks (valid for query
		 * and insert)
		 * <li><code>CONTENT_URI/#</code>: returns task with ID #
		 * (valid for query, update, delete)
		 * <li><code>CONTENT_URI/#/##</code>: returns tasks that overlap
		 * interval # to ## (with numbers in milliseconds since epoch)
		 * (valid for query)
		 */
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
		
		public static final String SORT_ORDER_DEFAULT = END_INSTANT + " ASC";
		
		/** ID (long not null) */
		public static final int COL_ID = 0;
		/** Subject name (string) */
		public static final int COL_SUBJECT_NAME = 1;
		/** Name (string not null) */
		public static final int COL_NAME = 2;
		/** Is done (int 0/1 not null) */
		public static final int COL_IS_DONE = 3;
		/** Start instant in millis since epoch (long >= 0 not null) */
		public static final int COL_START_INSTANT = 4;
		/** End instant in millis since epoch (long >= 0 not null) */
		public static final int COL_END_INSTANT = 5;
		/** Notes (string) */
		public static final int COL_NOTES = 6;
		
		/** Gets a URI for the given task ID */
		public static Uri getUriForTask(long taskId) {
			return ContentUris.withAppendedId(CONTENT_URI, taskId);
		}

		/** Get a URI for the given date range (in milliseconds since epoch) */
		public static Uri getUriForRange(long start, long end) {
			return Uri.withAppendedPath(CONTENT_URI, start + "/" + end);
		}
	}
	
	private static final long serialVersionUID = -2567385792745859337L;

	/** Generic new task ID */
	public static final long NEW_TASK_ID = -1;
	
	/** Unique ID of the task. */
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
	private MutableInterval taskInterval;
	
	/** Work times for the task */
	private TreeSet<TaskWorkInterval> workIntervals = 
			new TreeSet<TaskWorkInterval>();
	
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
	 * Gets the end time/date of this task in milliseconds since the 
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
	 * Gets an unmodifiable view of the task's work intervals 
	 */
	public Collection<TaskWorkInterval> getWorkIntervals() {
		return Collections.unmodifiableSortedSet(workIntervals);
	}
	
	/**
	 * Sets the task's work intervals from the given cursor. The cursor
	 * must be from the Task_Work_Interval table in the database and include
	 * columns as defined by {@link TaskWorkInterval.Schema.COLUMNS}.
	 * It should be at one position before the desired start position.
	 * @param cursor The cursor to read the work intervals from
	 * @param replace If true, clear the work intervals before adding the
	 * ones from the cursor
	 * @throws IllegalArgumentException if a value found in the database
	 * was illegal, the cursor was null, the work interval's task ID did not
	 * match this task's ID, or the work interval was outside the task interval.
	 * @throws NumberFormatException if a value that was supposed to be a 
	 * number was not actually a number
	 */
	public void setWorkIntervals(Cursor cursor, boolean replace) {
		if (cursor == null)
			throw new IllegalArgumentException("cursor must not be null");
		if (replace)
			workIntervals.clear();
		while (cursor.moveToNext()) 
			addWorkInterval(new TaskWorkInterval(cursor));
	}
	
	/**
	 * Adds a work interval to this task.
	 * @param interval The interval to add
	 * @throws IllegalArgumentException if the work interval's task ID did not
	 * match this task's ID or the work interval was outside the task interval.
	 */
	public void addWorkInterval(TaskWorkInterval interval) {
		if (interval.getTaskId() != id)
			throw new IllegalArgumentException("Work interval found for "
					+ "task with different ID");
		if (!taskInterval.contains(interval.getInterval()))
			throw new IllegalArgumentException("Work interval out of task "
					+ "start/end interval");
		workIntervals.add(interval);
	}
	
	/**
	 * Sets the start date/time for the task. 
	 * @param start The new start date/time.
	 * @param maintainDuration If this is true, the end date/time will be
	 * shifted to maintain the task's previous duration, and work intervals
	 * will be shifted. If this is false and start is after end, end will be 
	 * updated to be the same as start (and all work times will be wiped out).
	 * @throws IllegalArgumentException if start is < 0
	 */
	public void setStart (long start, boolean maintainDuration) {
		long oldStart = getStartMillis();
		TimeUtils.setStart(taskInterval, start, maintainDuration);
		fixWorkIntervalsStart(oldStart, maintainDuration);
	}
	
	/**
	 * Sets the new start time for the task.
	 * @param hour new hour of day, 0-23
	 * @param minute new minute of hour, 0-59
	 * @param maintainDuration If this is true, the end time will be shifted
	 * to maintain the task's previous duration, and work intervals
	 * will be shifted. If this is false and new start is after end, end will be 
	 * updated to be the same as start (and all work times will be wiped out).
	 * @throws IllegalArgumentException if minute or hour is invalid
	 */
	public void setStartTime(int hour, int minute, boolean maintainDuration) {
		long oldStart = getStartMillis();
		TimeUtils.setStartTime(taskInterval, hour, minute, maintainDuration);
		fixWorkIntervalsStart(oldStart, maintainDuration);
	}
	
	/**
	 * Sets the new start date for the task.
	 * @param year new year, 1970-2036
	 * @param month new month of year, 0-11
	 * @param day new day of month, 1-31
	 * @param maintainDuration If this is true, the end date will be shifted
	 * to maintain the task's previous duration, and work intervals will be
	 * shifted. If this is false and setting start's date to the given values 
	 * results in a date that is after end, end will be updated to be the same 
	 * as start (and all work times will be wiped out).
	 * @throws IllegalArgumentException if day, month, or year is invalid
	 */
	public void setStartDate(int year, int month, int day,
			boolean maintainDuration) {
		long oldStart = getStartMillis();
		TimeUtils.setStartDate(taskInterval, year, month, day, maintainDuration);
		fixWorkIntervalsStart(oldStart, maintainDuration);
	}
	
	/** 
	 * Set the new end time/date for the task.
	 * @param end The new end time/date. If it is before start, start will be 
	 * updated to be the same as end (and all work intervals will be deleted).
	 * @throws IllegalArgumentException if end < 0
	 */
	public void setEnd (long end) {
		TimeUtils.setEnd(taskInterval, end);
		fixWorkIntervals();
	}
	
	/**
	 * Sets the new end time for the task.
	 * @param hour new hour, 0-23
	 * @param minute new minute, 0-59
	 * @param incDayIfEndBeforeStart If this is true and the resulting end time
	 * is before start, increment the new end time's day. If this is false and
	 * the resulting end time is before start, start will be updated to be the
	 * same as end (and all work intervals will be deleted).
	 * @throws IllegalArgumentException if hour or minute is invalid
	 */
	public void setEndTime(int hour, int minute, 
			boolean incDayIfEndBeforeStart) {
		TimeUtils.setEndTime(taskInterval, hour, minute, incDayIfEndBeforeStart);
		fixWorkIntervals(); //TODO anything else for this one?
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
		TimeUtils.setEndDate(taskInterval, year, month, day);
		fixWorkIntervals();
	}
	
	/**
	 * Set the new start and end date/time for the task.
	 * @param start The new start time
	 * @param end The new end time
	 * @throws IllegalArgumentException if start > end
	 */
	public void setStartAndEnd(long start, long end) {
		TimeUtils.setStartAndEnd(taskInterval, start, end);
	}
	
	/**
	 * Shifts all the times of the task.
	 * @param shiftBy The time by which to shift, in milliseconds.
	 */
	public void shiftTimes (long shiftBy) {
		//If shiftBy is 0, returns without doing anything
		if (shiftBy == 0)
			return;
		
		// Shift the start and end
		TimeUtils.shiftTimeOfInterval(taskInterval, shiftBy);
		// Shift the work times
		for (TaskWorkInterval interval : workIntervals)
			interval.shiftTime(shiftBy);
	}
	
	/**
	 * If maintainDuration is true, shifts all work intervals.
	 * If false, calls fixWorkIntervals.
	 */
	private void fixWorkIntervalsStart(long oldStart, boolean maintainDuration) {
		if (maintainDuration) {
			shiftTimes(getStartMillis() - oldStart);
		} else {
			fixWorkIntervals();
		}
	}
	
	/**
	 * Ensures the work intervals are valid for the task interval.
	 * If the task duration is 0, deletes all work intervals.
	 * Deletes any work intervals that are entirely outside the task interval.
	 * Updates the start/end of intervals overlapping the task start/end.
	 */
	private void fixWorkIntervals() {
		if (taskInterval.toDurationMillis() == 0) {
			workIntervals.clear();
			return;
		}
		ArrayList<TaskWorkInterval> toRemove = new ArrayList<TaskWorkInterval>();
		for (TaskWorkInterval twi : workIntervals) {
			ReadableInterval interval = twi.getInterval();
			// only do anything if the task interval does not fully 
			// contain this interval
			if (!taskInterval.contains(interval)) {
				// if it overlaps at all, update the start or end
				if (taskInterval.overlaps(interval)) {
					if (taskInterval.contains(interval.getStartMillis())) {
						// contains the start; update the end
						twi.setEnd(getEndMillis());
					} else {
						// contains the end; update the start
						twi.setStart(getStartMillis(), false);
					}
				} else {
					// interval is no longer within task interval
					toRemove.add(twi);
				}
			}
		}
		workIntervals.removeAll(toRemove);
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
	
	/** Compares tasks by due date */
	@Override
	public int compareTo(Task another) {
		long ldue = getEndMillis();
		long rdue = another.getEndMillis();
		return ldue < rdue ? -1 : (ldue == rdue ? 0 : 1);
	}
}
