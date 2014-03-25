package edu.ou.gradeient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Typeface;

public class CalendarTaskView extends View {
	
	Paint myPaint;
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
		myPaint = new Paint (Paint.ANTI_ALIAS_FLAG);
		myPaint.setARGB(255,  25,  100,  30);
		myPaint.setStyle(Paint.Style.FILL);
		
		daySeparatorPaint = new Paint ();
		daySeparatorPaint.setARGB(255,  0,  0,  255);
		daySeparatorPaint.setStyle(Paint.Style.FILL);
		
		dayTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//dayTextPaint.setTypeface(Typeface.DEFAULT);
		dayTextPaint.setTextSize(50);
		dayTextPaint.setARGB(255,  0,  0,  0);
		
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		String [] days = new String [] {"22", "23", "24", "25", "26", "27"};
		drawDaySeparators(days, canvas);
	}
	
	private void drawDaySeparators(String[] days, Canvas canvas)
	{
		float height = canvas.getHeight();
		float dayHeight = height/days.length;
		int currentDay = 0;
		int width = canvas.getWidth();
		while (currentDay < days.length)
		{
			canvas.drawLine(0, dayHeight*currentDay ,  width,  
					dayHeight*currentDay, daySeparatorPaint);
			canvas.drawText(days[currentDay] , 5,  
					dayHeight*currentDay+50, dayTextPaint);
			++currentDay;
		}
	}
	
	private void drawDayText(int[] days, Canvas canvas)
	{
		
	}
}
