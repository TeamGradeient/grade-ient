package edu.ou.gradeient.data;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

// Followed tutorial: 
// https://www.grokkingandroid.com/android-tutorial-writing-your-own-content-provider/

/**
 * This is a ContentProvider for accessing the database of tasks, subjects,
 * and semesters. (Currently, all the subject and semester parts are commented.)
 * The data is accessed by querying the URIs defined in the classes
 * corresponding to each table (Task, Subject, etc.). 
 * 
 * To get only the task/etc. with a specific ID, use ContentUris.withAppendedId
 * to append the ID to the URI.
 * 
 * To get tasks that overlap a certain date range, use Uri.withAppendedPath
 * to append start_millis/end_millis to the task table URI.
 */
public class TaskProvider extends ContentProvider {
	private static final String TAG = "TaskProvider";
	
	/** The authority of the Task provider */
	public static final String AUTHORITY = "edu.ou.gradeient";
	/** The URI of the Task authority */
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	// helper constants for use with the UriMatcher
	private static final int TASKS = 1;
	private static final int TASK_ID = 2;
	private static final int TASK_DATES = 3;
	private static final int TASK_WORK_INTERVALS = 4;
	private static final int TASK_WORK_INTERVAL_ID = 5;
	private static final int TASK_WORK_INTERVALS_TASK_ID = 6;
	private static final int TASK_WORK_INTERVALS_DATES = 7;
	private static final int WORK_INTERVALS_TASKS = 8;
	private static final int WORK_INTERVALS_TASKS_DATES = 9;
//	private static final int SUBJECTS = 10;
//	private static final int SUBJECT_ID = 11;
//	private static final int SEMESTERS = 20;
//	private static final int SEMESTER_ID = 21;
	private static final UriMatcher URI_MATCHER;
	// prepare the UriMatcher
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, "tasks", TASKS);
		URI_MATCHER.addURI(AUTHORITY, "tasks/#", TASK_ID);
		URI_MATCHER.addURI(AUTHORITY, "tasks/#/#", TASK_DATES);
		URI_MATCHER.addURI(AUTHORITY, "tasks/work_intervals", TASK_WORK_INTERVALS);
		URI_MATCHER.addURI(AUTHORITY, "tasks/work_intervals/#", TASK_WORK_INTERVAL_ID);
		URI_MATCHER.addURI(AUTHORITY, "tasks/work_intervals/task/#", TASK_WORK_INTERVALS_TASK_ID);
		URI_MATCHER.addURI(AUTHORITY, "tasks/work_intervals/#/#", TASK_WORK_INTERVALS_DATES);
		URI_MATCHER.addURI(AUTHORITY, "work_intervals_tasks", WORK_INTERVALS_TASKS);
		URI_MATCHER.addURI(AUTHORITY, "work_intervals_tasks/#/#", WORK_INTERVALS_TASKS_DATES);
//		URI_MATCHER.addURI(AUTHORITY, "subjects", SUBJECTS);
//		URI_MATCHER.addURI(AUTHORITY, "subjects/#", SUBJECT_ID);
//		URI_MATCHER.addURI(AUTHORITY, "semesters", SEMESTERS);
//		URI_MATCHER.addURI(AUTHORITY, "semesters/#", SEMESTER_ID);
	}
	
	/** "where" clause template (for TextUtils.expandTemplate) for getting
	 * tasks that overlap a certain date range */
	private static final String TASK_DATES_WHERE = 
			"(^1 between " + Task.Schema.START_INSTANT + " and " + 
					Task.Schema.END_INSTANT + ") "
			+ "or (" + Task.Schema.START_INSTANT + " between ^1 and ^2)"
			+ " or (" + Task.Schema.END_INSTANT + " between ^1 and ^2)";
	private static final String WORK_INTERVALS_TASKS_WHERE = TASK_DATES_WHERE
			.replaceAll(Task.Schema.START_INSTANT, 
					TaskWorkInterval.Schema.TABLE + "." + 
							TaskWorkInterval.Schema.START_INSTANT)
							.replaceAll(Task.Schema.END_INSTANT,
									TaskWorkInterval.Schema.TABLE + "." +
											TaskWorkInterval.Schema.END_INSTANT);
