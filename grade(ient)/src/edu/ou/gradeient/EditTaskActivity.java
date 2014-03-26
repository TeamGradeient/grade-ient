package edu.ou.gradeient;

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog; //TODO later, change to radial version?
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

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
	private Button startDateButton;
	private Button endDateButton;
	private Button startTimeButton;
	private Button endTimeButton;

	private TimePickerDialog startTimePickerDialog;
	private TimePickerDialog endTimePickerDialog;
	private DatePickerDialog datePickerDialog;
	
	private Task task;
	private int taskStatus;
	
	/** Options for task status to be passed in bundle */
	public interface TaskStatus {
		public static final int NEW_TASK = 0;
		public static final int EDIT_TASK = 1;
	}
	
	/** Options for things to pass in bundle */
	public interface Extras {
		public static final String TASK_STATUS = "edu.ou.gradeient.TASK_STATUS";
		public static final String TASK_ID = "edu.ou.gradeient.TASK_ID";
	}
	
	private static final String TASK_OBJ = "edu.ou.gradeient.TASK_OBJ";
	
	/* This class is used to update the time buttons. 
	 * (from Android Calendar's com.android.calendar.event.EditEventView) */
	private class TimeListener implements OnTimeSetListener {
		private View view;
		
		public TimeListener(View view) {
			this.view = view;
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			if (this.view == startTimeButton)
				task.setStartTime(hourOfDay, minute, true);
			else
				task.setEndTime(hourOfDay, minute, true);
			updateTimeDateButtons();
		}
	}
	
	private class TimeClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			TimePickerDialog dialog;
			if (v == startTimeButton) {
				DateTime start = task.getStart();
				if (startTimePickerDialog == null) {
					startTimePickerDialog = new TimePickerDialog(
							EditTaskActivity.this, 
							new TimeListener(v), start.getHourOfDay(), 
							start.getMinuteOfHour(), 
							DateFormat.is24HourFormat(EditTaskActivity.this));
				} else {
					startTimePickerDialog.updateTime(start.getHourOfDay(),
							start.getMinuteOfHour());
				}
				dialog = startTimePickerDialog;
			} else {
				DateTime end = task.getEnd();
				if (endTimePickerDialog == null) {
					endTimePickerDialog = new TimePickerDialog(
							EditTaskActivity.this,
							new TimeListener(v), end.getHourOfDay(), 
							end.getMinuteOfHour(),
							DateFormat.is24HourFormat(EditTaskActivity.this));
				} else {
					endTimePickerDialog.updateTime(end.getHourOfDay(), 
							end.getMinuteOfHour());
				}
				dialog = endTimePickerDialog;
			}
			//TODO make sure that this works and we don't need to use a 
			// fragment or any sort of fancy management stuff
			dialog.show(); 
		}
	}
	
	private class DateListener implements OnDateSetListener {
		private View view;
		
		public DateListener(View view) {
			this.view = view;
		}
		
		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			// For some reason, Android's date pickers return months in range
			// 0 to 11, but joda-time (understandably) disapproves...
			if (this.view == startDateButton)
				task.setStartDate(year, month + 1, day, true);
			else
				task.setEndDate(year, month + 1, day);
			updateTimeDateButtons();
		}
	}
	
	private class DateClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			//TODO why does Android Cal implement this differently from
			// the TimeClickListener?
			
			if (datePickerDialog != null)
				datePickerDialog.dismiss();
			DateTime time;
			if (v == startDateButton) 
				time = task.getStart();
			else
				time = task.getEnd();
			// Note that joda-time 1-12 months have to be adjusted to Android
			// date picker 0-11 months...
			datePickerDialog = new DatePickerDialog(EditTaskActivity.this, 
					new DateListener(v), time.getYear(), 
					time.getMonthOfYear() - 1,
					time.getDayOfMonth());
			//TODO make sure that this works and we don't need to use a 
			// fragment or any sort of fancy management stuff
			datePickerDialog.show();
		}
	}
	
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
		startDateButton = (Button)findViewById(R.id.start_date);
		endDateButton = (Button)findViewById(R.id.end_date);
		startTimeButton = (Button)findViewById(R.id.start_time);
		endTimeButton = (Button)findViewById(R.id.end_time);
		
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
		updateTimeDateButtons();
		startDateButton.setOnClickListener(new DateClickListener());
		endDateButton.setOnClickListener(new DateClickListener());
		startTimeButton.setOnClickListener(new TimeClickListener());
		endTimeButton.setOnClickListener(new TimeClickListener());
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
			taskStatus = TaskStatus.NEW_TASK;
			return;
		}
			
		// Get the new/edit status, if specified
		taskStatus = extras.getInt(Extras.TASK_STATUS, -1);
		if (taskStatus == -1)
			taskStatus = TaskStatus.NEW_TASK;
		// Get the ID, if specified. (Default to NEW_TASK_ID.)
		long taskId = extras.getLong(Extras.TASK_ID, Task.NEW_TASK_ID);

		// If we're supposed to edit a task, get its Task object by ID.
		if (taskStatus == TaskStatus.EDIT_TASK) {
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
						task = new Task(cursor);
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
					taskStatus = TaskStatus.NEW_TASK;
				}
			}
		}
	}
	
	/**
	 * Fill the 
	 * @param savedInstanceState The bundle passed to onCreate
	 */
	private void setTaskFromBundle(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			Log.w(TAG, "savedInstanceState in setTaskFromBundle was null");
			return;
		}
		taskStatus = savedInstanceState.getInt(Extras.TASK_STATUS);
		try {
			task = (Task)savedInstanceState.getSerializable(TASK_OBJ);
		} catch (ClassCastException ex) {
			Log.w(TAG, "Task could not be de-serialized.");
			taskStatus = TaskStatus.NEW_TASK;
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
	
	private void updateTimeDateButtons() {
		long startMillis = task.getStartMillis();
		long endMillis = task.getEndMillis();
		setDate(startDateButton, startMillis);
		setTime(startTimeButton, startMillis);
		setDate(endDateButton, endMillis);
		setTime(endTimeButton, endMillis);
	}

	private void setDate(TextView view, long millis) {
		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
				| DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH
				| DateUtils.FORMAT_ABBREV_WEEKDAY;

		//TODO what to do with this?
//		// Unfortunately, DateUtils doesn't support a timezone other than the
//		// default timezone provided by the system, so we have this ugly hack
//		// here to trick it into formatting our time correctly. In order to
//		// prevent all sorts of craziness, we synchronize on the TimeZone class
//		// to prevent other threads from reading an incorrect timezone from
//		// calls to TimeZone#getDefault()
//		String dateString;
//		synchronized (TimeZone.class) {
//			TimeZone.setDefault(TimeZone.getTimeZone(mTimezone));
//			dateString = DateUtils.formatDateTime(mActivity, millis, flags);
//			// setting the default back to null restores the correct behavior
//			TimeZone.setDefault(null);
//		}
		view.setText(DateUtils.formatDateTime(this, millis, flags));
	}

	private void setTime(TextView view, long millis) {

		int flags = DateUtils.FORMAT_SHOW_TIME;
		flags |= DateUtils.FORMAT_CAP_NOON_MIDNIGHT;
		if (DateFormat.is24HourFormat(this)) 
			flags |= DateUtils.FORMAT_24HOUR;

//		//TODO what to do with this?
//		// Unfortunately, DateUtils doesn't support a timezone other than the
//		// default timezone provided by the system, so we have this ugly hack
//		// here to trick it into formatting our time correctly. In order to
//		// prevent all sorts of craziness, we synchronize on the TimeZone class
//		// to prevent other threads from reading an incorrect timezone from
//		// calls to TimeZone#getDefault()
//		String timeString;
//		synchronized (TimeZone.class) {
//			TimeZone.setDefault(TimeZone.getTimeZone(mTimezone));
//			timeString = DateUtils.formatDateTime(mActivity, millis, flags);
//			TimeZone.setDefault(null);
//		}
		view.setText(DateUtils.formatDateTime(this, millis, flags));
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
					case TaskStatus.NEW_TASK:
						getContentResolver().insert(
								Task.Schema.CONTENT_URI, 
								task.toContentValues());
						break;
					case TaskStatus.EDIT_TASK:
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
		outState.putSerializable(TASK_OBJ, task);
	}
}