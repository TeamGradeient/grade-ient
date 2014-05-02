package edu.ou.gradeient;

import org.joda.time.DateTime;

import android.app.Activity;
import android.app.FragmentManager;
import android.view.View;
import android.widget.Button;

import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.date.DatePickerDialog.OnDateSetListener;

import edu.ou.gradeient.data.DateTimeInterval;
import edu.ou.gradeient.data.TimeUtils;

/** This class is used to update the date buttons. 
 * (sort of from Android Calendar's com.android.calendar.event.EditEventView) */
public class DateClickListener implements View.OnClickListener {
    private static final String FRAG_TAG_DATE_PICKER = "datePickerDialogFragment";
	private Activity activity;
	private DateTimeInterval interval;
	private DatePickerDialog datePickerDialog;
	
	private Button startDateButton;
	private Button endDateButton;
	private Button startTimeButton;
	private Button endTimeButton;

	private class DateListener implements OnDateSetListener {
		private View view;

		public DateListener(View view) {
			this.view = view;
		}

		@Override
		public void onDateSet(DatePickerDialog view, int year, int month, int day) {
			// For some reason, Android's date pickers return months in range
			// 0 to 11, but joda-time (understandably) disapproves...
			if (this.view == startDateButton)
				interval.setStartDate(year, month + 1, day, true);
			else
				interval.setEndDate(year, month + 1, day);
			
			TimeUtils.setDateText(interval.getInterval(), startTimeButton, 
					startDateButton, endTimeButton, endDateButton);
		}
	}
	
	public DateClickListener(Activity activity, DateTimeInterval interval) {
		this.activity = activity;
		this.interval = interval;
		startDateButton = (Button)activity.findViewById(R.id.start_date);
		endDateButton = (Button)activity.findViewById(R.id.end_date);
		startTimeButton = (Button)activity.findViewById(R.id.start_time);
		endTimeButton = (Button)activity.findViewById(R.id.end_time);
	}
	
	@Override
	public void onClick(View v) {
		//TODO why does Android Cal implement this differently from
		// the TimeClickListener?
		
		if (datePickerDialog != null)
			datePickerDialog.dismiss();
		DateTime time;
		if (v == startDateButton) 
			time = interval.getStart();
		else
			time = interval.getEnd();
		if (datePickerDialog != null)
			datePickerDialog.dismiss();
		
		// Note that joda-time 1-12 months have to be adjusted to Android
		// date picker 0-11 months...
		datePickerDialog = DatePickerDialog.newInstance(
				new DateListener(v), time.getYear(), 
				time.getMonthOfYear() - 1,
				time.getDayOfMonth());
		datePickerDialog.setYearRange(1970, 2036);
		
		FragmentManager fm = activity.getFragmentManager();
		fm.executePendingTransactions();
		if (!datePickerDialog.isAdded())
			datePickerDialog.show(fm, FRAG_TAG_DATE_PICKER); 
	}
}
