package edu.ou.gradeient;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeSet;

import edu.ou.gradeient.data.Task;
import edu.ou.gradeient.data.TaskWorkInterval;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * There is a TreeSet of Tasks called "tasks" as a member.
 * TreeSet means the tasks stay sorted.
 * Get the tasks ending after a certain time by:
 * 		tasks.tailSet(new Task(..., end time in millis, ...))
 * The tasks have their work times pulled too in the init method.
 * The set will get updated whenever the activity gets created/destroyed
 *   (which I think is pretty frequent).
 * If you want them updated on data change, you might be able to add some
 * sort of notification/event listening thing, or an update method 
 * CalendarActivity can call (CalendarTaskView's tasks would have to be
 * static then too).
 */

public class CalendarView extends View {
	private static final String TAG = "CalendarTaskView";
	
	private static final int DAY_MILLIS = 86400000;
	
	private final Typeface roboto = Typeface.createFromAsset(
			getContext().getAssets(), "Roboto-Regular.ttf");
	private final Typeface robotoLight = Typeface.createFromAsset(
			getContext().getAssets(), "Roboto-Light.ttf");
	
	private Paint daySeparatorPaint;
	private Paint dayNameTextPaint;
	private Paint dayNumberTextPaint;
	private Paint currentTimeBarPaint;
	private Paint taskBackgroundPaint;
	
	private DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
	
	private int absoluteHeight = displaymetrics.heightPixels;
	private int absoluteWidth = displaymetrics.widthPixels;
	private int visibleHeight;
	private int usableWidth;
	private float scale = displaymetrics.density;
	/**Minimum height of a day*/ 
	private float dayHeight = 100 * scale;
	/**Distance between left edge of screen and the leftmost task*/
	private float distanceFromEdge = 35 * scale;
	/**Half the number of pixels between each task*/
	private float distanceBetweenTasks = 5 * scale;
	/**The number of pixels that the rectangles 
	 * of task backgrounds should be rounded*/
	private float taskRectangleRounding = 7.5f * scale;

	private String taskDueTime;
	
	private Calendar startDate = new GregorianCalendar();
	private Calendar endDate = new GregorianCalendar();
	
	private long startTime;
	private long endTime;
	private long currentTime = System.currentTimeMillis();
	private long timeInterval;
	private long timeSinceStart;

	// TEMPORARY
	private TreeSet<Task> tasks = new TreeSet<Task>();

	public CalendarView(Context context) {
		super(context);
		init();
	}
	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public CalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init()
	{
		initializePaints();
		setDisplayDates();

		startTime = startDate.getTimeInMillis();
		endTime = endDate.getTimeInMillis();
		timeInterval= endTime-startTime;
		
		setVisibleHeight();
		usableWidth = (int) (absoluteWidth - distanceFromEdge);
		
		// wait for tasks to show up
		try { // yaaaay catching everything
			int waitTime = 0;
			while (CalendarActivity.tasks == null && waitTime < 1000) {
				try {
					Thread.sleep(50);
					waitTime += 50;
				} catch (InterruptedException e) {
				}
			}
			if (waitTime == 1000)
				return;
			CalendarActivity.tasks.moveToFirst();
			// get all tasks, temporarily indexed by ID
			HashMap<Long, Task> taskMap = new HashMap<Long, Task>();
			while (CalendarActivity.tasks != null &&
					CalendarActivity.tasks.moveToNext()) {
				try {
					Task t = new Task(CalendarActivity.tasks);
					taskMap.put(t.getId(), t);
				} catch (Exception ex) {
					Log.w(TAG, "While getting tasks: " + ex);
				}
			}
			// match up work times with tasks
			if (CalendarActivity.workTimes != null)
				CalendarActivity.workTimes.moveToFirst();
			while (CalendarActivity.workTimes != null && 
					CalendarActivity.workTimes.moveToNext()) {
				try {
					TaskWorkInterval twi = 
							new TaskWorkInterval(CalendarActivity.workTimes);
					if (taskMap.containsKey(twi.getTaskId()))
						taskMap.get(twi.getTaskId()).addWorkInterval(twi);
				} catch (Exception ex) {
					Log.w(TAG, "While getting work times: " + ex);
				}
			}
			// add everything to the actual task map
			tasks.addAll(taskMap.values());
		} catch (Exception ex) {
			Log.w(TAG, "While getting tasks: " + ex);
		}
	}
	
	private void drawTask(Canvas canvas, int numberOfTasks, 
			int taskNumber, long start, long end, String taskName)
	{	
		if (start > end) {
			throw new IllegalArgumentException("Start cannot be greater than end.");
		}
		float taskWidth = usableWidth / (float)numberOfTasks;
		float leftEdge = distanceFromEdge + taskWidth*taskNumber;
		float rightEdge = leftEdge + taskWidth;
		RectF taskBounds = new RectF (leftEdge + distanceBetweenTasks,
				findYPosition(start),
				rightEdge - distanceBetweenTasks,
				findYPosition(end));
		canvas.drawRoundRect(taskBounds, taskRectangleRounding,  
				taskRectangleRounding,  taskBackgroundPaint);
		drawTaskDueTime(canvas, leftEdge, rightEdge, end);
	}
	
