package edu.ou.gradeient;

import edu.ou.gradeient.db.Database;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import java.io.*;

public class MainActivity extends Activity {
	
	private static final String TAG = "edu.ou.gradeient.MainActivity";
	private static TaskModel model;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new Database(this).getWritableDatabase();
		
		//Construct an instance of the model
		model = new TaskModel();
		
		
		/*----------------testing for model----------------*/
		
		Time today = new Time();
		today.set(System.currentTimeMillis());
		Time tomorrow = new Time();
		tomorrow.set(System.currentTimeMillis()+10000000);
		//Add some example tasks to the model for testing
		model.addTask(new Task2("Test task 1", today, tomorrow));
		model.addTask(new Task2("Test task 2", today, tomorrow));
		model.addTask(new Task2("Test task 3", today, tomorrow));
		
		//Serialize the model and write it to a file on the Android file system
		//writeModelToFile("testFile");
		
		//Set model to null
		//model = null;
		
		//Reads file from Android file system
		//readModelFromFile("testFile");
		
		//Prints names of tasks
		Task2[] myTasks = model.getTaskList();
		for (int i = 0; i < myTasks.length; ++i)
		{
			System.out.println(myTasks[i].getName());
		}
		
		/*----------------end of testing----------------*/
		
		setContentView(R.layout.activity_main);
	}

	public static TaskModel getModel()
	{
		return model;
	}
	
	private void writeModelToFile(String fileName) {
		try {
		     FileOutputStream fileOut = openFileOutput(fileName, MODE_PRIVATE);
		     ObjectOutputStream out = new ObjectOutputStream(fileOut);
		     out.writeObject(model);
		     out.close();
		     fileOut.close();
		     Log.d(TAG, "Serialized data is saved in testFile");
	    } catch(IOException i){
	          i.printStackTrace();
	    }
	}

	private void readModelFromFile(String fileName) {
		try {
		     FileInputStream fileIn = openFileInput(fileName);
		     ObjectInputStream in = new ObjectInputStream(fileIn);
		     model = (TaskModel) in.readObject();
		     in.close();
		     fileIn.close();
		     Log.d(TAG, "Serialized data has been loaded from testFile");
	    } catch(IOException i){
	    	i.printStackTrace();
	    } catch (ClassNotFoundException e){
			e.printStackTrace();
		}
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
		intent.setData(null);
		startActivity(intent);
		
		View myView = getLayoutInflater().inflate(R.layout.activity_task_view, 
				null);
	}
	
	public void showEditTaskView(View view) {
		Log.d(TAG, "The add task button was pressed.");
		Intent intent = new Intent(this, EditTaskActivity.class);
		startActivity(intent);
	}

}
