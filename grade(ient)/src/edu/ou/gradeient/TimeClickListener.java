package edu.ou.gradeient;

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.FragmentManager;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;

import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.android.datetimepicker.time.TimePickerDialog.OnTimeSetListener;

import edu.ou.gradeient.data.DateTimeInterval;

/** This class is used to update the time buttons. 
 * (sort of from Android Calendar's com.android.calendar.event.EditEventView) */
public class TimeClickListener implements View.OnClickListener {
    private static final String FRAG_TAG_TIME_PICKER = "timePickerDialogFragment";
	private Activity activity;
	private DateTimeInterval interval;
	private TimePickerDialog startTimePickerDialog;
	private TimePickerDialog endTimePickerDialog;
	
	private Button startDateButton;
	private Button endDateButton;
	private Button startTimeButton;
	private Button endTimeButton;
	
	private class TimeListener implements OnTimeSetListener {
		private View view;
		public TimeListener(View view) {
			this.view = view;
		}

		@Override
		public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
			if (this.view == startTimeButton)
				interval.setStartTime(hourOfDay, minute, true);
			else
				interval.setEndTime(hourOfDay, minute, true);
			
			TimeUtils.setDateText(interval.getInterval(), startTimeButton, 
					startDateButton, endTimeButton, endDateButton);
		}
	}
	
	public TimeClickListener(Activity activity, DateTimeInterval interval) {
		this.activity = activity;
		this.interval = interval;
		startDateButton = (Button)activity.findViewById(R.id.start_date);
		endDateButton = (Button)activity.findViewById(R.id.end_date);
		startTimeButton = (Button)activity.findViewById(R.id.start_time);
		endTimeButton = (Button)activity.findViewById(R.id.end_time);
	}
	
	@Override
	public void onClick(View v) {
		TimePickerDialog dialog;
		if (v == startTimeButton) {
			DateTime start = interval.getStart();
			if (startTimePickerDialog == null) {
				startTimePickerDialog = TimePickerDialog.newInstance(
						new TimeListener(startTimeButton), 
						start.getHourOfDay(), start.getMinuteOfHour(), 
						DateFormat.is24HourFormat(activity));
			} else {
				startTimePickerDialog.setStartTime(start.getHourOfDay(),
						start.getMinuteOfHour());
			}
			dialog = startTimePickerDialog;
		} else {
			DateTime end = interval.getEnd();
			if (endTimePickerDialog == null) {
				endTimePickerDialog = TimePickerDialog.newInstance(
						new TimeListener(endTimeButton),
						end.getHourOfDay(), end.getMinuteOfHour(),
						DateFormat.is24HourFormat(activity));
			} else {
				endTimePickerDialog.setStartTime(end.getHourOfDay(), 
						end.getMinuteOfHour());
			}
			dialog = endTimePickerDialog;
		}
		FragmentManager fm = activity.getFragmentManager();
		fm.executePendingTransactions();
		if (!dialog.isAdded())
			dialog.show(fm, FRAG_TAG_TIME_PICKER); 
	}
}