	private void setTaskTitle(String title, float leftEdge, float rightEdge)
	{
		AbsoluteLayout al = (AbsoluteLayout) findViewById(R.id.task_titles);
		TextView tv = new TextView(al.getContext());
		AbsoluteLayout.LayoutParams parameters = (LayoutParams) al.getLayoutParams();
		parameters.x = (int) leftEdge;
		parameters.y = (int)(50 * scale);
		al.addView(tv, parameters);
	}
	
	private void drawTaskDueTime(Canvas canvas, float leftEdge, 
			float rightEdge, long end)
	{
		int flags = DateUtils.FORMAT_SHOW_TIME;
		flags |= DateUtils.FORMAT_CAP_NOON_MIDNIGHT;
		if (DateFormat.is24HourFormat(getContext())) 
			flags |= DateUtils.FORMAT_24HOUR;
		taskDueTime = DateUtils.formatDateTime(getContext(), 
				(long) end, flags);
		canvas.drawText(taskDueTime, leftEdge + distanceBetweenTasks,
				findYPosition(end)+(15*scale), dayNameTextPaint);
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
	
	public float findYForBeginningOfDay(long milliseconds)
	{
		long dayNumber = (milliseconds-startTime)/DAY_MILLIS;
		return dayNumber*dayHeight; 
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
		daySeparatorPaint = new Paint ();
		daySeparatorPaint.setARGB(255,  100,  100,  100);
		daySeparatorPaint.setStyle(Paint.Style.FILL);

		dayNameTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dayNameTextPaint.setTypeface(roboto);
		dayNameTextPaint.setTextSize(15 * scale);
//		dayNameTextPaint.setTextSize((int)getResources().getDimension(R.dimen.home_text_l));
		dayNameTextPaint.setARGB(255,  100,  100,  100);	
		
		dayNumberTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dayNumberTextPaint.setTypeface(roboto);
		dayNumberTextPaint.setTextSize(25 * scale);
		dayNumberTextPaint.setARGB(255,  100,  100,  100);	
		
		currentTimeBarPaint = new Paint();
		currentTimeBarPaint.setARGB(255, 200, 0, 0);
		currentTimeBarPaint.setStyle(Paint.Style.FILL);
		
		taskBackgroundPaint = new Paint();
		taskBackgroundPaint.setARGB(85, 100, 0, 100);
		taskBackgroundPaint.setStyle(Paint.Style.FILL);
//		taskBackgroundPaint.setShadowLayer(1, -10, -10, Color.DKGRAY);
	}

//	public void addTaskToView(int taskNumber, float start, float end, String taskName)
//	{
//		++numberOfTasks;
//	}
	
	/**Rough implementation of onMeasure method*/
	@Override
	public void onMeasure(int x, int y)
	{	
		setVisibleHeight();
		float numberOfDays = timeInterval/DAY_MILLIS;
		if (dayHeight*numberOfDays < visibleHeight) {
			setMeasuredDimension(MeasureSpec.getSize(x), visibleHeight);
		}
		else {
			setMeasuredDimension(MeasureSpec.getSize(x), (int) (numberOfDays*dayHeight));	
		}
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		drawBackground(canvas);
		//drawTasks(canvas);
		drawTask(canvas, 4, 0, startTime + (long)(1.5*DAY_MILLIS), startTime + 3*DAY_MILLIS, "Calculus");
		//Draw a task again in the same place to indicate a work time. 
		//drawTaskBackground(canvas, 4, 0, startTime + 2.2*DAY_MILLIS, startTime + 2.5*DAY_MILLIS);
		drawTask(canvas, 4, 2, startTime + 2*DAY_MILLIS, startTime + (long)(3.5*DAY_MILLIS), "SS work");
		drawTask(canvas, 4, 3, startTime + (long)(2.5*DAY_MILLIS), startTime + (long)(3.5*DAY_MILLIS), "Comp. Org");
		drawTask(canvas, 4, 1, startTime + (long)(0.5*DAY_MILLIS), startTime + 2*DAY_MILLIS, "Data Structures.");
		drawTimeBar(canvas);
	}
	
	private void drawTimeBar(Canvas canvas)
	{	
		if (currentTime < endTime && currentTime > startTime) {
			timeSinceStart = currentTime-startTime;
			canvas.drawRect(0,  timeSinceStart/(float)timeInterval*canvas.getHeight(), 
					canvas.getWidth(), 
					timeSinceStart/(float)timeInterval*canvas.getHeight() + 3*scale, 
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
					dayHeight*(currentDay - firstDay)+2.5f*scale, daySeparatorPaint);
			//Draw text for each day
			canvas.drawText(day.getDisplayName(Calendar.DAY_OF_WEEK, 
												Calendar.SHORT, Locale.US),
					3*scale, dayHeight*(currentDay-firstDay)+15*scale, dayNameTextPaint);
			canvas.drawText(String.valueOf(day.get(Calendar.DAY_OF_MONTH)),
					3*scale, dayHeight*(currentDay-firstDay)+37*scale, dayNumberTextPaint);
			++currentDay;
			day.roll(Calendar.DAY_OF_YEAR,  true);
		}
	}
}
