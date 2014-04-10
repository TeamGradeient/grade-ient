package edu.ou.gradeient;

import android.os.Bundle;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class CalendarActivity extends Activity
	implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "CalendarTaskActivity";
	private static final String[] COLUMNS = { Task.Schema._ID, 
		Task.Schema.NAME, Task.Schema.END_INSTANT };
	private static final int ADD_REQUEST = 1;
	private static final int EDIT_REQUEST = 2;
	private static final int LOADER_ID = 1;
	/** Callbacks to interact with the LoaderManager */
	private LoaderManager.LoaderCallbacks<Cursor> callbacks;
	/** Adapter that binds the data to the View */
	private SimpleCursorAdapter adapter;

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
			case LOADER_ID:
				// Create a new CursorLoader with the following query parameters.
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
