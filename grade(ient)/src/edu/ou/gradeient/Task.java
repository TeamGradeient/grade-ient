package edu.ou.gradeient;

public class Task 
{
	/**A String containing the name of this task*/
	private String name;
	
	/**A reference to the subject with which this task is associated.*/
	private Subject subject;
	
	/**A string containing notes about this task*/
	private String notes;
	
	/**True if the task is done, false otherwise.*/
	private boolean isDone;
	
	/**Comparator to compare two tasks by their due dates*/
	public final CompareTasksByDate BY_DUE_DATE = new CompareTasksByDate();
	
	/**No-argument constructor. Creates a default task with null values for all fields.*/
	public Task ()
	{
		isDone = false;
	}
	
	/**
	 * Creates a default task with the name given.
	 * @param name The name of the task
	 */
	public Task (String name)
	{
		this.name = name;
		isDone = false;
	}
	
	/**
	 * Returns the name of this task
	 * @return The name of this task
	 */
	public String getName ()
	{
		return name;
	}
	
	/**
	 * Returns the subject of this task
	 * @return A reference to the subject of this task, or null if no subject is associated with the task
	 */
	public Subject getSubject ()
	{
		return subject;
	}
	
	/**
	 * Returns true if this task is done, and false otherwise
	 * @return True if this task is done, and false otherwise
	 */
	public boolean getIsDone ()
	{
		return isDone;
	}
	
	public void setName (String newName)
	{
		name = newName;
	}
	
	public void setSubject (Subject newSubject)
	{
		subject = newSubject;
	}
	
	public void setIsDone (boolean newIsDone)
	{
		isDone = newIsDone;
	}
	
}
