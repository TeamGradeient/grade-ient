package edu.ou.gradeient;

import java.util.ArrayList;

import android.util.Log;

public class TaskModel 
{
	private static final String TAG = "edu.ou.gradeient.TaskModel";
	
	/**ArrayList to store Task objects**/
	ArrayList<Task> taskList = new ArrayList<Task>();
	
	/**
	 * Adds a new task to the model.
	 * @param newTask The task to be added
	 */
	public void addTask (Task newTask)
	{
		taskList.add(newTask);
		Log.d(TAG, "A new task has been added.");
	}
	
	/**
	 * Removes a task from the model.
	 * @param toDelete The task to be deleted
	 * @return True if the task was deleted, false 
	 * if the task did not exist in the model. 
	 */
	public boolean removeTask (Task toDelete)
	{
		return taskList.remove(toDelete);
	}
	
	public Task[] getTaskList ()
	{
		Task [] list = new Task [1];
		return taskList.toArray(list);
	}
	
	/**
	 * Finds the task at a certain index and marks it as complete
	 * or incomplete.
	 * @throws IllegalArgumentException if the index given is outside of the
	 * range of the list of tasks.
	 * @param taskIndex The index of the task
	 * @param isDone
	 */
	public void setIsDone (int taskIndex, boolean isDone)
	{
		if (taskIndex >= taskList.size())
		{
			throw new IllegalArgumentException("The index must be within "
					+ "the size of the list of tasks.");
		}
		taskList.get(taskIndex).setIsDone(isDone);
		Log.d(TAG, "Task completion set as " + isDone);
	}

}
