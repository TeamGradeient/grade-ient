package edu.ou.gradeient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

/**
 * Entry point activity for the app
 */
public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void showTaskView (View view)
	{
		startActivity(new Intent (this, TaskListActivity.class));
	}
	
	public void showEditTaskView(View view) {
		startActivity(new Intent(this, EditTaskActivity.class));
	}
	
	public void showCalendarView(View view) {
		startActivity(new Intent (this, CalendarActivity.class));
	}
}
