package edu.ou.gradeient;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class CalendarActivity extends Activity
	implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "CalendarTaskActivity";
	private static final int TASK_LOADER = 1;
	private static final int WORK_LOADER = 2;
	/** Callbacks to interact with the LoaderManager */
	private LoaderManager.LoaderCallbacks<Cursor> callbacks;
	
	// TODO HORRIBLE HORRIBLE HACK DELETE THIS VERY SOON
	public static Cursor tasks;
	public static Cursor workTimes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar);
		// Show the Up button in the action bar.
		setupActionBar();
		
//		LinearLayout ll = (LinearLayout) findViewById(R.id.task_layout);
//		CalendarTaskView ctv = (CalendarTaskView) findViewById(R.id.calendar_task_view);
//		
//		//ll.setWeightSum(1);
//		
//		System.out.println(ctv);
//		System.out.println(ll);
//		View nv = new View(this);
//		nv.setBackgroundColor(Color.BLACK);
//		ll.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
		//nv.setLayoutParams(ll.getLayoutParams());
		//ll.addView(nv);
		//View nv2 = new View(this);
		//nv2.setBackgroundColor(Color.RED);
		//ll.addView(nv2);
		//ctv.populateLayoutWithTasks(ll);
		
		callbacks = this;
		getLoaderManager().initLoader(TASK_LOADER, null, callbacks);
		getLoaderManager().initLoader(WORK_LOADER, null, callbacks);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

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
			FrameLayout fl = (FrameLayout) findViewById(R.id.calendar_frame);
			CalendarTaskView ctv=(CalendarTaskView)findViewById(R.id.calendar_task_view);
			int y = (int)ctv.findYForBeginningOfDay((long)System.currentTimeMillis());
			fl.scrollTo(0, y);
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		//TODO THIS IS TEMPORARY
			case TASK_LOADER:
				// Query all the tasks
				return new CursorLoader(this, Task.Schema.CONTENT_URI,
						Task.Schema.COLUMNS, null, null, null);
			case WORK_LOADER:
				// Query all the work times
				return new CursorLoader(this, TaskWorkInterval.Schema.CONTENT_URI,
						TaskWorkInterval.Schema.COLUMNS, null, null, null);
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int id = loader.getId();
		switch (id) {
			case TASK_LOADER:
				CalendarActivity.tasks = data;
				break;
			case WORK_LOADER:
				CalendarActivity.workTimes = data;
				break;
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		switch (id) {
			case TASK_LOADER:
				CalendarActivity.tasks = null;
				break;
			case WORK_LOADER:
				CalendarActivity.workTimes = null;
				break;
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
	}

}
