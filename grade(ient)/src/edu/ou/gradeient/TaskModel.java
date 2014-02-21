package edu.ou.gradeient;

import java.util.ArrayList;

public class TaskModel 
{
	/**ArrayList to store Task objects**/
	ArrayList<Task> taskList = new ArrayList<Task>();
	
	/**
	 * Adds a new task to the model.
	 * @param newTask The task to be added
	 */
	public void addTask (Task newTask)
	{
		taskList.add(newTask);
		System.out.println("A new task has been added.");
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
		Task [] list = new Task [10];
		return taskList.toArray(list);
	}

}
