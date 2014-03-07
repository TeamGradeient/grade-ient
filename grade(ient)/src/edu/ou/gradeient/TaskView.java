package edu.ou.gradeient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

public class TaskView extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_view);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
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

	/**
	 * Starts an intent to add a new task
	 */
	private void addTask() {
		Intent intent = new Intent(this, EditTaskActivity.class);
		// Indicate that this is a new task
		intent.putExtra(EditTaskActivity.Extras.TASK_STATUS, 
				EditTaskActivity.TaskStatus.NEW_TASK);
		// The ID of the task to add/edit can also be passed in the bundle.
		// -1 means new task with no ID specified.
		// TODO remove later if not used
		intent.putExtra(EditTaskActivity.Extras.TASK_ID, -1);
		startActivity(intent);
	}
}
