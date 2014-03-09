package edu.ou.gradeient;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class GradeientApp extends Application {
	private static final String TAG = "GradeientApp";
	static final String MODEL_FILE = "model.dat";
	private static GradeientApp instance;

	private TaskModel model;
	
	public static Context getAppContext() {
		return instance.getApplicationContext();
	}
	
	public static TaskModel getModel() {
		return instance.model;
	}
	
	@Override
	public void onCreate() {
		instance = this;
		model = TaskModel.readModelFromFile(this, MODEL_FILE);
		model.setWriteAfterUpdate(true);
		Log.d(TAG, "Application instantiated");
		
//		//Construct an instance of the model
//		model = new TaskModel(this);
//		
//		/*----------------testing for model----------------*/
//		
//		Time today = new Time();
//		today.set(System.currentTimeMillis());
//		Time tomorrow = new Time();
//		tomorrow.set(System.currentTimeMillis()+10000000);
//		//Add some example tasks to the model for testing
//		model.addTask(new Task2("Test task 1", today, tomorrow));
//		model.addTask(new Task2("Test task 2", today, tomorrow));
//		model.addTask(new Task2("Test task 3", today, tomorrow));
//		
//		//Serialize the model and write it to a file on the Android file system
//		//writeModelToFile("testFile");
//		
//		//Set model to null
//		//model = null;
//		
//		//Reads file from Android file system
//		//readModelFromFile("testFile");
//		
//		//Prints names of tasks
//		Task2[] myTasks = model.getTaskList();
//		for (int i = 0; i < myTasks.length; ++i)
//		{
//			System.out.println(myTasks[i].getName());
//		}
//		
//		/*----------------end of testing----------------*/
	}
}