//	private static final String WORK_INTERVALS_TASKS_WHERE =
//			TextUtils.expandTemplate("(^1 between ^2.^3 and ^2.^4) "
//					+ "or (^2.^3 > ^1)", "^1", TaskWorkInterval.Schema.TABLE,
//					TaskWorkInterval.Schema.START_INSTANT,
//					TaskWorkInterval.Schema.END_INSTANT).toString();
	
	@Override
	public boolean onCreate() {
		// Initialize the database.
		// (ContentProviders are apparently created before Application.onCreate()
		// is called, so we can't use GradeientApp's getAppContext() method
		// because the instance won't have been initialized.)
		Database.getHelper(getContext().getApplicationContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		int uriMatch = URI_MATCHER.match(uri);
		
		// Get the start and end date segments of the URI if applicable
		if (uriMatch == TASK_DATES || uriMatch == TASK_WORK_INTERVALS_DATES
				|| uriMatch == WORK_INTERVALS_TASKS_DATES) {
			List<String> segments = uri.getPathSegments();
			if (segments.size() < 2) // shouldn't happen, but check anyway
				throw new IllegalArgumentException("Unsupported URI: " + uri);
			String start = segments.get(segments.size() - 2);
			String end = segments.get(segments.size() - 1);
			if (uriMatch == TASK_DATES || uriMatch == TASK_WORK_INTERVALS_DATES)
				builder.appendWhereEscapeString(TextUtils.expandTemplate(
						TASK_DATES_WHERE, start, end).toString());
			else
				builder.appendWhereEscapeString(TextUtils.expandTemplate(
						WORK_INTERVALS_TASKS_WHERE, start, end).toString());
		}
		// In the case of most *_ID queries, limit to returning the one result 
		// matching the requested ID.
		else if (uriMatch == TASK_ID) {
//				|| uriMatch == SUBJECT_ID || uriMatch == SEMESTER_ID) {
			builder.appendWhere(BaseColumns._ID + " = " + 
					uri.getLastPathSegment());
		}
		
		// Choose the correct table (and the sort order and columns if relevant)
		switch (uriMatch) {
			case TASKS:
			case TASK_DATES:
				if (TextUtils.isEmpty(sortOrder))
					sortOrder = Task.Schema.SORT_ORDER_DEFAULT;
				// falling through
			case TASK_ID:
				builder.setTables(Task.Schema.TABLE);
				break;
			case TASK_WORK_INTERVALS:
			case TASK_WORK_INTERVALS_DATES:
			case TASK_WORK_INTERVALS_TASK_ID:
				if (TextUtils.isEmpty(sortOrder))
					sortOrder = TaskWorkInterval.Schema.SORT_ORDER_DEFAULT;
				builder.setTables(TaskWorkInterval.Schema.TABLE);
				break;
			case WORK_INTERVALS_TASKS:
			case WORK_INTERVALS_TASKS_DATES:
				if (TextUtils.isEmpty(sortOrder))
					sortOrder = TaskWorkInterval.Schema.SORT_ORDER_DEFAULT_HYBRID;
				builder.setTables(TaskWorkInterval.Schema.TABLE_HYBRID);
				if (projection == null)
					projection = TaskWorkInterval.Schema.COLUMNS_HYBRID;
				break;
//			case SUBJECTS:
//				if (TextUtils.isEmpty(sortOrder))
//					sortOrder = Subject.Schema.SORT_ORDER_DEFAULT;
//				// falling through
//			case SUBJECT_ID:
//				builder.setTables(Subject.Schema.TABLE);
//				break;
//			case SEMESTERS:
//				if (TextUtils.isEmpty(sortOrder))
//					sortOrder = Semester.Schema.SORT_ORDER_DEFAULT;
//				// falling through
//			case SEMESTER_ID:
//				builder.setTables(Semester.Schema.TABLE);
//				break;
			// TASK_WORK_INTERVALS and TASK_WORK_INTERVAL_ID can't be queried
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		SQLiteDatabase db = Database.getHelper().getReadableDatabase();
		Cursor cursor = builder.query(db, projection, selection, selectionArgs,
				null, null, sortOrder, null);
		//TODO apparently if returning joins of tables, it might be better to
		// use the general authority URI instead
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case TASKS:
			case TASK_DATES:
				return Task.Schema.CONTENT_TYPE;
			case TASK_ID:
				return Task.Schema.CONTENT_ITEM_TYPE;
			case TASK_WORK_INTERVALS: 
			case TASK_WORK_INTERVALS_TASK_ID: 
			case TASK_WORK_INTERVALS_DATES:
				return TaskWorkInterval.Schema.CONTENT_TYPE;
			case TASK_WORK_INTERVAL_ID: 
				return TaskWorkInterval.Schema.CONTENT_ITEM_TYPE;
			case WORK_INTERVALS_TASKS:
			case WORK_INTERVALS_TASKS_DATES:
				return TaskWorkInterval.Schema.CONTENT_TYPE;
//			case SUBJECTS:		return Subject.Schema.CONTENT_TYPE;
//			case SUBJECT_ID:	return Subject.Schema.CONTENT_ITEM_TYPE;
//			case SEMESTERS:		return Semester.Schema.CONTENT_TYPE;
//			case SEMESTER_ID:	return Semester.Schema.CONTENT_ITEM_TYPE;
			case -1:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
			default:
				Log.wtf(TAG, "Known URI did not match content type: match "
						+ "value was " + URI_MATCHER.match(uri) + ", URI was"
						+ uri.toString());
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String table = null;
		switch (URI_MATCHER.match(uri)) {
			case TASKS: 	table = Task.Schema.TABLE; break;
			case TASK_WORK_INTERVALS: 
				table = TaskWorkInterval.Schema.TABLE; break;
//			case SUBJECTS: 	table = Subject.Schema.TABLE; break;
//			case SEMESTERS: table = Semester.Schema.TABLE; break;
			default:
				throw new IllegalArgumentException(
						"Unsupported URI for insertion: " + uri);
		}
		SQLiteDatabase db = Database.getHelper().getWritableDatabase();
		long id = db.insert(table, null, values);

		if (id == -1) {
			Log.w(TAG, "Error inserting into database!");
			return null;
		}
		Uri itemUri = ContentUris.withAppendedId(uri, id);
		// Notify all listeners of changes
		getContext().getContentResolver().notifyChange(itemUri, null);
		return itemUri;
	}

	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs) {
		// Add the "where _id = (id)" clause for single deletions
		switch (URI_MATCHER.match(uri)) {
			case TASK_ID:
			case TASK_WORK_INTERVAL_ID:
//			case SUBJECT_ID:
//			case SEMESTER_ID:
				String idEq = BaseColumns._ID + " = " + uri.getLastPathSegment();
				if (TextUtils.isEmpty(whereClause))
					whereClause = idEq;
				else
					whereClause = idEq + " AND " + whereClause;
				break;
			case TASK_WORK_INTERVALS_TASK_ID:
				String taskIdEq = TaskWorkInterval.Schema.TASK_ID + " = " + 
						uri.getLastPathSegment();
				if (TextUtils.isEmpty(whereClause))
					whereClause = taskIdEq;
				else
					whereClause = taskIdEq + " AND " + whereClause;
				break;
		}
		
		// Figure out what table to delete from
		String table = null;
		switch (URI_MATCHER.match(uri)) {
			case TASK_ID:
			case TASKS:
				table = Task.Schema.TABLE; break;
			case TASK_WORK_INTERVAL_ID:
			case TASK_WORK_INTERVALS_TASK_ID:
				table = TaskWorkInterval.Schema.TABLE; break;
//			case SUBJECT_ID:
//				// falling through
//			case SUBJECTS:
//				table = Subject.Schema.TABLE; break;
//			case SEMESTERS:
//				table = Semester.Schema.TABLE; break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		SQLiteDatabase db = Database.getHelper().getWritableDatabase();
		int delCount = db.delete(table, whereClause, whereArgs);
		if (delCount > 0)
			getContext().getContentResolver().notifyChange(uri, null);
		return delCount;
	}

	@Override
	public int update(Uri uri, ContentValues values, String whereClause,
			String[] whereArgs) {
		// Add the "where _id = (id)" clause for single selections
		switch (URI_MATCHER.match(uri)) {
			case TASK_ID:
			case TASK_WORK_INTERVAL_ID:
//			case SUBJECT_ID:
//			case SEMESTER_ID:
				String idEq = BaseColumns._ID + " = " + uri.getLastPathSegment();
				if (TextUtils.isEmpty(whereClause))
					whereClause = idEq;
				else
					whereClause = idEq + " AND " + whereClause;
				break;
			case TASK_WORK_INTERVALS_TASK_ID:
				String taskIdEq = TaskWorkInterval.Schema.TASK_ID + " = " + 
						uri.getLastPathSegment();
				if (TextUtils.isEmpty(whereClause))
					whereClause = taskIdEq;
				else
					whereClause = taskIdEq + " AND " + whereClause;
				break;
		}
		
		// Figure out what table to update
		String table = null;
		switch (URI_MATCHER.match(uri)) {
			case TASK_ID:
			case TASKS:
				table = Task.Schema.TABLE;
				break;
			case TASK_WORK_INTERVAL_ID:
			case TASK_WORK_INTERVALS_TASK_ID:
				table = TaskWorkInterval.Schema.TABLE; break;
//			case SUBJECT_ID:
//			case SUBJECTS:
//				table = Subject.Schema.TABLE;
//				break;
//			case SEMESTERS:
//				table = Semester.Schema.TABLE;
//				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		SQLiteDatabase db = Database.getHelper().getWritableDatabase();
		int updateCount = db.update(table, values, whereClause, whereArgs);
		if (updateCount > 0)
			getContext().getContentResolver().notifyChange(uri, null);
		return updateCount;
	}
}
