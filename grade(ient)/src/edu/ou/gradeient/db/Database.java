package edu.ou.gradeient.db;

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

public class Database extends SQLiteOpenHelper {
	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "grade-ient.db";
	private static final String TAG = "grade(ient)DB";
	private Context context;
	
	public Database(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
		try {
			executeScript(db, "db_create.sql");
		} catch (IOException ex) {
			//TODO what to do here?
			Log.w(TAG, ex);
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
			if (input != null) input.close();
			if (reader != null) reader.close();
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
