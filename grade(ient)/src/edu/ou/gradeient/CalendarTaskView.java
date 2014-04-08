package edu.ou.gradeient;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarTaskView extends View {
	
	/**Minimum height of a day*/
	final static int DAY_HEIGHT = 200;
	/**Distance between left edge of screen and the leftmost task*/
	final static float DISTANCE_FROM_EDGE = 50;
	
	Paint daySeparatorPaint;
	Paint dayNameTextPaint;
	Paint dayNumberTextPaint;
	Paint currentTimeBarPaint;
	
	DisplayMetrics displaymetrics = new DisplayMetrics();
	
	int absoluteHeight;
	int absoluteWidth;
	int visibleHeight;
	int actionBarHeight;
	
	float dayHeight;

	Calendar startDate = new GregorianCalendar();
	Calendar endDate = new GregorianCalendar();
	
	float startTime;
	float endTime;
	float currentTime = System.currentTimeMillis();
	float timeInterval;
	float timeSinceStart;

	public CalendarTaskView(Context context) {
		super(context);
		init();
	}
	public CalendarTaskView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}
	public CalendarTaskView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init(AttributeSet attrs)
	{
		initializePaints();
		setDisplayDates();

		startTime = startDate.getTimeInMillis();
		endTime = endDate.getTimeInMillis();
		timeInterval= endTime-startTime;
		
		((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		absoluteHeight = displaymetrics.heightPixels;
		absoluteWidth = displaymetrics.widthPixels;
		setVisibleHeight();
	}
	
	private void init()
	{
		initializePaints();
		setDisplayDates();

		startTime = startDate.getTimeInMillis();
		endTime = endDate.getTimeInMillis();
		timeInterval= endTime-startTime;
		
		((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		absoluteHeight = displaymetrics.heightPixels;
		absoluteWidth = displaymetrics.widthPixels;
		setVisibleHeight();
	}
	
	private void drawTask(Canvas canvas, int numberOfTasks)
	{
		
	}
	
	/**Finds the time represented by a given y-position on the screen*/
	private float findTimeFromYPosition(float yPosition)
	{
		//If the pixel is not on the screen, return -1.
		//This should never happen. 
		if (yPosition < 0 || yPosition > visibleHeight) {
			return -1;
		}
		//otherwise, calculate and return the time in milliseconds
		return yPosition*timeInterval/getHeight() - startTime;
	}
	
	/**Finds the y-position on the screen for a given time.*/
	private float findYPosition(float milliseconds)
	{
		//If time is outside the interval between start
		//and end times, return -1 
		if (milliseconds < startTime || milliseconds > endTime) {
			return -1;
		}
		//Otherwise, calculate and return the position on the screen
		return (milliseconds - startTime)*getHeight()/timeInterval;
	}
	
	/**Probably not going to use this method*/
	public void populateLayoutWithTasks(LinearLayout ll)
	{
		TextView tv = new TextView(this.getContext());
		tv.setText("Test");
		ll.addView(tv);
		TextView tv2 = new TextView(this.getContext());
		tv2.setText("another test!!");
		ll.addView(tv2);
	}
	
	private void setVisibleHeight()
	{
		TypedValue tv = new TypedValue();
		this.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
		int actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);
		actionBarHeight *= 2;
		visibleHeight = absoluteHeight - actionBarHeight;
	}
	
	/**This is just a method to set up dates to make testing easier*/
	private void setDisplayDates()
	{
		startDate.set(Calendar.MILLISECOND, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.HOUR_OF_DAY, 0);
		
		endDate.set(Calendar.MILLISECOND, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.HOUR_OF_DAY, 0);

		startDate.add(Calendar.DAY_OF_YEAR,  0);
		endDate.add(Calendar.DAY_OF_YEAR, 4); 
	}
	
	private void initializePaints()
	{	
		Typeface roboto = Typeface.createFromAsset(this.getContext().getAssets(), 
				"Roboto-Regular.ttf");
		
		daySeparatorPaint = new Paint ();
		daySeparatorPaint.setARGB(255,  100,  100,  100);
		daySeparatorPaint.setStyle(Paint.Style.FILL);

		dayNameTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dayNameTextPaint.setTypeface(roboto);
		dayNameTextPaint.setTextSize(30);
		dayNameTextPaint.setARGB(255,  100,  100,  100);	
		
		dayNumberTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dayNumberTextPaint.setTypeface(roboto);
		dayNumberTextPaint.setTextSize(50);
		dayNumberTextPaint.setARGB(255,  100,  100,  100);	
		
		currentTimeBarPaint = new Paint();
		currentTimeBarPaint.setARGB(150, 200, 0, 0);
		currentTimeBarPaint.setStyle(Paint.Style.FILL);
	}

	
	/**Rough implementation of onMeasure method*/
	@Override
	public void onMeasure(int x, int y)
	{	
		setVisibleHeight();
		float numberOfDays = timeInterval/86400000;
		if (DAY_HEIGHT*numberOfDays < visibleHeight) {
			setMeasuredDimension(MeasureSpec.getSize(x), visibleHeight);
		}
		else {
			setMeasuredDimension(MeasureSpec.getSize(x), (int) (numberOfDays*DAY_HEIGHT));	
		}
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		drawBackground(canvas);
		drawTasks(canvas);
		drawTimeBar(canvas);
	}
	
	private void drawTimeBar(Canvas canvas)
	{	
		if (currentTime < endTime && currentTime > startTime)
		{
			timeSinceStart = currentTime-startTime;
			canvas.drawRect(0,  timeSinceStart/timeInterval*canvas.getHeight(), 
					canvas.getWidth(), 
					timeSinceStart/timeInterval*canvas.getHeight() + 10, 
					currentTimeBarPaint);
			return;
		}
		System.out.println("Current time not within interval of display.");
		return;
	}
	
	private void drawTasks(Canvas canvas)
	{
		
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
