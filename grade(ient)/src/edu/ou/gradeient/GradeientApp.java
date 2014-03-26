package edu.ou.gradeient;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * This is the top-level Application object. One instance of this class will be
 * alive throughout the entire time the app is running (it's what's returned
 * by getApplicationContext()). This implementation includes a static method
 * to access the application context instance from places that don't generally
 * have access to a Context.
 * <p>
 * TODO Consider removing this in the future. It was added to facilitate some
 * stuff for the non-database model that's not being used any more, and the
 * method to access the application context from anywhere feels like a 
 * code smell.
 */
public class GradeientApp extends Application {
	private static final String TAG = "GradeientApp";
	private static final boolean DELETE_FILES = false;
	
	private static GradeientApp instance;

	/**
	 * Gets the application context. 
	 * DO NOT CALL FROM STATIC INITIALIZATION METHODS/BLOCKS!
	 * @return The application context
	 * @throws IllegalStateException if the static GradeientApp instance has
	 * not yet been initialized
	 */
	public static Context getAppContext() {
		if (instance == null)
			throw new IllegalStateException("getAppContext() was called "
					+ "when no instance of GradeientApp had been created "
					+ "(did you call it as part of static initialization?)");
		return instance.getApplicationContext();
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		if (DELETE_FILES) deleteFiles();
		Log.d(TAG, "Application instantiated");
	}
	
	/**
	 * Temporary hack for deleting the database if major changes are made to
	 * the schema.
	 * TODO remove from release
	 */
	private void deleteFiles() {
		deleteDatabase("grade-ient.db");
		deleteDatabase("grade-ient.db-journal");
	}
}
