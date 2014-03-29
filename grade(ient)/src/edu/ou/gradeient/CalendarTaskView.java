package edu.ou.gradeient;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class CalendarTaskView extends View {
	
	Paint daySeparatorPaint;
	Paint dayNameTextPaint;
	Paint dayNumberTextPaint;

	Calendar startDate = new GregorianCalendar();
	Calendar endDate = new GregorianCalendar();

	public CalendarTaskView(Context context) {
		super(context);
		initializePaints();
		
		startDate.add(Calendar.DAY_OF_YEAR,  3);
		endDate.add(Calendar.DAY_OF_YEAR, 9); 
	}
	public CalendarTaskView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializePaints();
		
		startDate.add(Calendar.DAY_OF_YEAR,  3);
		endDate.add(Calendar.DAY_OF_YEAR, 9); 
	}
	public CalendarTaskView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initializePaints();

		startDate.add(Calendar.DAY_OF_YEAR,  3);
		endDate.add(Calendar.DAY_OF_YEAR, 9); 
	}
	private void initializePaints()
	{	
		daySeparatorPaint = new Paint ();
		daySeparatorPaint.setARGB(255,  100,  100,  100);
		daySeparatorPaint.setStyle(Paint.Style.FILL);

		dayNameTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dayNameTextPaint.setTypeface(Typeface.DEFAULT);
		dayNameTextPaint.setTextSize(30);
		dayNameTextPaint.setARGB(255,  100,  100,  100);	
		
		dayNumberTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dayNumberTextPaint.setTypeface(Typeface.DEFAULT);
		dayNumberTextPaint.setTextSize(50);
		dayNumberTextPaint.setARGB(255,  100,  100,  100);	
	}

	//TODO: Implement onMeasure so that we don't have to set a minimum
	//height for our view. 

	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		drawBackground(canvas);
	}

	//TODO: This definitely won't work if startDate and endDate are in 
	//different years. However, this works as a temporary solution.
	private void drawBackground(Canvas canvas)
	{
		int firstDay = startDate.get(Calendar.DAY_OF_YEAR);
		int currentDay = firstDay;
		int lastDay = endDate.get(Calendar.DAY_OF_YEAR);
		Calendar day = (Calendar) startDate.clone();
		float dayHeight = canvas.getHeight()/(lastDay-currentDay);
		int width = canvas.getWidth();
		while (currentDay < lastDay)
		{
			//Draw separators
			canvas.drawRect(0,  dayHeight*(currentDay - firstDay),  width,  
					dayHeight*(currentDay - firstDay)+5, daySeparatorPaint);
			//Draw text for each day
			canvas.drawText(day.getDisplayName(Calendar.DAY_OF_WEEK, 
												Calendar.SHORT, Locale.US),
					5, dayHeight*(currentDay-firstDay)+30, dayNameTextPaint);
			canvas.drawText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)),
					5, dayHeight*(currentDay-firstDay)+75, dayNumberTextPaint);
			++currentDay;
			day.roll(Calendar.DAY_OF_YEAR,  true);
		}
	}

}
