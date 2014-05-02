package edu.ou.gradeient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import edu.ou.gradeient.data.Task;
import edu.ou.gradeient.data.TaskWorkInterval;

public class TaskSummaryView extends Activity {

	private static final String TAG = "TaskSummaryView";
	private static final int EDIT_REQUEST = 1;
	
	private Task task;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_summary_view);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (savedInstanceState == null) {
			setTaskFromIntent();
		} else {
			try {
				task =(Task)savedInstanceState.getSerializable(Extras.TASK_OBJ);
			} catch (Exception ex) {
				Log.w(TAG, "Task could not be de-serialized.");
				throw new IllegalArgumentException("Task could not be de-serialized.");
			}
		}
		
		updateView();
	}

		
	private void setTaskFromIntent() {
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			throw new IllegalArgumentException("Missing extras in intent");
		}
		
		long taskId = extras.getLong(Extras.TASK_ID, -1);
		if (taskId == -1) {
			throw new IllegalArgumentException("Must Specify a Task ID");
		}
		setTaskFromDb(taskId);
		
		if (task == null) {
			Log.w(TAG, "Couldn't find requested task ID" + taskId);
			throw new IllegalArgumentException("Couldn't find requested Task ID");
		}
	}

	private void setTaskFromDb(long taskId) {
		Cursor cursor = getContentResolver().query(
				Task.Schema.getUriForTask(taskId), null, null, null, null);
		Cursor workCursor = getContentResolver().query(
				TaskWorkInterval.Schema.getUriForTaskId(taskId), 
				null, null, null, null);
		
		try {
			if (cursor.getCount() > 0) {
				cursor.moveToNext();
				task = new Task(cursor, workCursor);
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error reading task from database", ex);
			throw new IllegalArgumentException("Couldn't find requested Task ID");
		} finally {
			cursor.close();
			workCursor.close();
		}
	}

	private void updateView() {
		TextView taskName = (TextView)findViewById(R.id.task_name);
		TextView subjectName = (TextView)findViewById(R.id.subject_name);
		TextView dueDate = (TextView)findViewById(R.id.due_time);
		
		taskName.setText(task.getName());
		subjectName.setText(task.getSubject());
		dueDate.setText("Due Time Goes Here");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_summary_view, menu);
	
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
				NavUtils.navigateUpFromSameTask(this);
				return true;
				
			case R.id.action_discard:
				//Show pop-up dialogue. Ask if they want to delete.
				new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
				.setTitle(getString(R.string.action_delete_task))
				.setMessage(TextUtils.expandTemplate(
						getString(R.string.delete_msg_template), task.getName()))
				.setPositiveButton(android.R.string.yes, 
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
						getContentResolver().delete(
								Task.Schema.getUriForTask(task.getId()),
								null, null);
						//TODO: This probably should go somewhere else?
						Toast.makeText(getApplicationContext(),
								getString(R.string.task_deleted),
								Toast.LENGTH_SHORT).show();
						finish();
					}
				})
				.setNegativeButton(android.R.string.no, null)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
				return true;
				
			case R.id.action_edit:
				Intent intent = new Intent(this, EditTaskActivity.class);
				// Indicate that this is an existing task to edit
				intent.putExtra(Extras.TASK_STATUS,
						Extras.TaskStatus.EDIT_TASK);
				// Indicate that it is the task with the given ID that should be edited
				intent.putExtra(Extras.TASK_ID, task.getId());
				startActivityForResult(intent, EDIT_REQUEST);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent data) {
		// Make sure the view updates
		if (resultCode == RESULT_OK) {
			setTaskFromDb(task.getId());
			updateView();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(Extras.TASK_OBJ, task);
	}
}
