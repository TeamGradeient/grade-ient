package edu.ou.gradeient;

import java.util.ArrayList;

import edu.ou.gradeient.data.Task;
import edu.ou.gradeient.data.TaskWorkInterval;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class HomeScreenActivity extends Activity 
		implements LoaderManager.LoaderCallbacks<Cursor>{

	private static final String TAG = "HomeScreenActivity";
	private static final int EDIT_REQUEST = 2;
	private static final int TASK_LIST = 0;
	private static final int CALENDAR_ITEM = 1;
	
	// These are the columns to get from the database for each view
	private static final String[] TASK_LOADER_COLUMNS = { Task.Schema._ID,
		Task.Schema.SUBJECT_NAME, Task.Schema.NAME, Task.Schema.END_INSTANT };
	private static final String[] WORK_LOADER_COLUMNS = 
			TaskWorkInterval.Schema.COLUMNS_HYBRID;
	// ID for the loader for each view
	private static final int TASK_LOADER_ID = 1;
	private static final int WORK_LOADER_ID = 2;
	// SimpleCursorAdapter for each view to display the data in the lists
	private SimpleCursorAdapter taskAdapter;
	private SimpleCursorAdapter workAdapter;
	/** Callbacks to interact with the LoaderManager */
	private LoaderManager.LoaderCallbacks<Cursor> callbacks;
	
	/**DrawerLayout to hold the slide-out drawer*/
	private DrawerLayout drawerLayout;
	
	/**ListView to put inside drawer*/
	private ListView drawerList;
	
	/**Adapter to hold string array for drawer items*/
	private ArrayAdapter<String> drawerAdapter;
	
	/**ArrayList of strings for drawer items*/
	private ArrayList<String> drawerItems;
	
	/** request to add task */
	private static final int ADD_REQUEST = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);
