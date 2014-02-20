package edu.ou.gradeient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new Database(this).getWritableDatabase();
		setContentView(R.layout.activity_main);
		//Hello!
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
		System.out.println("The button was pressed.");
		
		Intent intent = new Intent (this, TaskView.class);
		startActivity(intent);
	}

}
