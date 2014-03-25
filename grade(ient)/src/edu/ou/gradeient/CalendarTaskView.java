package edu.ou.gradeient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class CalendarTaskView extends View {

	Paint daySeparatorPaint;
	Paint dayTextPaint;
	
	public CalendarTaskView(Context context) {
		super(context);
		initializePaints();
	}
	public CalendarTaskView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializePaints();
	}
	public CalendarTaskView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initializePaints();
	}
	private void initializePaints()
	{	
		daySeparatorPaint = new Paint ();
		daySeparatorPaint.setARGB(255,  100,  100,  100);
		daySeparatorPaint.setStyle(Paint.Style.FILL);
		
		dayTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dayTextPaint.setTypeface(Typeface.DEFAULT);
		dayTextPaint.setTextSize(50);
		dayTextPaint.setARGB(255,  100,  100,  100);	
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		String [] days = new String [] {"22", "23", "24", "25", "26", "27"};
		drawBackground(days, canvas);
	}
	
	private void drawBackground(String[] days, Canvas canvas)
	{
		float height = canvas.getHeight();
		float dayHeight = height/days.length;
		int currentDay = 0;
		int width = canvas.getWidth();
		while (currentDay < days.length)
		{
			//Draw separators
			canvas.drawRect(0,  dayHeight*currentDay,  width,  
					dayHeight*currentDay+5, daySeparatorPaint);
			//Draw text for each day
			canvas.drawText(days[currentDay] , 5,  
					dayHeight*currentDay+50, dayTextPaint);
			++currentDay;
		}
	}
	
}
