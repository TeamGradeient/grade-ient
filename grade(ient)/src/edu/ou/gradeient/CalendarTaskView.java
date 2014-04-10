package edu.ou.gradeient;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
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
	final static float DISTANCE_FROM_EDGE = 70;
	/**Half the number of pixels between each task*/
	final static float DISTANCE_BETWEEN_TASKS = 10;
	/**The number of pixels that the rectangles 
	 * of task backgrounds should be rounded*/
	final static float TASK_RECTANGLE_ROUNDING = 15;
	
	Paint daySeparatorPaint;
	Paint dayNameTextPaint;
	Paint dayNumberTextPaint;
	Paint currentTimeBarPaint;
	Paint taskBackgroundPaint;
	
	DisplayMetrics displaymetrics = new DisplayMetrics();
	
	int absoluteHeight;
	int absoluteWidth;
	int visibleHeight;
	int actionBarHeight;
	float usableWidth;
	float dayHeight;

	String taskDueTime;
	
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
		usableWidth = absoluteWidth - DISTANCE_FROM_EDGE;
	}
	
	private void init()
	{
		initializePaints();
		setDisplayDates();

		startTime = startDate.getTimeInMillis();
		endTime = endDate.getTimeInMillis();
		timeInterval= endTime-startTime;
		
		((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		absoluteHeight = displaymetrics.heightPixels;
		absoluteWidth = displaymetrics.widthPixels;
		setVisibleHeight();
		usableWidth = absoluteWidth - DISTANCE_FROM_EDGE;
	}
	
	private void drawTaskBackground(Canvas canvas, int numberOfTasks, 
			int taskNumber, float start, float end)
	{	
		if (start > end) {
			throw new IllegalArgumentException("Start cannot be greater than end.");
		}
		float taskWidth = usableWidth / (float)numberOfTasks;
		float leftEdge = DISTANCE_FROM_EDGE + taskWidth*taskNumber;
		float rightEdge = leftEdge + taskWidth;
		RectF taskBounds = new RectF (leftEdge + DISTANCE_BETWEEN_TASKS,
				findYPosition(start),
				rightEdge - DISTANCE_BETWEEN_TASKS,
				findYPosition(end));
		canvas.drawRoundRect(taskBounds, TASK_RECTANGLE_ROUNDING,  
				TASK_RECTANGLE_ROUNDING,  taskBackgroundPaint);
		drawTaskDueTime(canvas, leftEdge, rightEdge, end);
	}
	
	private void drawTaskDueTime(Canvas canvas, float leftEdge, 
			float rightEdge, float end)
	{
		int flags = DateUtils.FORMAT_SHOW_TIME;
		flags |= DateUtils.FORMAT_CAP_NOON_MIDNIGHT;
		if (DateFormat.is24HourFormat(getContext())) 
			flags |= DateUtils.FORMAT_24HOUR;
		taskDueTime = DateUtils.formatDateTime(getContext(), 
				(long) end, flags);
		canvas.drawText(taskDueTime, leftEdge + DISTANCE_BETWEEN_TASKS,
				findYPosition(end)+30, dayNameTextPaint);
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
		return yPosition*timeInterval/this.getHeight() - startTime;
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
		return (milliseconds - startTime)*this.getHeight()/timeInterval;
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
		actionBarHeight *= 1;
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

		startDate.add(Calendar.DAY_OF_YEAR,  -2);
		endDate.add(Calendar.DAY_OF_YEAR, 20); 
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
		currentTimeBarPaint.setARGB(255, 200, 0, 0);
		currentTimeBarPaint.setStyle(Paint.Style.FILL);
		
		taskBackgroundPaint = new Paint();
		taskBackgroundPaint.setARGB(85, 100, 0, 100);
		taskBackgroundPaint.setStyle(Paint.Style.FILL);
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
		drawTaskBackground(canvas, 4, 0, startTime + (1.5f)*86400000f, startTime + (3f)*86400000f);
		//Draw a task again in the same place to indicate a work time. 
		//drawTaskBackground(canvas, 4, 0, startTime + (2.2f)*86400000f, startTime + (2.5f)*86400000f);
		drawTaskBackground(canvas, 4, 2, startTime + (2.0f)*86400000f, startTime + (3.5f)*86400000f);
		drawTaskBackground(canvas, 4, 3, startTime + (2.5f)*86400000f, startTime + (3.5f)*86400000f);
		drawTaskBackground(canvas, 4, 1, startTime + (0.5f)*86400000f, startTime + (2f)*86400000f);
		drawTimeBar(canvas);
	}
	
	private void drawTimeBar(Canvas canvas)
	{	
		if (currentTime < endTime && currentTime > startTime) {
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
