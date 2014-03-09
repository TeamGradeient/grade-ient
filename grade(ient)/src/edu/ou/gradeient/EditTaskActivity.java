package edu.ou.gradeient;

/*
 * Currently this is sort of a horrible mess sort of based off of
 * com.android.calendar.event.EditEventView. It will get better!!
 */

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog; //TODO later, change to radial version?
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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

public class EditTaskActivity extends Activity {
	private static final String TAG = "EditTaskActivity";
	
	// This is so we can reference the activity from inner classes
	private Activity self = this;
	
	private TextView nameText;
	private TextView subjectText;
	private TextView notesText;
	private CheckBox doneCheckBox;
	private Button startDateButton;
	private Button dueDateButton;
	private Button startTimeButton;
	private Button dueTimeButton;

	private TimePickerDialog startTimePickerDialog;
	private TimePickerDialog dueTimePickerDialog;
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
					startTimePickerDialog = new TimePickerDialog(self, 
							new TimeListener(v), start.getHourOfDay(), 
							start.getMinuteOfHour(), 
							DateFormat.is24HourFormat(self));
				} else {
					startTimePickerDialog.updateTime(start.getHourOfDay(),
							start.getMinuteOfHour());
				}
				dialog = startTimePickerDialog;
			} else {
				DateTime due = task.getEnd();
				if (dueTimePickerDialog == null) {
					dueTimePickerDialog = new TimePickerDialog(self,
							new TimeListener(v), due.getHourOfDay(), 
							due.getMinuteOfHour(),
							DateFormat.is24HourFormat(self));
				} else {
					dueTimePickerDialog.updateTime(due.getHourOfDay(), 
							due.getMinuteOfHour());
				}
				dialog = dueTimePickerDialog;
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
			if (this.view == startDateButton)
				task.setStartDate(year, month, day, true);
			else
				task.setEndDate(year, month, day);
			updateTimeDateButtons();
		}
	}
	
	private class DateClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "DateClickListener! " + v);
			//TODO Android Cal version had a check for if activity is paused--
			//necessary?
			//TODO why does Android Cal implement this differently from
			// the TimeClickListener?
			
			if (datePickerDialog != null)
				datePickerDialog.dismiss();
			DateTime time;
			if (v == startDateButton) 
				time = task.getStart();
			else
				time = task.getEnd();
			datePickerDialog = new DatePickerDialog(self, new DateListener(v), 
					time.getYear(), time.getMonthOfYear(), time.getDayOfMonth());
			//TODO make sure that this works and we don't need to use a 
			// fragment or any sort of fancy management stuff
			datePickerDialog.show();
		}
	}
	
	/*
	 * TODO Android Cal has something interesting for showing recurrence picker
	 * dialog in onClick
	 * 
	 * fillModelFromUI - updates CalendarEventModel from UI elements, 
	 * including interacting with ContentProvider
	 * 
	 * constructor arguments of unknown purpose: view, done,
	 * timeSelectedWasStartTime, dateSelectedWasStartDate
	 * (it also sets up time/date picker dialogs, apparently)
	 */

	private void updateTimeDateButtons() {
		long startMillis = task.getStartMillis();
		long endMillis = task.getEndMillis();
		setDate(startDateButton, startMillis);
		setTime(startTimeButton, startMillis);
		setDate(dueDateButton, endMillis);
		setTime(dueTimeButton, endMillis);
	}
	
	private void setDate(TextView view, long millis) {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH
                | DateUtils.FORMAT_ABBREV_WEEKDAY;

        //TODO what to do with this?
//        // Unfortunately, DateUtils doesn't support a timezone other than the
//        // default timezone provided by the system, so we have this ugly hack
//        // here to trick it into formatting our time correctly. In order to
//        // prevent all sorts of craziness, we synchronize on the TimeZone class
//        // to prevent other threads from reading an incorrect timezone from
//        // calls to TimeZone#getDefault()
//        String dateString;
//        synchronized (TimeZone.class) {
//            TimeZone.setDefault(TimeZone.getTimeZone(mTimezone));
//            dateString = DateUtils.formatDateTime(mActivity, millis, flags);
//            // setting the default back to null restores the correct behavior
//            TimeZone.setDefault(null);
//        }
        view.setText(DateUtils.formatDateTime(this, millis, flags));
    }
	
    private void setTime(TextView view, long millis) {
        int flags = DateUtils.FORMAT_SHOW_TIME;
        flags |= DateUtils.FORMAT_CAP_NOON_MIDNIGHT;
        if (DateFormat.is24HourFormat(this)) 
            flags |= DateUtils.FORMAT_24HOUR;

        //TODO what to do with this?
//        // Unfortunately, DateUtils doesn't support a timezone other than the
//        // default timezone provided by the system, so we have this ugly hack
//        // here to trick it into formatting our time correctly. In order to
//        // prevent all sorts of craziness, we synchronize on the TimeZone class
//        // to prevent other threads from reading an incorrect timezone from
//        // calls to TimeZone#getDefault()
//        String timeString;
//        synchronized (TimeZone.class) {
//            TimeZone.setDefault(TimeZone.getTimeZone(mTimezone));
//            timeString = DateUtils.formatDateTime(mActivity, millis, flags);
//            TimeZone.setDefault(null);
//        }
        view.setText(DateUtils.formatDateTime(this, millis, flags));
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
		dueDateButton = (Button)findViewById(R.id.due_date);
		startTimeButton = (Button)findViewById(R.id.start_time);
		dueTimeButton = (Button)findViewById(R.id.due_time);
		
		// Figure out if the user requested to add or edit a task
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			taskStatus = TaskStatus.NEW_TASK;
		} else {
			// Get the new/edit status, if specified
			Object status = extras.get(Extras.TASK_STATUS);
			taskStatus = status == null ? TaskStatus.NEW_TASK : (Integer)status;
			// Get the ID, if specified.
			Object id = extras.get(Extras.TASK_ID);
			long taskId = id == null ? Task.NEW_TASK_ID : (Long)id;
			
			// If we're supposed to edit a task, get its Task object by ID.
			if (taskStatus == TaskStatus.EDIT_TASK) {
				if (taskId == Task.NEW_TASK_ID) {
					Log.e(TAG, "Requested editing a task with ID NEW_TASK_ID.");
				} else {
					// Try to get the task with the given ID.
					task = GradeientApp.getModel().getTask(taskId);
					// This is bad but not fatal. Create a new task instead.
					if (task == null) {
						Log.w(TAG, "Couldn't find requested task ID " + taskId);
						taskStatus = TaskStatus.NEW_TASK;
					}
				}
			}
		}
		// If we're supposed to create a task (or something went wrong with
		// finding the task to edit), make a new Task object.
		if (task == null) {
			DateTime start = new DateTime();
			DateTime end = new DateTime(start).plusDays(1);
			task = new Task("", start.getMillis(), end.getMillis());
		}
		updateTimeDateButtons();
		startDateButton.setOnClickListener(new DateClickListener());
		dueDateButton.setOnClickListener(new DateClickListener());
		startTimeButton.setOnClickListener(new TimeClickListener());
		dueTimeButton.setOnClickListener(new TimeClickListener());
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
		Log.d(TAG, "onOptionsItemSelected called");
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
				// the object in the model.
				fillTaskNonDateFields();
				switch (taskStatus) {
					case TaskStatus.NEW_TASK:
						GradeientApp.getModel().addTask(task);
						break;
					case TaskStatus.EDIT_TASK:
						GradeientApp.getModel().updateTask(task);
						break;
					default:
						Log.wtf(TAG, "Unknown task status: " + taskStatus);
				}
				setResult(RESULT_OK, new Intent());
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
