package edu.ou.gradeient;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TaskItemView extends RelativeLayout
{
	private TextView taskItemName;
	private TextView taskItemDueDate;

	public TaskItemView (Context context) 
	{
		this(context, null, 0);
	}

	public TaskItemView(Context context, AttributeSet attrs) 
	{
		this(context, attrs, 0); 
	}

	public TaskItemView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		taskItemName = (TextView) findViewById(android.R.id.text1);
		taskItemDueDate = (TextView) findViewById(android.R.id.text2);
	}
	
	TextView getTaskNameView ()
	{
		return taskItemName;
	}
	
	TextView getTaskDueDateView()
	{
		return taskItemDueDate;
	}
}
