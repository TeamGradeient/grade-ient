package edu.ou.gradeient.data;

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
import android.text.TextUtils;

/**
 * Represents a work interval for a task.
 * This class does NO checking whether it's within the parent Task's interval.
 */
public class TaskWorkInterval extends DateTimeInterval
		implements Comparable<TaskWorkInterval>, Serializable {

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
				Task.Schema.CONTENT_URI, "/work_intervals");
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
		
		
		/** URI for list of hybrid work times/tasks. The only valid form is
		 * <code>CONTENT_URI_HYBRID/#/##</code>, work times in range # to ##
		 * (in milliseconds since epoch). */
		public static final Uri CONTENT_URI_HYBRID = Uri.withAppendedPath(
				TaskProvider.CONTENT_URI, "work_intervals_tasks");
		/** MIME type for list of hybrid work times/tasks */
		public static final String CONTENT_TYPE_HYBRID = 
				ContentResolver.CURSOR_DIR_BASE_TYPE + 
				"/vnd." + TaskProvider.AUTHORITY + "_work_intervals_tasks";
		/** MIME type for single hybrid work time/task */
		public static final String CONTENT_ITEM_TYPE_HYBRID = 
				ContentResolver.CURSOR_ITEM_BASE_TYPE + 
				"/vnd." + TaskProvider.AUTHORITY + "_work_intervals_tasks";
		
		/** Get a URI for hybrid work time/tasks in the given date range 
		 * (in milliseconds since epoch) */
		public static Uri getUriForRangeHybrid(long start, long end) {
			return Uri.withAppendedPath(CONTENT_URI_HYBRID, start + "/" + end); 
		}
		
		/** Special join table for getting work intervals with task names
		 * and subjects ordered by start date */
		static final String TABLE_HYBRID = TABLE + " inner join " 
				+ Task.Schema.TABLE + " on " + TABLE + "." + TASK_ID + " = "
				+ Task.Schema.TABLE + "." + Task.Schema._ID;
		
		/** Columns for TABLE_HYBRID */
		public static final String[] COLUMNS_HYBRID = { 
			TABLE + "." + _ID, Task.Schema.TABLE + "." + Task.Schema.NAME,
			Task.Schema.TABLE + "." + Task.Schema.SUBJECT_NAME, 
			TABLE + "." + START_INSTANT, TABLE + "." + END_INSTANT, 
			TABLE + "." + CERTAINTY };
		
		/** Default sort order for TABLE_HYBRID */
		public static final String SORT_ORDER_DEFAULT_HYBRID = 
				TABLE + "." + SORT_ORDER_DEFAULT;
		
		//TODO is this even the right way to do this?
		private static final String PROJ_FMT = "^1.^2 as ^2";
		public static final String[] PROJ_ARGS_JOIN = {
			TextUtils.expandTemplate(PROJ_FMT, TABLE, _ID).toString(),
			TextUtils.expandTemplate(PROJ_FMT, Task.Schema.TABLE, 
					Task.Schema.NAME).toString(),
			TextUtils.expandTemplate(PROJ_FMT, Task.Schema.TABLE,
					Task.Schema.SUBJECT_NAME).toString(),
			TextUtils.expandTemplate(PROJ_FMT, TABLE, START_INSTANT).toString(),
			TextUtils.expandTemplate(PROJ_FMT, TABLE, END_INSTANT).toString(),
			TextUtils.expandTemplate(PROJ_FMT, TABLE, CERTAINTY).toString() };
	}
	
	private static final long serialVersionUID = 7389238194133816662L;

	/** Generic new work interval ID */
	public static final long NEW_ID = -1;
	
	/** Unique ID of the work interval */
	private long id;
	/** Task ID this work interval is for */
	private long taskId;
	/** Certainty of work interval (false = maybe, true = definitely) */
	private boolean isCertain;
	/** Name of the task this work interval is for (will be null except for
	 * special hybrid task/work interval objects) */
	private String taskName;
	/** Name of the subject this work interval is for (will be null except for
	 * special hybrid task/work interval objects) */
	private String subjectName;
	
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
	
	/** 
	 * Gets the name of the task this work interval is for, which will be null
	 * unless the work interval was created with {@link #hybridWorkInterval}.
	 */
	public String getTaskName() {
		return taskName;
	}
	
	/**
	 * Gets the name of the subject this work interval is for, which will be
	 * null unless the work interval was created with {@link #hybridWorkInterval}.
	 */
	public String getSubjectName() {
		return subjectName;
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
	
	/**
	 * Creates a TaskWorkInterval from a cursor to a URI returned by 
	 * {@link Schema#getUriForRangeHybrid(long, long)}. Assumes the cursor is 
	 * already pointing to the correct row.
	 * @param cursor A cursor for the task + work interval join in the database
	 * @throws IllegalArgumentException if a value found in the database
	 * was illegal or the cursor was null
	 * @throws NumberFormatException if a value that was supposed to be a 
	 * number was not actually a number
	 */
	public static TaskWorkInterval hybridWorkInterval(Cursor cursor) {
		if (cursor == null)
			throw new IllegalArgumentException("cursor must not be null");

		// parseLong gives better error checking than cursor.getLong
		long id = Long.parseLong(cursor.getString(0));
		String taskName = cursor.getString(1);
		String subjectName = cursor.getString(2);
		long start = Long.parseLong(cursor.getString(3));
		long end = Long.parseLong(cursor.getString(4));
		boolean isCertain = Integer.parseInt(cursor.getString(5)) == 1;
		TaskWorkInterval twi = new TaskWorkInterval(id, start, end, isCertain);
		twi.taskName = taskName;
		twi.subjectName = subjectName;
		return twi;
	}
}
