package edu.ou.gradeient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;

public class TaskModel implements Serializable
{
	private static final long serialVersionUID = 6994739496300531572L;

	private static final String TAG = "TaskModel";
	
	/**ArrayList to store Task objects**/
	private ArrayList<Task> taskList;
	
	private transient boolean writeAfterUpdate;
	
	/**
	 * Make a new TaskModel, setting writeAfterUpdate to false.
	 */
	public TaskModel()
	{
		this(false);
	}
	
	/**
	 * Make a new TaskModel.
	 * @param writeAfterUpdate true if changes should be serialized to the 
	 * model file after each update
	 */
	public TaskModel(boolean writeAfterUpdate) {
		taskList = new ArrayList<Task>();
		this.writeAfterUpdate = writeAfterUpdate;
	}
	
	/**
	 * Adds a new task to the model.
	 * @param newTask The task to be added
	 */
	public void addTask (Task newTask) {
		taskList.add(newTask);
		Log.d(TAG, "A new task has been added.");
		// For now, write the file on every change. (TODO take out later)
		if (writeAfterUpdate)
			writeFile(GradeientApp.getAppContext(), GradeientApp.MODEL_FILE);
	}
	
	public void setWriteAfterUpdate(boolean writeAfterUpdate) {
		this.writeAfterUpdate = writeAfterUpdate;
	}
	
	/**
	 * Update the given task's entry in the model. Matching is done by ID.
	 * If the task is not found, it will be added (but an error message will
	 * be logged).
	 * @param task The task to update
	 */
	public void updateTask (Task task) {
		long taskId = task.getId();
		boolean matched = false;
		// Find the task with the matching ID and replace it.
		for (int i = 0; i < taskList.size(); ++i) {
			if (taskList.get(i).getId() == taskId) {
				taskList.set(i, task);
				matched = true;
				break;
			}
		}
		if (!matched) {
			Log.w(TAG, "Non-existant task updated: name = " + task.getName()
					+ ", id = " + taskId);
			taskList.add(task);
		}
		// For now, write the file on every change. TODO take out later
		if (writeAfterUpdate)
			writeFile(GradeientApp.getAppContext(), GradeientApp.MODEL_FILE);
	}
	
	/**
	 * Removes a task from the model.
	 * @param toDelete The task to be deleted
	 * @return True if the task was deleted, false 
	 * if the task did not exist in the model. 
	 */
	public boolean removeTask (Task toDelete) {
		if (taskList.contains(toDelete))
			Log.d(TAG, "Task deleted.");
		else
			Log.d(TAG, "Task does not exist in the list.");
		boolean ret = taskList.remove(toDelete);
		// For now, write the file on every change. TODO take out later
		if (writeAfterUpdate)
			writeFile(GradeientApp.getAppContext(), GradeientApp.MODEL_FILE);
		return ret;
	}
	
	/**
	 * Gets a task by ID. Returns null if the ID isn't found.
	 * @param id The task ID to look up
	 * @return The task with the given ID, or null
	 */
	public Task getTask(long id) {
		for (Task t : taskList) {
			if (t.getId() == id)
				return t;
		}
		return null;
	}
	
	public Task getTaskAtIndex(int i) {
		return taskList.get(i);
	}
	
	public Task[] getTaskArray () {
		Task [] list = new Task [1];
		return taskList.toArray(list);
	}
	
	ArrayList<Task> getTaskList() {
		return taskList;
	}
	
	public String getTaskDueDateAtIndex(int i) {
		//TODO This is from EditEventActivity in Android Calendar.
		// They seemed to think there could be timezone-related display issues.
		// Figure out if that's true, and if so, handle it.
		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
				| DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY
				| DateUtils.FORMAT_SHOW_TIME;
		Context context = GradeientApp.getAppContext();
		if (DateFormat.is24HourFormat(context))
			flags |= DateUtils.FORMAT_24HOUR;
		return DateUtils.formatDateTime(context,
				taskList.get(i).getEndMillis(), flags);
	}
	
