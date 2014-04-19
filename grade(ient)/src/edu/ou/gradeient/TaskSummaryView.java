package edu.ou.gradeient;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import edu.ou.gradeient.EditTaskActivity.Extras;

public class TaskSummaryView extends Activity {

	private final String TAG = "TaskSummaryView";
	private Task task;
	
	private static final String TASK_OBJ = "edu.ou.gradient.TASK_OBJ";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_summary_view);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (savedInstanceState == null) {
			setTaskFromIntent();
			
		}
		
		else {
			try {
				task =(Task)savedInstanceState.getSerializable(TASK_OBJ);
			} catch (Exception ex) {
				Log.w(TAG, "Task could not be de-serialized.");
				throw new IllegalArgumentException("Task could not be de-serialized.");
			}
		}
		
		TextView taskName = (TextView)findViewById(R.id.task_name);
		TextView subjectName = (TextView)findViewById(R.id.subject_name);
		TextView dueDate = (TextView)findViewById(R.id.due_time);
		
		taskName.setText(task.getName());
		subjectName.setText(task.getSubject());
		dueDate.setText("Due Time Goes Here");
		
		
	}

		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_summary_view, menu);
	
		return true;
	}
	
	private void setTaskFromIntent() {
		

		Bundle extras = getIntent().getExtras();
		
		if (extras == null) {
			
			throw new IllegalArgumentException("Must Specify Task ID");
			
		}
		
		long taskId = extras.getLong(Extras.TASK_ID, -1);
		
		if (taskId == -1) {
			
			throw new IllegalArgumentException("Must Specify a Task ID");
		}
		
		Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(Task.Schema.CONTENT_URI, taskId), null, null, null, null);
		
		try {
			if (cursor.getCount() > 0) {
				cursor.moveToNext();task = new Task(cursor);
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error reading task from database", ex);
			
			throw new IllegalArgumentException("Couldn't find requested Task ID");
			
		} finally {
			cursor.close();
		}
		
		if (task == null) {
			
			Log.w(TAG, "Couldn't find requested task ID" + taskId);
			
			throw new IllegalArgumentException("Couldn't find requested Task ID");
		}
	
			
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			
			return true;
			
		case R.id.action_discard://Show pop-up dialogue here. Ask if they want to delete.
			//If they say yes:
			getContentResolver().delete(ContentUris.withAppendedId(Task.Schema.CONTENT_URI, task.getId()), null, null);
			
			finish();
			
			return true;
			
		case R.id.action_edit:
	
		}
		
		
			
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(TASK_OBJ, task);
	}
	
}
