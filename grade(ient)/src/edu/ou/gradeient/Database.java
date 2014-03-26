package edu.ou.gradeient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLiteOpenHelper for accessing the task database
 */
public class Database extends SQLiteOpenHelper {
	private static final String TAG = "Database";
	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "grade-ient.db";
	private static Database instance;
	
	private Context context;

	/**
	 * Get the shared Database/SQLiteOpenHelper instance. 
	 * DO NOT USE THIS AS PART OF STATIC INITIALIZATION.
	 * @return The Database instance
	 * @throws IllegalStateException if GradeientApp.getAppContext() throws
	 * because no instances of GradeientApp had been created yet
	 */
	public static synchronized Database getHelper() {
		return getHelper(GradeientApp.getAppContext());
	}
	
	/**
	 * Get the shared Database/SQLiteOpenHelper instance.
	 * @param context The context to use to get the Application context
	 * @return The Database instance
	 */
	public static synchronized Database getHelper(Context context) {
		if (instance == null)
			instance = new Database(context);
		return instance;
	}

	private Database(Context context) {
		// Use the application context to avoid leaking an individual context
		super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
		try {
			executeScript(db, "db_create_task.sql");
		} catch (IOException ex) {
			//TODO what to do here?
			Log.e(TAG, "Error creating database", ex);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	/**
	 * Execute a SQL script from a file. Since semicolons could appear in 
	 * various places other than the end of a statement, ;---- must be used
	 * as a statement delimiter.
	 * @param db
	 * @param assetFilename
	 * @throws IOException
	 */
	private void executeScript(SQLiteDatabase db, String assetFilename) 
			throws IOException {
		AssetManager manager = context.getAssets();
		InputStream input = null;
		BufferedReader reader = null;
		StringBuilder text = new StringBuilder();
		try {
			input = manager.open(assetFilename);
			//TODO figure out if this reading method is adequately efficient
			// (or if it even matters in this case)
			reader = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = reader.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
		} finally {
			try {
				if (reader != null) reader.close();
			} finally {
				if (input != null) input.close();
			}
		}
		String[] statements = Pattern.compile(";----").split(text);
		//TODO make sure this works if there are comments in the SQL
		for (String sql : statements) {
			try {
				if (sql.length() > 0)
					db.execSQL(sql);
			} catch (SQLException ex) {
				Log.w(TAG, ex);
			}
		}
	}
}
