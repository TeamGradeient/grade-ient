package edu.ou.gradeient;

import java.util.ArrayList;

import org.joda.time.DateTime;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import edu.ou.gradeient.data.Subject;
import edu.ou.gradeient.data.Task;
import edu.ou.gradeient.data.TaskWorkInterval;
import edu.ou.gradeient.data.TimeUtils;

public class HomeScreenActivity extends Activity 
		implements LoaderManager.LoaderCallbacks<Cursor>{

	private static final String TAG = "HomeScreenActivity";
	private static final int DRAWER_ITEM_TASK_LIST = 0;
	private static final int DRAWER_ITEM_CALENDAR = 1;
	
	// These are the columns to get from the database for each view
	private static final String[] TASK_LOADER_COLUMNS = { Task.Schema._ID,
		Task.Schema.SUBJECT_NAME, Task.Schema.NAME, Task.Schema.END_INSTANT,
		Task.Schema.IS_DONE };
	private static final String[] WORK_LOADER_COLUMNS = 
			TaskWorkInterval.Schema.COLUMNS_HYBRID;
	private static final int WHITE_TRANSPARENT = Color.parseColor("#99FFFFFF");
	// ID for the loader for each view
	private static final int TASK_LOADER_ID = 1;
	private static final int WORK_LOADER_ID = 2;
	private static final int NEXT_WORK_LOADER_ID = 3;
	// SimpleCursorAdapter for each view to display the data in the lists
	private SimpleCursorAdapter taskAdapter;
	private SimpleCursorAdapter workAdapter;
	
	/** request to add task */
	private static final int ADD_REQUEST = 1;

	/** DrawerLayout to hold the slide-out drawer */
	private DrawerLayout drawerLayout;
	/** Toggle handler for the drawer */
	private ActionBarDrawerToggle drawerToggle;
	/** ListView to put inside drawer */
	private ListView drawerList;
	/** Adapter to hold string array for drawer items */
	private ArrayAdapter<String> drawerAdapter;
	/** ArrayList of strings for drawer items */
	private ArrayList<String> drawerItems;
	
	private TextView weekdayMonthDate;
	private TextView nextWorkPriority;
	private TextView nextWorkTitle;
	private TextView nextWorkDue;
	private ListView upcomingTasks;
	private ListView upcomingWork;
	
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.app_name);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			actionBar.setHomeButtonEnabled(true);
		
		weekdayMonthDate = (TextView) findViewById(R.id.home_date);
		nextWorkPriority = (TextView)findViewById(R.id.home_next_priority);
		nextWorkTitle = (TextView) findViewById(R.id.home_next_task_title);
		nextWorkDue = (TextView)findViewById(R.id.home_next_due);
		upcomingWork = (ListView)findViewById(R.id.upcoming_work);		
		upcomingTasks = (ListView)findViewById(R.id.upcoming_tasks);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.nav_drawer);
		
		// set up CursorAdapters
		
		// mappings from view IDs to column names
		int[] workViewIds = { R.id.list_subject, R.id.list_name,
				R.id.list_date, R.id.list_time };
		String[] workColumns = { Task.Schema.SUBJECT_NAME, Task.Schema.NAME,
				TaskWorkInterval.Schema.START_INSTANT,
				TaskWorkInterval.Schema.START_INSTANT };
		//TODO how to do lighter/darker based on priority?
		workAdapter = new SimpleCursorAdapter(this, R.layout.home_work_time_list_item,
				null, workColumns, workViewIds, 0);
		workAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				String value = cursor.getString(columnIndex);
				// For uncertain work times, set all the text views to "lighter"
				// text than usual.
				String certainStr = cursor.getString(
						TaskWorkInterval.Schema.COL_CERTAINTY_HYBRID);
				boolean certain = certainStr.length() > 0 
						&& certainStr.charAt(0) == '1';
				if (!certain && view instanceof TextView)
					((TextView) view).setTextColor(WHITE_TRANSPARENT);
				
				// Do any special setting of text
				switch (view.getId()) {
					case R.id.list_date:
						((TextView)view).setText(TimeUtils.formatMonthDayToday(
								Long.parseLong(value)));
						return true;
					case R.id.list_time:
						// For the time field, we need to display a time range,
						// start time - end time.
						long start = Long.parseLong(value);
						long end = Long.parseLong(cursor.getString(
								TaskWorkInterval.Schema.COL_END_HYBRID));
						((TextView)view).setText(TimeUtils.formatTime(start)
								+ '-' + TimeUtils.formatTime(end));
						return true;
					case R.id.list_subject:
						((TextView)view).setText(Subject.abbreviateName(value));
						return true;
				}
				// If the view ID was not one of the ones above, this view binder
				// didn't handle binding the content (probably because it's
				// basic text that can be bound as-is).
				return false;
			}
		});
		upcomingWork.setAdapter(workAdapter);
		
		// mappings from view IDs to column names: the row layout itself is
		// mapped to "is done" so that we can set its background to
		// strikethrough if the task is done
		int[] taskViewIds = { R.id.list_subject, R.id.list_name,
				R.id.list_date, R.id.list_time, R.id.list_layout };
		String[] taskColumns = { Task.Schema.SUBJECT_NAME, Task.Schema.NAME,
				Task.Schema.END_INSTANT, Task.Schema.END_INSTANT,
				Task.Schema.IS_DONE };
		taskAdapter = new SimpleCursorAdapter(this, R.layout.home_list_item, 
				null, taskColumns, taskViewIds, 0);
		taskAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				String value = cursor.getString(columnIndex);
				switch (view.getId()) {
					case R.id.list_date:
						((TextView)view).setText(TimeUtils.formatMonthDayToday(
								Long.parseLong(value)));
						return true;
					case R.id.list_time:
						((TextView)view).setText(TimeUtils.formatTime(
								Long.parseLong(value)));
						return true;
					case R.id.list_subject:
						((TextView)view).setText(Subject.abbreviateName(value));
						return true;
					case R.id.list_layout:
						boolean isDone = false;
						try { 
							isDone = Integer.parseInt(value) == 1; 
						} catch (NumberFormatException ex) { 
						}
						// bg_strikethrough is a special 9-patch drawable that
						// makes it look like the text is struck through
						if (isDone)
							view.setBackgroundResource(R.drawable.bg_strikethrough);
						return true;
				}
				return false;
			}
		});
		upcomingTasks.setAdapter(taskAdapter);
		//Add a listener that will be called when an upcoming task is clicked
		upcomingTasks.setOnItemClickListener(new TaskItemClickListener());
		
		// Load things into the view
		updateView();

		/*--------------Slide-out navigation drawer--------------*/
		drawerItems = new ArrayList<String>();
		drawerItems.add(this.getResources().getString(R.string.action_task_list));
		drawerItems.add(this.getResources().getString(R.string.calendar_view));
		//Set up adapter and attach it to the ListView
		drawerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				android.R.id.text1,
				drawerItems);
		drawerList.setAdapter(drawerAdapter);
		drawerList.setOnItemClickListener(new DrawerItemClickListener());
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_drawer, R.string.open_drawer, R.string.close_drawer) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // can call getActionBar().setTitle(regular title) here
                // if title changes
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // can call getActionBar().setTitle(contextual title) here
                // if title changes
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
	}
	
	private void updateView() {
		// re-init loaders
		getLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
		getLoaderManager().restartLoader(WORK_LOADER_ID, null, this);
		getLoaderManager().restartLoader(NEXT_WORK_LOADER_ID, null, this);
		// updating the time here and immediately making sure it's not
		// ellipsized doesn't work, so the current hack to get around the issue
		// is putting the time update in onCreateLoader/onLoadFinished
	}

	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
	
	public void startCalendarActivity(View view)
	{
		startActivity(new Intent (this, CalendarActivity.class));
	}
	
	public void startTaskListActivity(View view)
	{
		startActivity(new Intent (this, TaskListActivity.class));
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_screen, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle in case it was the app icon
		// touch event
		if (drawerToggle.onOptionsItemSelected(item))
			return true;

		switch (item.getItemId()) {
			case R.id.action_add_task:
				// Start an intent to add a task
				Intent intent = new Intent(this, EditTaskActivity.class);
				// Indicate that this is a new task
				intent.putExtra(Extras.TASK_STATUS, 
						Extras.TaskStatus.NEW_TASK);
				startActivityForResult(intent, -1);
				return true;
			case R.id.action_task_list:
				startActivity(new Intent(this, TaskListActivity.class));
				return true;
			case R.id.action_settings:
				return true; //TODO
		}
		return super.onOptionsItemSelected(item);
	}
	
	// Called whenever we call invalidateOptionsMenu() 
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        menu.findItem(R.id.action_add_task).setVisible(!drawerOpen); //TODO others?
        return super.onPrepareOptionsMenu(menu);
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent data) {
		switch (requestCode) {
			case ADD_REQUEST:
				// Go to the calendar view
				//TODO is that what we want to do?
				//TODO eventually we will want to set up an extra in the intent
				// to scroll to the task start date (Extras.SCROLL_TO)
				startActivity(new Intent(this, CalendarActivity.class));
				break;
			default:
				Log.wtf(TAG, "Unknown request code: " + requestCode);
		}
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Update the time every time there's a query (hack...)
		long now = System.currentTimeMillis();
		weekdayMonthDate.setText(TimeUtils.formatWeekdayMonthDay(now));
		
		// Display tasks and work times in the next two weeks
		long later = new DateTime(now).plusWeeks(2).getMillis();
		System.out.println("Now: " + now + ", later: " + later);
		System.out.println(Task.Schema.getUriForRange(now, later));
		switch (id) {
			case TASK_LOADER_ID:
				//TODO FIGURE OUT WHY RANGE VERSION WON'T WORK
				return new CursorLoader(this, 
						Task.Schema.CONTENT_URI,
//						Task.Schema.getUriForRange(now, later),
						TASK_LOADER_COLUMNS, null, null, null);
			case WORK_LOADER_ID:
				return new CursorLoader(this, 
						TaskWorkInterval.Schema.CONTENT_URI_HYBRID,
//						TaskWorkInterval.Schema.getUriForRangeHybrid(now, later),
						WORK_LOADER_COLUMNS, null, null, null);
			case NEXT_WORK_LOADER_ID:
				// For some reason, there's not a limit parameter for 
				// CursorLoaders or ContentProviders, so you have to tack on
				// the limit to the sort order since that's where it would be
				// in a raw SQL query...
				return new CursorLoader(this,
						TaskWorkInterval.Schema.getUriForRangeHybrid(now, later),
						WORK_LOADER_COLUMNS, null, null, 
						TaskWorkInterval.Schema.SORT_ORDER_DEFAULT_HYBRID + " LIMIT 1");
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Every time a query returns, make sure that the text that was set
		// in onCreateLoader didn't get ellipsized, and fix it if it did.
		// This is a hack to get around the fact that getLayout can return null
		// when the layout has just been changed...
		Layout layout = weekdayMonthDate.getLayout();
		if (layout != null && layout.getEllipsisCount(0) != 0) {
			// use a shorter format if date was ellipsized
			weekdayMonthDate.setText(TimeUtils.formatWeekdayMonDay(
					System.currentTimeMillis()));
		}
		
		int id = loader.getId();
		switch (id) {
			// The asynchronous load is complete and the data is now
			// available for use. Only now can we associate the queried 
			// Cursor with the SimpleCursorAdapter.
			case TASK_LOADER_ID:
				taskAdapter.swapCursor(data);
				Log.i(TAG, "Tasks loaded: " + data.getCount());
				break;
			case WORK_LOADER_ID:
				workAdapter.swapCursor(data);
				Log.i(TAG, "Work times loaded: " + data.getCount());
				break;
			case NEXT_WORK_LOADER_ID:
				if (data.getCount() == 0)
					break;
				data.moveToNext();
				TaskWorkInterval twi;
				try {
					twi = TaskWorkInterval.hybridWorkInterval(data);
				} catch (Exception ex) {
					Log.w(TAG, "Illegal value in database: " + ex);
					break;
				}
				nextWorkTitle.setText(twi.getTaskName());
				nextWorkPriority.setText(twi.isCertain() ? 
						getResources().getString(R.string.home_next_definitely)
						: getResources().getString(R.string.home_next_maybe));
				//TODO we actually need another field (the design includes
				// the task's due date, and we might also want to show the
				// subject somehow) and a way to handle what to show when 
				// the current time is not actually a work time
				break;
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
		// The old cursors will be closed automatically.
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		switch (id) {
			// For whatever reason, the Loader's data is now unavailable.
			// Remove any references to the old data by replacing it with
			// a null Cursor.
			case TASK_LOADER_ID:
				taskAdapter.swapCursor(null); break;
			case WORK_LOADER_ID:
				workAdapter.swapCursor(null); break;
			case NEXT_WORK_LOADER_ID:
				nextWorkTitle.setText("");
				nextWorkPriority.setText("");
				break;
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
	}

	/** Provides a listener to start editing a task when an item is 
	 * clicked on in the next tasks area. */
	private class TaskItemClickListener implements AdapterView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view,
				int position, long id) 
		{
			System.out.println("Task " + id + " clicked!");

			Intent intent = new Intent(adapterView.getContext(), EditTaskActivity.class);
			// Indicate that this is an existing task to edit
			intent.putExtra(Extras.TASK_STATUS, Extras.TaskStatus.EDIT_TASK);
			// Indicate that it is the task with the given ID that should be edited
			intent.putExtra(Extras.TASK_ID, id);
			startActivity(intent);
		}
	}
	
	/** Handles nav drawer item clicks */
	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, 
				int position, long id) 
		{
			switch(position)
			{
				case DRAWER_ITEM_TASK_LIST:
					startTaskListActivity(view);
					break;
				case DRAWER_ITEM_CALENDAR:
					startCalendarActivity(view);
					break;
				default:
					Log.e(TAG, "This task was not recognized!");
					break;
			}
			drawerLayout.closeDrawer(drawerList);
		}
	}
}
