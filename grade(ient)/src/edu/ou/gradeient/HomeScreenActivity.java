package edu.ou.gradeient;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HomeScreenActivity extends Activity {

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
		TextView timeAndDay = (TextView) findViewById(R.id.home_time_and_day);
		//int timeFlags = DateUtils.FORMAT_12HOUR;
		int timeFlags = DateUtils.FORMAT_SHOW_TIME;
		timeFlags |= DateUtils.FORMAT_SHOW_WEEKDAY;
		timeAndDay.setText(DateUtils.formatDateTime(this, 
				System.currentTimeMillis(), timeFlags));
		timeAndDay.setTextColor(Color.WHITE);
		
		//Put them together in a linear layout and set the background
		//TODO: replace the background color with a value from a colors
		//attributes XML file
		LinearLayout dateAndTimeLayout = 
				(LinearLayout) findViewById(R.id.home_date_and_time);
		dateAndTimeLayout.setBackgroundColor(Color.rgb(115, 115, 123));
		/*----------end of time and date display----------*/
		
		/*---------------display next task---------------*/
		TextView nextTaskTitle = (TextView) findViewById(R.id.home_next_task_title);
		nextTaskTitle.setText("Next task!");
		nextTaskTitle.setTextColor(Color.WHITE);
		
		LinearLayout nextTaskLayout = 
				(LinearLayout) findViewById(R.id.home_next_task);
		nextTaskLayout.setBackgroundColor(Color.rgb(247, 157, 11));
		
		/*---------------set up calendar button---------------*/
		RelativeLayout calendarButton = (RelativeLayout) 
				findViewById(R.id.home_goto_calendar_button);
		calendarButton.setBackgroundColor(Color.rgb(52, 148, 43));
		TextView calendarButtonLabel = (TextView)
				findViewById(R.id.home_goto_calendar_button_label);
		calendarButtonLabel.setTextColor(Color.WHITE);
		
		
	}

	public void startCalendarActivity(View view)
	{
		startActivity(new Intent (this, CalendarActivity.class));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_screen, menu);
		return true;
	}

}