//		ActionBar actionBar = getActionBar();
//		actionBar.setTitle(R.string.app_name);
//		actionBar.setDisplayShowTitleEnabled(true);
		
		/*------------display time and date------------*/
		//Date
		TextView dateAndYear = (TextView) findViewById(R.id.home_date);
		int dateFlags = DateUtils.FORMAT_SHOW_DATE;
		dateFlags |= DateUtils.FORMAT_SHOW_YEAR;
		String date = DateUtils.formatDateTime(this,
				System.currentTimeMillis(), dateFlags);
		dateAndYear.setText(date);
		dateAndYear.setTextColor(Color.WHITE);
		
		//Time, and weekday
		//TODO FunTimes/joda-time methods
		TextView timeAndDay = (TextView) findViewById(R.id.home_time_and_day);
		//int timeFlags = DateUtils.FORMAT_12HOUR;
		int timeFlags = DateUtils.FORMAT_SHOW_TIME;
		timeFlags |= DateUtils.FORMAT_SHOW_WEEKDAY;
		timeAndDay.setText(DateUtils.formatDateTime(this, 
				System.currentTimeMillis(), timeFlags));
		timeAndDay.setTextColor(Color.WHITE);
		
		
		/*---------------display next task---------------*/
		TextView nextTaskTitle = (TextView) findViewById(R.id.home_next_task_title);
		nextTaskTitle.setText("Essay 3");

		// set up CursorAdapters
		int[] viewIds = { R.id.list_subject, R.id.list_name,
				R.id.list_date, R.id.list_time };
		String[] workColumns = { Task.Schema.SUBJECT_NAME, Task.Schema.NAME,
				TaskWorkInterval.Schema.START_INSTANT,
				TaskWorkInterval.Schema.START_INSTANT };
		//TODO how to do lighter/darker based on priority?
		workAdapter = new SimpleCursorAdapter(this, R.layout.home_list_item,
				null, workColumns, viewIds, 0) {
			@Override
			public void setViewText(TextView v, String text) {
				//TODO fix this method
				if (v.getId() == R.id.list_date) 
					text = TimeUtils.formatMonthDayTodayTomorrow(Long.parseLong(text));
				else if (v.getId() == R.id.list_time)
					text = TimeUtils.formatTime(Long.parseLong(text));
				v.setText(text);
			}
		};
		ListView upcomingWork = (ListView)findViewById(R.id.upcoming_work);		
		upcomingWork.setAdapter(workAdapter);
		
		String[] taskColumns = { Task.Schema.SUBJECT_NAME, Task.Schema.NAME,
				Task.Schema.END_INSTANT, Task.Schema.END_INSTANT };
		taskAdapter = new SimpleCursorAdapter(this, R.layout.home_list_item, 
				null, taskColumns, viewIds, 0) {
			@Override
			public void setViewText(TextView v, String text) {
				if (v.getId() == R.id.list_date) 
					text = TimeUtils.formatMonthDayTodayTomorrow(Long.parseLong(text));
				else if (v.getId() == R.id.list_time)
					text = TimeUtils.formatTime(Long.parseLong(text));
				v.setText(text);
			}
		};
		ListView upcomingTasks = (ListView)findViewById(R.id.upcoming_tasks);
		upcomingTasks.setAdapter(taskAdapter);

		//Add a listener that will be called when an upcoming task is clicked
		upcomingTasks.setOnItemClickListener(new TaskItemClickListener());

		// register for notifications and init loaders
		callbacks = this;
		getLoaderManager().initLoader(TASK_LOADER_ID, null, callbacks);
		getLoaderManager().initLoader(WORK_LOADER_ID, null, callbacks);
		
		
		/*--------------Slide-out drawer--------------*/
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.nav_drawer);
		
		//Initialize ArrayList of strings
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
		switch (item.getItemId()) {
			case R.id.action_add_task:
				// Start an intent to add a task
				Intent intent = new Intent(this, EditTaskActivity.class);
				// Indicate that this is a new task
				intent.putExtra(EditTaskActivity.Extras.TASK_STATUS, 
						EditTaskActivity.TaskStatus.NEW_TASK);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent data) {
		switch (requestCode) {
			case ADD_REQUEST:
				// Go to the calendar view
				//TODO is that what we want to do?
				//TODO eventually we will want to set up an extra in the intent
				// to scroll to the task start date
				startActivity(new Intent(this, CalendarActivity.class));
				break;
			default:
				Log.wtf(TAG, "Unknown request code: " + requestCode);
		}
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case TASK_LOADER_ID:
				return new CursorLoader(this, Task.Schema.CONTENT_URI,
						TASK_LOADER_COLUMNS, null, null, null);
			case WORK_LOADER_ID:
				return new CursorLoader(this, 
						TaskWorkInterval.Schema.getUriForAwesome(
								System.currentTimeMillis()),
						WORK_LOADER_COLUMNS, null, null, null);
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int id = loader.getId();
		switch (id) {
			// The asynchronous load is complete and the data is now
			// available for use. Only now can we associate the queried 
			// Cursor with the SimpleCursorAdapter.
			case TASK_LOADER_ID:
				Log.i(TAG, "got data: " + data.getCount());
				taskAdapter.swapCursor(data);
				break;
			case WORK_LOADER_ID:
				workAdapter.swapCursor(data);
				break;
			default:
				throw new IllegalArgumentException("Unknown id: " + id);
		}
		// The listview now displays the queried data.
	}
	
	//Provides a listener to start editing a task when an item is clicked on in the
	//next tasks area.
	private class TaskItemClickListener 
	implements AdapterView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view,
				int position, long id) 
		{
			System.out.println("Task " + id + " clicked!");

			Intent intent = new Intent(adapterView.getContext(), EditTaskActivity.class);
			// Indicate that this is an existing task to edit
			intent.putExtra(EditTaskActivity.Extras.TASK_STATUS,
					EditTaskActivity.TaskStatus.EDIT_TASK);
			// Indicate that it is the task with the given ID that should be edited
			intent.putExtra(EditTaskActivity.Extras.TASK_ID, id);
			startActivity(intent);
		}
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position,
				long id) 
		{
			switch(position)
			{
				case TASK_LIST:
					startTaskListActivity(view);
					break;
				case CALENDAR_ITEM:
					startCalendarActivity(view);
					break;
				default:
					Log.e(TAG, "This task was not recognized!");
					break;
			}
			drawerLayout.closeDrawer(drawerList);
		}
		
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
			default:
				throw new IllegalArgumentException("Unknown id: " + id);

//		@Override
//		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//				long arg3) {
//			// TODO Auto-generated method stub
//			
//		}
		}
	}
}
