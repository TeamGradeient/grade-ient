package edu.ou.gradeient;

import java.util.TreeMap;

import org.joda.time.DateTime;

public class TaskModel 
{
	/**TreeMap with key of due date and value of Task**/
	TreeMap<DateTime, Task> taskList = new TreeMap<DateTime, Task>();
	
	/**
	 * Adds a new task to the model.
	 * @param newTask The task to be added
	 */
	public void addTask (Task newTask)
	{
		taskList.put(newTask.getTaskInterval().getEnd(), newTask);
		System.out.println("A new task has been added.");
	}

}
