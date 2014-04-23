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

	private static final String TAG = "CalendarTaskActivity";
	private static final int ADD_REQUEST = 1;
	private static final int TASK_LOADER = 1;
	private static final int WORK_LOADER = 2;
	
	private CalendarView calendarView;
	private Cursor taskCursor;
	private Cursor workCursor;
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
		
		calendarView = (CalendarView)findViewById(R.id.calendar_task_view);
		
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
		
		getLoaderManager().initLoader(TASK_LOADER, null, this);
		getLoaderManager().initLoader(WORK_LOADER, null, this);
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
				if (resultCode == RESULT_OK)
					//TODO do I need to requery or anything?
					calendarView.updateTasks(taskCursor, workCursor, 
							startMillis, endMillis);
				break;
			//TODO handle edit requests too!
			default:
				Log.wtf(TAG, "Unknown request code: " + requestCode);
		}
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
			taskCursor = loadedTaskCursor;
			workCursor = loadedWorkCursor;
			loadedTaskCursor = null;
			loadedWorkCursor = null;
			// Update the calendar view
			calendarView.updateTasks(taskCursor, workCursor, 
					startMillis, endMillis);
			scrollToNow();
		}
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		switch (id) {
		//TODO do anything to update the calendar view? (maybe not needed)
			case TASK_LOADER:
				taskCursor = null;
				break;
			case WORK_LOADER:
				workCursor = null;
				break;
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
	}

	private void scrollToNow() {
		FrameLayout fl = (FrameLayout) findViewById(R.id.calendar_frame);
		int y = calendarView.findYForBeginningOfDay(System.currentTimeMillis());
		fl.scrollTo(0, y);
	}
}
