package edu.ou.gradeient;

import edu.ou.gradeient.data.Task;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView;

// loader manager methods from 
// http://www.androiddesignpatterns.com/2012/07/understanding-loadermanager.html

/**
 * Activity for viewing a list of tasks from the database
 */
public class TaskListActivity extends ListActivity 
		implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = "TaskListActivity";
	
	private static final int ADD_REQUEST = 1;
	private static final int EDIT_REQUEST = 2;
	
	private static final String[] COLUMNS = { Task.Schema._ID, 
		Task.Schema.NAME, Task.Schema.END_INSTANT };
	
	private static final int LOADER_ID = 1;
	/** Callbacks to interact with the LoaderManager */
	private LoaderManager.LoaderCallbacks<Cursor> callbacks;
	/** Adapter that binds the data to the ListView */
	private SimpleCursorAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list_view);
		
		// Mappings of columns to views
		String[] dataColumns = { Task.Schema.NAME, Task.Schema.END_INSTANT };
		int[] viewIDs = { android.R.id.text1, android.R.id.text2 };

		// Initialize the adapter. Note that we pass a 'null' Cursor as the
		// third argument. We will pass the adapter a Cursor only when the
		// data has finished loading for the first time (i.e. when the
		// LoaderManager delivers the data to onLoadFinished). Also note
		// that we have passed the '0' flag as the last argument. This
		// prevents the adapter from registering a ContentObserver for the
		// Cursor (the CursorLoader will do this for us!).
		adapter = new SimpleCursorAdapter(this, 
				android.R.layout.simple_list_item_2, null, dataColumns, 
				viewIDs, 0) {
			@Override
			public void setViewText(TextView v, String text) {
				//TODO is this a good formatting method or should we use
				// Joda time? (the place where I got this code from mentioned
				// something a hack being needed for time zone support)
				if (v.getId() == android.R.id.text2) {
					// text2 is the line for the date, so format the date properly
					// instead of displaying a number in milliseconds.
					int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
							| DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY
							| DateUtils.FORMAT_SHOW_TIME;
					if (DateFormat.is24HourFormat(getApplicationContext()))
						flags |= DateUtils.FORMAT_24HOUR;
					text = DateUtils.formatDateTime(getApplicationContext(),
							Long.parseLong(text), flags);
				}
				v.setText(text);
			}
		};

		// Associate the (now empty) adapter with the ListView.
		setListAdapter(adapter);

		// The Activity (which implements the LoaderCallbacks<Cursor> 
		// interface) is the callbacks object through which we will interact
		// with the LoaderManager. The LoaderManager uses this object to
		// instantiate the Loader and to notify the client when data is made
		// available/unavailable.
		callbacks = this;

		// Initialize the Loader with id '1' and callbacks 'mCallbacks'.
		// If the loader doesn't already exist, one is created. Otherwise,
		// the already created Loader is reused. In either case, the
		// LoaderManager will manage the Loader across the Activity/Fragment
		// lifecycle, will receive any new loads once they have completed,
		// and will report this new data back to the 'mCallbacks' object.
		getLoaderManager().initLoader(LOADER_ID, null, callbacks);
		
		//Add a listener to handle long clicks
		getListView().setOnItemLongClickListener(new LongClickListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				//TODO is this needed? some docs say it isn't, as long as a 
				// parent activity is specified in the manifest.
				
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				NavUtils.navigateUpFromSameTask(this);
				return true;
			case R.id.action_add_task:
				addTask();
				return true;
			case R.id.action_settings:
				return true; //TODO
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, EditTaskActivity.class);
		// Indicate that this is an existing task to edit
		intent.putExtra(Extras.TASK_STATUS,
				Extras.TaskStatus.EDIT_TASK);
		// Indicate that it is the task with the given ID that should be edited
		intent.putExtra(Extras.TASK_ID, id);
		startActivityForResult(intent, EDIT_REQUEST);
	}
	
	private class LongClickListener 
	implements AdapterView.OnItemLongClickListener
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, 
				int position,long id) {
			System.out.println("Long click at position " + position);
			//TODO: Do something with the click!
			return true;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent data) {
		Log.d(TAG, "A result happened!");
		switch (requestCode) {
			case ADD_REQUEST:
				// intentionally falling through
			case EDIT_REQUEST:
				// Make sure the view updates
				if (resultCode == RESULT_OK)
					adapter.notifyDataSetChanged();
				break;
			default:
				Log.wtf(TAG, "Unknown request code: " + requestCode);
		}
	}

	/**
	 * Starts an intent to add a new task
	 */
	private void addTask() {
		Intent intent = new Intent(this, EditTaskActivity.class);
		// Indicate that this is a new task
		intent.putExtra(Extras.TASK_STATUS, 
				Extras.TaskStatus.NEW_TASK);
		startActivityForResult(intent, ADD_REQUEST);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case LOADER_ID:
				// For now, get all the tasks, but only the columns specified.
				return new CursorLoader(this, Task.Schema.CONTENT_URI,
						COLUMNS, null, null, null);
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int id = loader.getId();
		switch (id) {
			case LOADER_ID:
				// The asynchronous load is complete and the data is now
				// available for use. Only now can we associate the queried 
				// Cursor with the SimpleCursorAdapter.
				adapter.swapCursor(data);
				break;
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
		// The listview now displays the queried data.
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		switch (id) {
			case LOADER_ID:
				// For whatever reason, the Loader's data is now unavailable.
				// Remove any references to the old data by replacing it with
				// a null Cursor.
				adapter.swapCursor(null);
				break;
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
	}
}
