package edu.ou.gradeient;

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
 * corresponding to each table (Task, Subject, etc.). You can also append a
 * number to the end of (for example) the Task URI (using Uri.withAppenededPath)
 * to get only the task with that ID. 
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
//	private static final int SUBJECTS = 3;
//	private static final int SUBJECT_ID = 4;
//	private static final int SEMESTERS = 5;
//	private static final int SEMESTER_ID = 6;
	private static final UriMatcher URI_MATCHER;
	// prepare the UriMatcher
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, "tasks", TASKS);
		URI_MATCHER.addURI(AUTHORITY, "tasks/#", TASK_ID);
//		URI_MATCHER.addURI(AUTHORITY, "subjects", SUBJECTS);
//		URI_MATCHER.addURI(AUTHORITY, "subjects/#", SUBJECT_ID);
//		URI_MATCHER.addURI(AUTHORITY, "semesters", SEMESTERS);
//		URI_MATCHER.addURI(AUTHORITY, "semesters/#", SEMESTER_ID);
	}
	
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
		// In the case of TASK_ID, SUBJECT_ID, or SEMESTER_ID, limit to
		// returning the one result matching the requested ID.
		switch (uriMatch) {
			case TASK_ID: 
//			case SUBJECT_ID: 
//			case SEMESTER_ID:
				builder.appendWhere(BaseColumns._ID + " = " + 
						uri.getLastPathSegment());
		}
		
		// Choose the correct table (and the sort order if relevant)
		switch (uriMatch) {
			case TASKS:
				if (TextUtils.isEmpty(sortOrder))
					sortOrder = Task.Schema.SORT_ORDER_DEFAULT;
				// falling through
			case TASK_ID:
				builder.setTables(Task.Schema.TABLE);
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
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		SQLiteDatabase db = Database.getHelper().getReadableDatabase();
		Cursor cursor = builder.query(db, projection, selection, selectionArgs,
				null, null, sortOrder);
		//TODO apparently if returning joins of tables, it might be better to
		// use the general authority URI instead
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case TASKS:			return Task.Schema.CONTENT_TYPE;
			case TASK_ID:		return Task.Schema.CONTENT_ITEM_TYPE;
//			case SUBJECTS:		return Subject.Schema.CONTENT_TYPE;
//			case SUBJECT_ID:	return Subject.Schema.CONTENT_ITEM_TYPE;
//			case SEMESTERS:		return Semester.Schema.CONTENT_TYPE;
//			case SEMESTER_ID:	return Semester.Schema.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String table = null;
		switch (URI_MATCHER.match(uri)) {
			case TASKS: 	table = Task.Schema.TABLE; break;
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
		//TODO tutorial had check using nonexistant method isInBatchMode()
		// Notify all listeners of changes
		getContext().getContentResolver().notifyChange(itemUri, null);
		return itemUri;
	}

	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs) {
		// Add the "where _id = (id)" clause for single deletions
		switch (URI_MATCHER.match(uri)) {
			case TASK_ID:
//			case SUBJECT_ID:
//			case SEMESTER_ID:
				String idEq = BaseColumns._ID + " = " + uri.getLastPathSegment();
				if (TextUtils.isEmpty(whereClause))
					whereClause = idEq;
				else
					whereClause = idEq + " AND " + whereClause;
		}
		
		// Figure out what table to delete from
		String table = null;
		switch (URI_MATCHER.match(uri)) {
			case TASK_ID:
				// falling through
			case TASKS:
				table = Task.Schema.TABLE;
				break;
//			case SUBJECT_ID:
//				// falling through
//			case SUBJECTS:
//				table = Subject.Schema.TABLE;
//				break;
//			case SEMESTERS:
//				table = Semester.Schema.TABLE;
//				break;
			//TODO should there be anything that's unsupported for updating?
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
//			case SUBJECT_ID:
//			case SEMESTER_ID:
				String idEq = BaseColumns._ID + " = " + uri.getLastPathSegment();
				if (TextUtils.isEmpty(whereClause))
					whereClause = idEq;
				else
					whereClause = idEq + " AND " + whereClause;
		}
		
		// Figure out what table to update
		String table = null;
		switch (URI_MATCHER.match(uri)) {
			case TASK_ID:
				// falling through
			case TASKS:
				table = Task.Schema.TABLE;
				break;
//			case SUBJECT_ID:
//				// falling through
//			case SUBJECTS:
//				table = Subject.Schema.TABLE;
//				break;
//			case SEMESTERS:
//				table = Semester.Schema.TABLE;
//				break;
			//TODO should there be anything that's unsupported for updating?
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
