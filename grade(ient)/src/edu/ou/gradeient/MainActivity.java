package edu.ou.gradeient;

import edu.ou.gradeient.db.Database;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	private static String TAG = "edu.ou.gradeient.MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new Database(this).getWritableDatabase();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void showTaskView (View view)
	{
		//Do something in response to button being pressed
		Log.d(TAG, "The task view button was pressed.");
		
		Intent intent = new Intent (this, TaskView.class);
		startActivity(intent);
	}
	
	public void showEditTaskView(View view) {
		Log.d(TAG, "The add task button was pressed.");
		Intent intent = new Intent(this, EditTaskActivity.class);
		startActivity(intent);
	}

}
