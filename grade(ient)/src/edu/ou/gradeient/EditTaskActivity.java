package edu.ou.gradeient;

import org.joda.time.DateTime;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import edu.ou.gradeient.data.Task;
import edu.ou.gradeient.data.TaskWorkInterval;
import edu.ou.gradeient.data.TimeUtils;

// This is somewhat based off of com.android.calendar.event.EditEventView

/**
 * Activity for adding or editing tasks
 */
public class EditTaskActivity extends Activity {
	private static final String TAG = "EditTaskActivity";
	
	private TextView nameText;
	private TextView subjectText;
	private TextView notesText;
	private CheckBox doneCheckBox;

	private Task task;
	private int taskStatus;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_task);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		nameText = (TextView)findViewById(R.id.task_name);
		subjectText = (TextView)findViewById(R.id.subject_name);
		notesText = (TextView)findViewById(R.id.notes);
		doneCheckBox = (CheckBox)findViewById(R.id.is_done);
		Button startDateButton = (Button)findViewById(R.id.start_date);
		Button endDateButton = (Button)findViewById(R.id.end_date);
		Button startTimeButton = (Button)findViewById(R.id.start_time);
		Button endTimeButton = (Button)findViewById(R.id.end_time);
		
		if (savedInstanceState == null)
			setTaskFromIntent();
		else
			setTaskFromBundle(savedInstanceState);
		
		// If we're supposed to create a task (or something went wrong),
		// make a new Task object.
		if (task == null) {
			DateTime start = new DateTime();
			DateTime end = new DateTime(start).plusDays(1);
			task = new Task("", start.getMillis(), end.getMillis());
		}
		TimeUtils.setDateText(task.getInterval(), startTimeButton, 
				startDateButton, endTimeButton, endDateButton);
		startDateButton.setOnClickListener(new DateClickListener(this, task));
		endDateButton.setOnClickListener(new DateClickListener(this, task));
		startTimeButton.setOnClickListener(new TimeClickListener(this, task));
		endTimeButton.setOnClickListener(new TimeClickListener(this, task));
		nameText.setTextKeepState(task.getName());
		subjectText.setTextKeepState(task.getSubject());
		notesText.setTextKeepState(task.getNotes());
		doneCheckBox.setChecked(task.isDone());
	}

	// We don't need to worry about onPause, onStop, or onDestroy because
	// edits should only be committed when the user explicitly says to do so
	// by pushing a button.
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_task, menu);
		return true;
	}

	/**
	 * Get information about the task to add or edit from the Intent that
	 * started the Activity, and get the Task to be edited from the database
	 * if relevant.
	 */
	private void setTaskFromIntent() {
		// Figure out if the user requested to add or edit a task
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			taskStatus = Extras.TaskStatus.NEW_TASK;
			return;
		}
			
		// Get the new/edit status, if specified
		taskStatus = extras.getInt(Extras.TASK_STATUS, -1);
		if (taskStatus == -1)
			taskStatus = Extras.TaskStatus.NEW_TASK;
		// Get the ID, if specified. (Default to NEW_TASK_ID.)
		long taskId = extras.getLong(Extras.TASK_ID, Task.NEW_TASK_ID);

		// If we're supposed to edit a task, get its Task object by ID.
		if (taskStatus == Extras.TaskStatus.EDIT_TASK) {
			if (taskId == Task.NEW_TASK_ID) {
				Log.e(TAG, "Requested editing a task with ID NEW_TASK_ID.");
			} else {
				// Try to get the task with the given ID. (Don't use a 
				// loader here because we do actually need to block until
				// the operation completes.)
				Cursor cursor = getContentResolver().query(
						ContentUris.withAppendedId(Task.Schema.CONTENT_URI, 
								taskId),
						null, null, null, null);
				try {
					if (cursor.getCount() > 0) {
						cursor.moveToNext();
						// Try to make a Task object from the cursor row
						task = new Task(cursor, null); //TODO add work times
					}
				} catch (Exception ex) {
					Log.e(TAG, "Error reading task from database", ex);
				} finally {
					// Make sure the cursor is closed even if something
					// strange happens.
					cursor.close();
				}

				// This is bad but not fatal. Create a new task instead.
				if (task == null) {
					Log.w(TAG, "Couldn't find requested task ID " + taskId);
					taskStatus = Extras.TaskStatus.NEW_TASK;
				}
			}
		}
	}
	
	/**
	 * Fill the task and status from the saved instance state
	 * @param savedInstanceState The bundle passed to onCreate
	 */
	private void setTaskFromBundle(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			Log.w(TAG, "savedInstanceState in setTaskFromBundle was null");
			return;
		}
		taskStatus = savedInstanceState.getInt(Extras.TASK_STATUS);
		try {
			task = (Task)savedInstanceState.getSerializable(Extras.TASK_OBJ);
		} catch (ClassCastException ex) {
			Log.w(TAG, "Task could not be de-serialized.");
			taskStatus = Extras.TaskStatus.NEW_TASK;
		}
	}
	
	/**
	 * Fill the fields in the task from the values entered in the UI.
	 * The date fields should have been getting updated continuously, so
	 * don't fill them here.
	 */
	private void fillTaskNonDateFields() {
		task.setIsDone(doneCheckBox.isChecked());
		task.setName(nameText.getText().toString());
		task.setNotes(notesText.getText().toString());
		task.setSubject(subjectText.getText().toString());
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// Don't use navigateUpFromSameTask here--it will go up too far.
				// INTENTIONALLY FALLING THROUGH
			case R.id.action_cancel:
				setResult(RESULT_CANCELED, new Intent());     
				finish();
				// Don't update anything.
				return true;
			case R.id.action_accept:
				// Update the data stored in the task object, then add/update
				// the object in the database.
				fillTaskNonDateFields();
				switch (taskStatus) {
					case Extras.TaskStatus.NEW_TASK:
						Uri taskUri = getContentResolver().insert(
								Task.Schema.CONTENT_URI, 
								task.toContentValues());
						//TODO TEMPORARY: add some random work intervals to the
						// task and put them in the database.
						long id = ContentUris.parseId(taskUri);
						Log.i(TAG, "Old id: " + task.getId() + "; new ID: " + id);
						task.setId(id);
						task.addRandomWorkIntervals();
						for (TaskWorkInterval twi : task.getWorkIntervals()) {
							getContentResolver().insert(
									TaskWorkInterval.Schema.CONTENT_URI,
									twi.toContentValues());
						}
						break;
					case Extras.TaskStatus.EDIT_TASK:
						getContentResolver().update(
								ContentUris.withAppendedId(
										Task.Schema.CONTENT_URI, task.getId()),
								task.toContentValues(), null, null);
						break;
					default:
						Log.wtf(TAG, "Unknown task status: " + taskStatus);
						setResult(RESULT_CANCELED);
						finish();
						return true;
				}
				setResult(RESULT_OK, new Intent());
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Per the documentation, this will save the contents of the Views.
		super.onSaveInstanceState(outState);
		// Save the task status
		outState.putInt(Extras.TASK_STATUS, taskStatus);
		// Save a serialized Task object
		outState.putSerializable(Extras.TASK_OBJ, task);
	}
}