	public String[] getTaskDueDates(){
		String[] dueDates = new String[taskList.size()];
		//TODO This is from EditEventActivity in Android Calendar.
		// They seemed to think there could be timezone-related display issues.
		// Figure out if that's true, and if so, handle it.
		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
				| DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY
				| DateUtils.FORMAT_SHOW_TIME;
		Context context = GradeientApp.getAppContext();
		if (DateFormat.is24HourFormat(context))
			flags |= DateUtils.FORMAT_24HOUR;
		for (int i = 0; i < taskList.size(); ++i){
			dueDates[i] = DateUtils.formatDateTime(context, 
					taskList.get(i).getEndMillis(), flags);
		}
		return dueDates;
	}
	
	/**
	 * Finds the task at a certain index and marks it as complete
	 * or incomplete.
	 * @throws IllegalArgumentException if the index given is outside of the
	 * range of the list of tasks.
	 * @param taskIndex The index of the task
	 * @param isDone
	 */
	public void setIsDone (int taskIndex, boolean isDone) {
		if (taskIndex >= taskList.size()) {
			throw new IllegalArgumentException("The index must be within "
					+ "the size of the list of tasks.");
		}
		taskList.get(taskIndex).setIsDone(isDone);
		// For now, write the file on every change.
		// TODO take out later
		if (writeAfterUpdate)
			writeFile(GradeientApp.getAppContext(), GradeientApp.MODEL_FILE);
	}
	
	public void writeFile (Context context, String filename) {
		FileOutputStream fileOut = null;
		ObjectOutputStream objOut = null;
		try {
		     fileOut = context.openFileOutput(filename, Context.MODE_PRIVATE);
		     objOut = new ObjectOutputStream(fileOut);
		     objOut.writeObject(this);
		     Log.d(TAG, "Serialized data is saved in " + filename);
	    } catch (IOException ex) {
	    	Log.e(TAG, "Error writing model file.", ex);
	    } finally {
	    	try {
	    		if (objOut != null) objOut.close();
	    		if (fileOut != null) fileOut.close();
	    	} catch (IOException ex) {
	    		Log.e(TAG, "Error closing model file after writing.", ex);
	    	}
	    }
	}

	/**
	 * Writes the ArrayList of tasks to an ObjectOutputStream
	 * @param out The ObjectOutputStream to which the data is to be written
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) 
			throws IOException {
		out.writeObject(taskList);
		Log.d(TAG, "Tasks written to file.");
	}
	
	/**
	 * Reads the ArrayList of tasks from an ObjectInputStream
	 * @param in The ObjectInputStream containing the data
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		taskList = (ArrayList<Task>) in.readObject();
		Log.d(TAG, "Tasks read from file.");
	}
	
	public static TaskModel readModelFromFile(Context context, String filename) {
		FileInputStream fileIn = null;
		ObjectInputStream objIn = null;
		TaskModel model = null;
		try {
			// Try to open the model file. If this is the first run of the
			// application, this will throw FileNotFoundException.
			fileIn = context.openFileInput(filename);
			objIn = new ObjectInputStream(fileIn);
			model = (TaskModel)objIn.readObject();
			Log.d(TAG, "Serialized data has been loaded from " + filename);
		} catch (FileNotFoundException ex) {
			Log.d(TAG, "Model file didn't exist. Creating new model...");
		} catch (IOException ex) {
			Log.e(TAG, "Error reading model file.", ex);
		} catch (ClassNotFoundException ex) {
			Log.e(TAG, "Error reading model file.", ex);
		} finally {
			// Free resources properly!
			try {
				if (objIn != null) objIn.close();
				if (fileIn != null) fileIn.close();
			} catch (IOException ex) {
				Log.e(TAG, "Error closing model file after reading.", ex);
			}
		}
		// If there were any errors (or this is the first run), make a new model
		if (model == null)
			model = new TaskModel();
		return model;
	}
}
