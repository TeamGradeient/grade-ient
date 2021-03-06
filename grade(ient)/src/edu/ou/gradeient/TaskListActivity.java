package edu.ou.gradeient;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;
import edu.ou.gradeient.data.Subject;
import edu.ou.gradeient.data.Task;
import edu.ou.gradeient.data.TimeUtils;

// loader manager methods from 
// http://www.androiddesignpatterns.com/2012/07/understanding-loadermanager.html

/**
 * Activity for viewing a list of tasks from the database
 */
public class TaskListActivity extends ListActivity 
		implements LoaderManager.LoaderCallbacks<Cursor>,
		AdapterView.OnItemLongClickListener{
	
	private static final String TAG = "TaskListActivity";
	
	private static final int ADD_REQUEST = 1;
	private static final int VIEW_REQUEST = 2;
	
	private static final String[] COLUMNS = { Task.Schema._ID,
		Task.Schema.NAME, Task.Schema.SUBJECT_NAME, Task.Schema.END_INSTANT,
		Task.Schema.IS_DONE };
	
	private static final int LOADER_ID = 1;
	/** Adapter that binds the data to the ListView */
	private SimpleCursorAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_list_view);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Mappings of columns to views
		String[] dataColumns = { Task.Schema.NAME, Task.Schema.SUBJECT_NAME,
				Task.Schema.END_INSTANT, Task.Schema.IS_DONE, 
				Task.Schema.SUBJECT_NAME };
		int[] viewIDs = { R.id.task_name, R.id.subject_abbrev,
				R.id.task_due, R.id.done_cb, R.id.task_color };
		
		// Initialize the adapter. Note that we pass a 'null' Cursor as the
		// third argument. We will pass the adapter a Cursor only when the
		// data has finished loading for the first time (i.e. when the
		// LoaderManager delivers the data to onLoadFinished). The '0' flag
		// prevents the adapter from registering a ContentObserver for the
		// Cursor (the CursorLoader will do this for us!).
		adapter = new SimpleCursorAdapter(this, R.layout.task_list_item_view,
				null, dataColumns, viewIDs, 0);
		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				switch (view.getId()) {
					case R.id.subject_abbrev:
						// Make the subject into an abbreviation
						String subject = cursor.getString(columnIndex);
						String abbrev = Subject.abbreviateName(subject);
						if (abbrev == null) {
							view.setVisibility(View.GONE);
						} else {
							view.setVisibility(View.VISIBLE);
							((TextView)view).setText(abbrev);
						}
						return true;
					case R.id.task_due:
						long due = Long.parseLong(cursor.getString(columnIndex));
						String text = TimeUtils.formatTimeDate(due);
						((TextView)view).setText(text);
						return true;
					case R.id.done_cb:
						boolean isDone = "1".equals(cursor.getString(columnIndex));
						final long taskId = cursor.getLong(0);
						CheckBox cb = (CheckBox)view;
						cb.setChecked(isDone);
						cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								// Update the task in the database when the
								// checkbox state is changed
								ContentValues cv = new ContentValues(1);
								cv.put(Task.Schema.IS_DONE, isChecked ? 1 : 0);
								getContentResolver().update(
										Task.Schema.getUriForTask(taskId), 
										cv, null, null);
							}
						});
						return true;
					case R.id.task_color:
						int colorRes = Subject.getColor(cursor.getString(columnIndex));
						view.setBackgroundResource(colorRes);
						return true;
				}
				return false;
			}
		});

		// Associate the (now empty) adapter with the ListView.
		setListAdapter(adapter);

		// Initialize the Loader with id LOADER_ID and callbacks this.
		// If the loader doesn't already exist, one is created. Otherwise,
		// the already created Loader is reused. In either case, the
		// LoaderManager will manage the Loader across the Activity/Fragment
		// lifecycle, will receive any new loads once they have completed,
		// and will report new data back to this object (which implements
		// the LoaderCallbacks<Cursor> interface that tells the LoaderManager
		// how to instantiate loaders and allows it to notify the client when
		// data is made available/unavailable).
		getLoaderManager().initLoader(LOADER_ID, null, this);
		
		//Add a listener to handle long clicks
		getListView().setOnItemLongClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
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
		Intent intent = new Intent(this, ViewTaskActivity.class);
		intent.putExtra(Extras.TASK_ID, id);
		startActivityForResult(intent, VIEW_REQUEST);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, 
			int position, final long id) {
		//TODO USING LONG CLICK FOR DELETE IS NOT PERMANENT
		
		// Get the task's name to include in the dialog
		Cursor cursor = (Cursor)adapter.getItem(position);
		if (cursor == null) return false;
		int column = cursor.getColumnIndex(Task.Schema.NAME);
		if (column == -1) return false;
		String name = cursor.getString(column);
		
		new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
		.setTitle(getString(R.string.action_delete_task))
		.setMessage(TextUtils.expandTemplate(
				getString(R.string.delete_msg_template), name))
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				getContentResolver().delete(
						ContentUris.withAppendedId(Task.Schema.CONTENT_URI, id),
						null, null);
				//TODO: This probably should go somewhere else?
				Toast.makeText(getApplicationContext(), 
						getString(R.string.task_deleted),
						Toast.LENGTH_SHORT).show();
			}
		})
		.setNegativeButton(android.R.string.no, null)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.show();
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent data) {
		switch (requestCode) {
			case ADD_REQUEST:
				// intentionally falling through
			case VIEW_REQUEST:
				// Make sure the view updates
				//TODO do we actually need to do this when view request returns?
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
