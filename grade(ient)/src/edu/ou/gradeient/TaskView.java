package edu.ou.gradeient;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TaskView extends ListActivity {
	
	private static final String TAG = "TaskView";
	private static final int ADD_REQUEST = 1;
	private static final int EDIT_REQUEST = 2;
	
	private ArrayAdapter<Task> arrayAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_view);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//Set up an ArrayAdapter with data from model.
		//This adapter contains the names of the tasks.
		arrayAdapter = new ArrayAdapter<Task>(this,
				android.R.layout.simple_list_item_2, android.R.id.text1,
				GradeientApp.getModel().getTaskList()) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				TextView text2 = (TextView) view.findViewById(android.R.id.text2);

				text1.setText(GradeientApp.getModel().getTaskAtIndex(position).toString());
				text2.setText(GradeientApp.getModel().getTaskDueDateAtIndex(position));
				return view;
			}
		};
					
		setListAdapter(arrayAdapter);
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
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
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
	protected void onListItemClick (ListView l, View v, int position, long id)
	{
		Intent intent = new Intent (this, EditTaskActivity.class);
		//Indicate that this is an existing task to edit
		intent.putExtra(EditTaskActivity.Extras.TASK_STATUS,
				EditTaskActivity.TaskStatus.EDIT_TASK);
		//gets the task that was clicked.
		Task task = (Task) getListView().getItemAtPosition(position);
		intent.putExtra(EditTaskActivity.Extras.TASK_ID, task.getId() );
		startActivityForResult(intent, EDIT_REQUEST);
	}
	
	private class LongClickListener 
	implements AdapterView.OnItemLongClickListener
	{
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			Log.i(TAG,  "Long click at position " + position);
			//TODO: Select the list item at this position so we can do
			//something with it 
			return true;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent data) {
		Log.d(TAG, "A result happened!");
		switch (requestCode) {
			case ADD_REQUEST:
				// INTENTIONALLY FALLING THROUGH
			case EDIT_REQUEST:
				// Make sure the view updates
				if (resultCode == RESULT_OK)
					arrayAdapter.notifyDataSetChanged();
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
		intent.putExtra(EditTaskActivity.Extras.TASK_STATUS, 
				EditTaskActivity.TaskStatus.NEW_TASK);
		startActivityForResult(intent, ADD_REQUEST);
	}
}
