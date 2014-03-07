package edu.ou.gradeient;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import android.util.Log;

public class TaskModel implements Serializable
{

	private static final long serialVersionUID = 1L;
	private static final String TAG = "edu.ou.gradeient.TaskModel";
	
	/**ArrayList to store Task objects**/
	ArrayList<Task> taskList;
	
	public TaskModel()
	{
		taskList = new ArrayList<Task>();
	}
	
	/**
	 * Adds a new task to the model.
	 * @param newTask The task to be added
	 */
	public void addTask (Task newTask) {
		taskList.add(newTask);
		Log.d(TAG, "A new task has been added.");
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
		return taskList.remove(toDelete);
	}
	
	public Task[] getTaskList () {
		Task [] list = new Task [1];
		return taskList.toArray(list);
	}
	
	public String[] getTaskDueDates(){
		String[] dueDates = new String[taskList.size()];
		for (int i = 0; i < taskList.size(); ++i){
			dueDates[i]=taskList.get(i).getTaskInterval().getEnd().toString();
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
		Log.d(TAG, "Task completion set as " + isDone);
	}

	/**
	 * Writes the ArrayList of tasks to an ObjectOutputStream
	 * @param out The ObjectOutputStream to which the data is to be written
	 * @throws IOException
	 */
	private void writeObject(java.io.ObjectOutputStream out) 
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
	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		taskList = (ArrayList<Task>) in.readObject();
		Log.d(TAG, "Tasks read from file.");
	}
	
}
