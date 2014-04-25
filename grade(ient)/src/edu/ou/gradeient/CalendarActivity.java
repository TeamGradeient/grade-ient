package edu.ou.gradeient;

import org.joda.time.DateTime;

import edu.ou.gradeient.data.Task;
import edu.ou.gradeient.data.TaskWorkInterval;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class CalendarActivity extends Activity
	implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "CalendarActivity";
	private static final int ADD_REQUEST = 1;
	private static final int TASK_LOADER = 1;
	private static final int WORK_LOADER = 2;
	
	private CalendarView calendarView;
	private Cursor loadedTaskCursor;
	private Cursor loadedWorkCursor;
	private long startMillis;
	private long endMillis;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (savedInstanceState != null) {
			startMillis = savedInstanceState.getLong(Extras.START_MILLIS);
			endMillis = savedInstanceState.getLong(Extras.END_MILLIS);
		}
		calendarView = (CalendarView)findViewById(R.id.calendar_task_view);
		
		getLoaderManager().initLoader(TASK_LOADER, null, this);
		getLoaderManager().initLoader(WORK_LOADER, null, this);
		
		scrollToNow();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calendar, menu);
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
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
			case R.id.action_go_to_now:
				scrollToNow();
				return true;
			case R.id.action_add_task:
				Intent intent = new Intent(this, EditTaskActivity.class);
				// Indicate that this is a new task
				intent.putExtra(Extras.TASK_STATUS, 
						Extras.TaskStatus.NEW_TASK);
				startActivityForResult(intent, ADD_REQUEST);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent data) {
		switch (requestCode) {
			case ADD_REQUEST:
				// Make sure the view updates
				if (resultCode == RESULT_OK) {
					getLoaderManager().restartLoader(TASK_LOADER, null, this);
					getLoaderManager().restartLoader(WORK_LOADER, null, this);
				}
				break;
			//TODO handle edit requests too!
			default:
				Log.wtf(TAG, "Unknown request code: " + requestCode);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Per the documentation, this will save the contents of the Views.
		super.onSaveInstanceState(outState);
		// Save last loaded start and end times
		outState.putLong(Extras.START_MILLIS, startMillis);
		outState.putLong(Extras.END_MILLIS, endMillis);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		synchronized(this) {
			loadedTaskCursor = null;
			loadedWorkCursor = null;
		}
		// Query tasks and work times within 1 week earlier and 3 weeks later.
		//TODO is this a good amount of data to load at once?
		DateTime start = DateTime.now().minusWeeks(1).withTimeAtStartOfDay();
		startMillis = start.getMillis();
		endMillis = start.plusWeeks(4).getMillis();
		switch (id) {
			//TODO WHY WON'T RANGE VERSIONS WORK?
			case TASK_LOADER:
				return new CursorLoader(this, 
						Task.Schema.CONTENT_URI,
//						Task.Schema.getUriForRange(startMillis, endMillis),
						Task.Schema.COLUMNS, null, null, null);
			case WORK_LOADER:
				return new CursorLoader(this, 
						TaskWorkInterval.Schema.CONTENT_URI,
//						TaskWorkInterval.Schema.getUriForRange(startMillis, endMillis),
						TaskWorkInterval.Schema.COLUMNS, null, null, null);
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
	}
	
	@Override
	public synchronized void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int id = loader.getId();
		switch (id) {
			case TASK_LOADER:
				loadedTaskCursor = data;
				break;
			case WORK_LOADER:
				loadedWorkCursor = data;
				break;
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
		// Wait until both parts of the required new data have loaded
		if (loadedTaskCursor != null && loadedWorkCursor != null) {
			System.out.println("# of tasks: " + loadedTaskCursor.getCount());
			System.out.println("# of work times: " + loadedWorkCursor.getCount());
			// Update the calendar view
			calendarView.updateTasks(loadedTaskCursor, loadedWorkCursor, 
					startMillis, endMillis);
			scrollToNow();
			// We don't need to store the cursors
			loadedTaskCursor = null;
			loadedWorkCursor = null;
		}
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// nothing to see here...
	}

	private void scrollToNow() {
		FrameLayout fl = (FrameLayout) findViewById(R.id.calendar_frame);
		int y = calendarView.findYForBeginningOfDay(System.currentTimeMillis());
		fl.scrollTo(0, y);
	}
}
