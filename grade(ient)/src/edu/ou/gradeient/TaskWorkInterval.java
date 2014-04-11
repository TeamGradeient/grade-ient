package edu.ou.gradeient;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.MutableInterval;
import org.joda.time.ReadableInterval;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Represents a work interval for a task.
 * This class does NO checking whether it's within the parent Task's interval.
 */
public class TaskWorkInterval implements Comparable<TaskWorkInterval>, 
		Serializable {

	/** Schema for the work interval table in the database and ContentProvider */
	public static final class Schema implements BaseColumns {
		/** 
		 * URI for the work interval table. Valid URIs: <ul>
		 * <li><code>CONTENT_URI</code>: valid for all operations
		 * <li><code>CONTENT_URI/#</code>: work time with ID # (valid only
		 * for update and delete)
		 * <li><code>CONTENT_URI/#/##</code>: work times in range # to ##
		 * in milliseconds since epoch (valid only for query)
		 * <li><code>CONTENT_URI/task/#</code>: work times for task ID #
		 * (valid for query, update, and delete)
		 */
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				Task.Schema.CONTENT_URI, "/task_work_intervals");
		/** MIME type for list of work times */
		public static final String CONTENT_TYPE = 
				ContentResolver.CURSOR_DIR_BASE_TYPE + 
				"/vnd." + TaskProvider.AUTHORITY + "_task_work_intervals";
		/** MIME type for single work time */
		public static final String CONTENT_ITEM_TYPE = 
				ContentResolver.CURSOR_ITEM_BASE_TYPE + 
				"/vnd." + TaskProvider.AUTHORITY + "_task_work_intervals";

		/* default */ static final String TABLE = "Task_Work_Interval";
		public static final String TASK_ID = "task_id";
		public static final String START_INSTANT = Task.Schema.START_INSTANT;
		public static final String END_INSTANT = Task.Schema.END_INSTANT;
		public static final String CERTAINTY = "certainty";

		public static final String[] COLUMNS = { _ID, TASK_ID, START_INSTANT, 
			END_INSTANT, CERTAINTY };
		
		/** Work intervals compare by start instant ascending */
		public static final String SORT_ORDER_DEFAULT = START_INSTANT + " ASC";
		
		/** ID (long not null) */
		public static final int COL_ID = 0;
		/** Task ID (long not null references task _ID) */
		public static final int COL_TASK_ID = 1;
		/** Start instant in millis since epoch (long >= 0 not null) */
		public static final int COL_START_INSTANT = 2;
		/** End instant in millis since epoch (long >= 0 not null) */
		public static final int COL_END_INSTANT = 3;
		/** Certainty (not null, 0 = maybe, 1 = definitely) */
		public static final int COL_CERTAINTY = 4;
		
		/** Gets the URI for the work time with the given ID (only valid for
		 * use with update and delete) */
		public static Uri getUriForId(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}
		/** Gets the URI for the work times for the given task ID 
		 * (only valid for use with query) */
		public static Uri getUriForTaskId(long taskId) {
			return Uri.withAppendedPath(CONTENT_URI, "task/" + taskId);
		}
		/** Gets the URI for the work times overlapping the given date range
		 * (in milliseconds since epoch) */
		public static Uri getUriForRange(long start, long end) {
			return Uri.withAppendedPath(CONTENT_URI, start + "/" + end);
		}
	}
	
	private static final long serialVersionUID = 7389238194133816662L;

	/** Generic new work interval ID */
	public static final long NEW_ID = -1;
	
	/** Unique ID of the work interval */
	private final long id;
	/** Task ID this work interval is for */
	private final long taskId;
	/** Interval of this work interval */
	private MutableInterval interval;
	/** Certainty of work interval (false = maybe, true = definitely) */
	private boolean isCertain;
	
	/**
	 * Creates a new work interval with the given data.
	 * @throws IllegalArgumentException if start or end is invalid
	 */
	public TaskWorkInterval(long taskId, long start, long end, 
			boolean isCertain) {
		this.id = NEW_ID;
		this.taskId = taskId;
		this.isCertain = isCertain;
		this.interval = new MutableInterval(start, end);
	}
	
	/** Copy constructor */
	public TaskWorkInterval(TaskWorkInterval other) {
		id = other.id;
		taskId = other.taskId;
		interval = new MutableInterval(other.interval);
		isCertain = other.isCertain;
	}
	
	/**
	 * Creates a work interval from a cursor. 
	 * Assumes the cursor is already pointing to the correct row.
	 * @param cursor A cursor for the Task table in the database
	 * @throws IllegalArgumentException if a value found in the database
	 * was illegal or the cursor was null
	 * @throws NumberFormatException if a value that was supposed to be a 
	 * number was not actually a number
	 */
	public TaskWorkInterval(Cursor cursor) {
		if (cursor == null)
			throw new IllegalArgumentException("cursor must not be null");
		// see Task for reason for using getString not getLong
		id = Long.parseLong(cursor.getString(Schema.COL_ID));
		taskId = Long.parseLong(cursor.getString(Schema.COL_TASK_ID));
		interval = new MutableInterval(
				Long.parseLong(cursor.getString(Schema.COL_START_INSTANT)),
				Long.parseLong(cursor.getString(Schema.COL_END_INSTANT)));
		isCertain = Integer.parseInt(cursor.getString(Schema.COL_CERTAINTY)) == 1;
	}
	
	public long getId() {
		return id;
	}
	
	public long getTaskId() {
		return taskId;
	}
	
	public boolean isCertain() {
		return isCertain;
	}
	
	public void setCertain(boolean isCertain) {
		this.isCertain = isCertain;
	}
	
	public ReadableInterval getInterval() {
		return interval;
	}
	
	/**
	 * Gets the start time/date of this work interval.
	 */
	public DateTime getStart() {
		return interval.getStart();
	}
	
	/**
	 * Gets the start time/date of this work interval in milliseconds since the 
	 * Unix epoch.
	 */
	public long getStartMillis() {
		return interval.getStartMillis();
	}

	/**
	 * Gets the end time/date of this work interval.
	 */
	public DateTime getEnd() {
		return interval.getEnd();
	}
	
	/**
	 * Gets the end time/date of this work interval in milliseconds since the 
	 * Unix epoch.
	 */
	public long getEndMillis() {
		return interval.getEndMillis();
	}
	
	/**
	 * Sets the start date/time for the interval.
	 * @param start The new start date/time.
	 * @param maintainDuration If this is true, the end date/time will be
	 * shifted to maintain the task's previous duration. If this is false and
	 * start is after end, end will be updated to be the same as start.
	 * @throws IllegalArgumentException if start is < 0
	 */
	public void setStart (long start, boolean maintainDuration) {
		FunTimes.setStart(interval, start, maintainDuration);
	}
	
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
	public void setStartTime(int hour, int minute, boolean maintainDuration) {
		FunTimes.setStartTime(interval, hour, minute, maintainDuration);
	}
	
	/**
	 * Sets the new start date for the interval.
	 * @param year new year, 1970-2036
	 * @param month new month of year, 0-11
	 * @param day new day of month, 1-31
	 * @param maintainDuration If this is true, the end date will be shifted
	 * to maintain the interval's previous duration. If this is false and 
	 * setting start's date to the given values results in a date that is after
	 * end, end will be updated to be the same as start.
	 * @throws IllegalArgumentException if day, month, or year is invalid
	 */
	public void setStartDate(int year, int month, int day,
			boolean maintainDuration) {
		FunTimes.setStartDate(interval, year, month, day, maintainDuration);
	}
	
	/** 
	 * Set the new end time/date for the interval.
	 * @param end The new end time/date. If it is before start, start will be 
	 * updated to be the same as end.
	 * @throws IllegalArgumentException if end < 0
	 */
	public void setEnd (long end) {
		FunTimes.setEnd(interval, end);
	}
	
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
	public void setEndTime(int hour, int minute, 
			boolean incDayIfEndBeforeStart) {
		FunTimes.setEndTime(interval, hour, minute, incDayIfEndBeforeStart);
	}
	
	/**
	 * Sets the new end date for the interval. If the resulting end time/date is 
	 * before start, start will be updated to be the same as end.
	 * @param year new year, 1970-2036
	 * @param month new month of year, 1-12
	 * @param day new day of month, 1-31
	 * @throws IllegalArgumentException if day, month, or year is invalid
	 */
	public void setEndDate(int year, int month, int day) {
		FunTimes.setEndDate(interval, year, month, day);
	}
	
	/**
	 * Set the new start and end date/time for the interval.
	 * @param start The new start time
	 * @param end The new end time
	 * @throws IllegalArgumentException if start > end
	 */
	public void setStartAndEnd(long start, long end) {
		FunTimes.setStartAndEnd(interval, start, end);
	}

	/**
	 * Shifts the time of this interval by the specified number of milliseconds.
	 * @param shiftBy time in milliseconds
	 */
	public void shiftTime(long shiftBy) {
		FunTimes.shiftTimeOfInterval(interval, shiftBy);
	}
	
	/**
	 * Put this work interval's contents (except ID) in a ContentValues object, 
	 * with keys corresponding to the column names of the database table.
	 * (The ID is not returned because it is an autoincrement primary key
	 * and can't be set or updated.)
	 * @return The ContentValues object with the task
	 */
	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(Schema.TASK_ID, taskId);
		cv.put(Schema.START_INSTANT, getStartMillis());
		cv.put(Schema.END_INSTANT, getEndMillis());
		cv.put(Schema.CERTAINTY, isCertain ? 1 : 0);
		return cv;
	}
	
	/** Compares intervals by start date */
	@Override
	public int compareTo(TaskWorkInterval another) {
		long lstart = getStartMillis();
		long rstart = another.getStartMillis();
		return lstart < rstart ? -1 : (lstart == rstart ? 0 : 1);
	}
}
