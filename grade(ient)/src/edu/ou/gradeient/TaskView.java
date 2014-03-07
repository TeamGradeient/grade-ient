package edu.ou.gradeient;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ListFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;

public class TaskView extends ListActivity {
	
	private static final String TAG = "edu.ou.gradeient.TaskView";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Task[] tasks = MainActivity.getModel().getTaskList();
		for (int i = 0; i < tasks.length; ++i)
		{
			System.out.println(tasks[i].getName());
		}
		
		setContentView(R.layout.activity_task_view);
		// Show the Up button in the action bar.
		setupActionBar();
		
		//Set up an ArrayAdapter with data from model.
		//This adapter contains the names of the tasks.
		ArrayAdapter<Task> arrayAdapter = new ArrayAdapter<Task>(this,
				android.R.layout.simple_list_item_2, android.R.id.text1, 
				MainActivity.getModel().getTaskList()) {
					  @Override
					  public View getView(int position, View convertView, ViewGroup parent) {
					    View view = super.getView(position, convertView, parent);
					    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
					    TextView text2 = (TextView) view.findViewById(android.R.id.text2);

					    text1.setText(MainActivity.getModel().getTaskList()[position].getName());
					    text2.setText(MainActivity.getModel().getTaskDueDates()[position]);
					    return view;
					  }
					};
		
		setListAdapter(arrayAdapter);
		//setListAdapter(taskDateAdapter);
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

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
		}
		return super.onOptionsItemSelected(item);
	}

}
