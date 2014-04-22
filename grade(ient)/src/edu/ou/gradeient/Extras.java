package edu.ou.gradeient;

/** Options for things to pass in bundles */
public interface Extras {
	/** Key to pass task status in bundle 
	 * (value: {@link TaskStatus#NEW_TASK} or {@link TaskStatus#EDIT_TASK}) */
	public static final String TASK_STATUS = "edu.ou.gradeient.TASK_STATUS";
	/** Key to pass task ID in bundle (value: long) */
	public static final String TASK_ID = "edu.ou.gradeient.TASK_ID";
	/** Key to pass serialized task object in bundle (mostly just used for
	 * onSaveInstanceState) */
	public static final String TASK_OBJ = "edu.ou.gradeient.TASK_OBJ";
	/** Key to pass start time (in millis since epoch) in bundle (value: long) */
	public static final String SCROLL_TO = "edu.ou.gradeient.SCROLL_TO";
	
	/** Task status values to be passed in bundle */
	public interface TaskStatus {
		public static final int NEW_TASK = 0;
		public static final int EDIT_TASK = 1;
	}
}