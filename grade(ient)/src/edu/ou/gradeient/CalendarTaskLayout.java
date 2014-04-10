package edu.ou.gradeient;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CalendarTaskLayout extends LinearLayout {

	public CalendarTaskLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		int childCount = this.getChildCount();
		int currentChild = 0;
		while(childCount > currentChild)
		{
			this.getChildAt(currentChild);
		}
		super.onDraw(canvas);
	}
	
}
