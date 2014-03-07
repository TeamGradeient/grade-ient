package edu.ou.gradeient;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog; //TODO later, change to radial version?
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

	private TaskModel model;
	private Task2 task;
	
	private TimePickerDialog startTimePickerDialog;
	private TimePickerDialog dueTimePickerDialog;
	private DatePickerDialog datePickerDialog;
	
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
				task.setStartTime(minute, hourOfDay, true);
			else
				task.setEndTime(minute, hourOfDay, true);
			updateTimeDateButtons(true);
		}
	}
	
	private class TimeClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Log.i(TAG, "TimeClickListener! " + v);
			TimePickerDialog dialog;
			if (v == startTimeButton) {
				Time start = task.getStart();
				if (startTimePickerDialog == null) {
					startTimePickerDialog = new TimePickerDialog(self, 
							new TimeListener(v), start.hour, start.minute, 
							DateFormat.is24HourFormat(self));
				} else {
					startTimePickerDialog.updateTime(start.hour, start.minute);
				}
				dialog = startTimePickerDialog;
			} else {
				Time due = task.getEnd();
				if (dueTimePickerDialog == null) {
					dueTimePickerDialog = new TimePickerDialog(self,
							new TimeListener(v), due.hour, due.minute,
							DateFormat.is24HourFormat(self));
				} else {
					dueTimePickerDialog.updateTime(due.hour, due.minute);
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
				task.setStartDate(day, month, year, true);
			else
				task.setEndDate(day, month, year);
			updateTimeDateButtons(true);
		}
	}
	
	private class DateClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Log.i(TAG, "DateClickListener! " + v);
			//TODO Android Cal version had a check for if activity is paused--
			//necessary?
			//TODO why does Android Cal implement this differently from
			// the TimeClickListener?
			
			if (datePickerDialog != null)
				datePickerDialog.dismiss();
			Time time;
			if (v == startDateButton) 
				time = task.getStart();
			else
				time = task.getEnd();
			datePickerDialog = new DatePickerDialog(self, new DateListener(v), 
					time.year, time.month, time.monthDay);
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
	 * TODO where do we get TaskModel and Task?
	 * 
	 * constructor arguments of unknown purpose: view, done,
	 * timeSelectedWasStartTime, dateSelectedWasStartDate
	 * (it also sets up time/date picker dialogs, apparently)
	 */

	//TODO put this in the actual appropriate place
	public void setTask(Task2 t) {
		task = t;
		//TODO put this in the proper place
		doneCheckBox.setOnCheckedChangeListener(
				new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, 
					boolean isChecked) {
				task.setIsDone(isChecked);
			}
		});
		updateTimeDateButtons(false);
		startDateButton.setOnClickListener(new DateClickListener());
		dueDateButton.setOnClickListener(new DateClickListener());
		startTimeButton.setOnClickListener(new TimeClickListener());
		dueTimeButton.setOnClickListener(new TimeClickListener());
		nameText.setTextKeepState(task.getName());
		subjectText.setTextKeepState(task.getSubject());
		notesText.setTextKeepState(task.getNotes());
	}
	
	private void updateTimeDateButtons(boolean ignoreDst) {
		long startMillis = task.getStartMillis(ignoreDst);
		long endMillis = task.getEndMillis(ignoreDst);
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
		setupActionBar();
		
		//TODO is this the right place for this?
		nameText = (TextView)findViewById(R.id.task_name);
		subjectText = (TextView)findViewById(R.id.subject_name);
		notesText = (TextView)findViewById(R.id.notes);
		doneCheckBox = (CheckBox)findViewById(R.id.is_done);
		startDateButton = (Button)findViewById(R.id.start_date);
		dueDateButton = (Button)findViewById(R.id.due_date);
		startTimeButton = (Button)findViewById(R.id.start_time);
		dueTimeButton = (Button)findViewById(R.id.due_time);
		Time start = new Time();
		start.setToNow();
		Time end = new Time(start);
		end.monthDay += 1;
		end.normalize(true);
		setTask(new Task2("", start, end));
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_task, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}

}
