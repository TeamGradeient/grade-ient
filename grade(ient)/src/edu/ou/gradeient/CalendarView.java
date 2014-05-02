package edu.ou.gradeient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.MutableDateTime;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.ou.gradeient.data.Subject;
import edu.ou.gradeient.data.Task;
import edu.ou.gradeient.data.TaskWorkInterval;
import edu.ou.gradeient.data.TimeUtils;

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
	private Paint taskNamePaint;
	private int[] colors;
	
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

	private DateTime startDate;
	private DateTime endDate;

	private long startTime;
	private long endTime;
	//TODO make a background task to update this
	private long currentTime = System.currentTimeMillis();
	private long timeInterval;
	private long timeSinceStart;

	// TEMPORARY
	private TreeSet<Task> tasks = new TreeSet<Task>();
	private ArrayList<ArrayList<Task>> columns = new ArrayList<ArrayList<Task>>();

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
	
	/**
	 * Update the tasks and work times the view displays
	 * @param taskCursor Cursor to task table
	 * @param workCursor Cursor to work times table
	 * @param startMillis Start time used in query
	 * @param endMillis End time used in query
	 */
	public void updateTasks(Cursor taskCursor, Cursor workCursor,
			long startMillis, long endMillis) {
		// Update the dates displayed
		setDisplayDates(startMillis, endMillis);

		//TODO figure out a better long-term solution
		// get all tasks in the date range shown, temporarily indexed by ID
		HashMap<Long, Task> taskMap = new HashMap<Long, Task>();
		Interval interval = new Interval(startTime, endTime);
		while (taskCursor.moveToNext()) {
			try {
				Task t = new Task(taskCursor, null);
				if (interval.overlaps(t.getInterval()))
					taskMap.put(t.getId(), t);
			} catch (Exception ex) {
				Log.w(TAG, "While getting tasks: " + ex);
			}
		}
		// match up work times with tasks
		while (workCursor.moveToNext()) {
			try {
				TaskWorkInterval twi = new TaskWorkInterval(workCursor);
				if (taskMap.containsKey(twi.getTaskId()))
					taskMap.get(twi.getTaskId()).addWorkInterval(twi);
			} catch (Exception ex) {
				Log.w(TAG, "While getting work times: " + ex);
			}
		}
		// add everything to the actual task map, sorted by start date
		tasks = new TreeSet<Task>(Task.BY_START_DATE);
		tasks.addAll(taskMap.values());
		
		// Determine which tasks should go into which columns (no more than 5 
		// columns are allowed). Currently, this goes through the tasks in order
		// of start date and uses a greedy strategy to place them in columns.
		// (Greedy means it places them in the first spot from left to right 
		// where nothing else is in the column at that time.)
		// This will not yield the best results visually, and it could also
		// result in less-than-optimal placements with some tasks left out.
		columns = new ArrayList<ArrayList<Task>>();
		for (int i = 0; i < 5; ++i)
			columns.add(new ArrayList<Task>());
		int failCount = 0;
		for (Task t : tasks) {
			boolean success = false;
			long tStart = t.getStartMillis();
			for (ArrayList<Task> col : columns) {
				// Try to find an empty column or a column in which the 
				// last task ends before this one begins.
				if (col.isEmpty()
						|| col.get(col.size() - 1).getEndMillis() < tStart) {
					col.add(t);
					success = true;
					break;
				}
			}
			if (success == false) {
				Log.w(TAG, "Couldn't display task " + t.getName() + 
						" at " + t.getStart().toString());
				++failCount;
			}
		}
		if (failCount != 0)
			//TODO perhaps show the user a dialog saying some tasks are missing?
			Log.w(TAG, "Total tasks missing: " + failCount);
		
		// If the fourth and fifth columns didn't get used, remove them so the
		// task bars can be wider.
		if (columns.get(4).isEmpty())
			columns.remove(4);
		if (columns.get(3).isEmpty())
			columns.remove(3);
		invalidate();
	}
	
	private void init()
	{
		initializePaints();
		DateTime startDate = DateTime.now().withTimeAtStartOfDay().minusWeeks(1);
		DateTime endDate = startDate.plusWeeks(4);
		setDisplayDates(startDate.getMillis(), endDate.getMillis());
	}
	
	// Task name should be null if we're drawing a work time
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
		if (taskName != null) {
			drawTaskDueTime(canvas, leftEdge, rightEdge, end);
			canvas.drawText(taskName, leftEdge, findYPosition(start) - 2*scale, 
					taskNamePaint);
		}
	}
	
	private void setTaskTitle(String title, float leftEdge, float rightEdge)
	{
//		AbsoluteLayout al = (AbsoluteLayout) findViewById(R.id.task_titles);
//		TextView tv = new TextView(al.getContext());
//		AbsoluteLayout.LayoutParams parameters = (LayoutParams) al.getLayoutParams();
//		parameters.x = (int) leftEdge;
//		parameters.y = (int)(50 * scale);
//		al.addView(tv, parameters);
	}
	
	private void drawTaskDueTime(Canvas canvas, float leftEdge, 
			float rightEdge, long end)
	{
		String taskDueTime = TimeUtils.formatTime(end);
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
	
	public int findYForBeginningOfDay(long milliseconds)
	{
		long dayNumber = (milliseconds-startTime)/DAY_MILLIS;
		return (int)(dayNumber*dayHeight); 
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
	
	private void setDisplayDates(long startMillis, long endMillis)
	{
		// So that only whole days are shown, "round" the start date up and the
		// end date down for viewing purposes.
		startDate = new DateTime(startMillis);
		if (startDate.getMillisOfDay() != 0)
			startDate = startDate.withTimeAtStartOfDay().plusDays(1);
		endDate = new DateTime(endMillis).withTimeAtStartOfDay();
		startTime = startDate.getMillis();
		endTime = endDate.getMillis();
		timeInterval= endTime-startTime;
		
		setVisibleHeight();
		usableWidth = (int) (absoluteWidth - distanceFromEdge);
	}
	
	private void initializePaints()
	{	
		daySeparatorPaint = new Paint ();
		daySeparatorPaint.setARGB(255,  100,  100,  100);
		daySeparatorPaint.setStyle(Paint.Style.FILL);

		dayNameTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dayNameTextPaint.setTypeface(roboto);
		dayNameTextPaint.setTextSize(15 * scale);
		dayNameTextPaint.setARGB(255,  100,  100,  100);	
		
		dayNumberTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dayNumberTextPaint.setTypeface(roboto);
		dayNumberTextPaint.setTextSize(25 * scale);
		dayNumberTextPaint.setARGB(255,  100,  100,  100);	
		
		currentTimeBarPaint = new Paint();
		currentTimeBarPaint.setARGB(255, 200, 0, 0);
		currentTimeBarPaint.setStyle(Paint.Style.FILL);
		
		colors = new int[Subject.COLOR_RESOURCES.length];
		for (int i = 0; i < colors.length; ++i) {
			int color = getResources().getColor(Subject.COLOR_RESOURCES[i]);
			// clear the alpha bits of the color
			color &= 16777215; // 2^24 - 1
			// | (85 << 24) means set the alpha to 85 out of 255
			colors[i] = color | (85 << 24);
		}
		
		taskBackgroundPaint = new Paint();
		taskBackgroundPaint.setStyle(Paint.Style.FILL);
		
		//same settings as day name paint, but set up as a separate
		//paint object so that we can change it later if we want
		taskNamePaint = new Paint(dayNameTextPaint);
	}

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
		drawTasks(canvas);
		drawTimeBar(canvas);
	}
	
	private void drawTimeBar(Canvas canvas)
	{	
		currentTime = System.currentTimeMillis();
		if (currentTime < endTime && currentTime > startTime) {
			timeSinceStart = currentTime-startTime;
			canvas.drawRect(0,  timeSinceStart/(float)timeInterval*canvas.getHeight(), 
					canvas.getWidth(), 
					timeSinceStart/(float)timeInterval*canvas.getHeight() + 3*scale, 
					currentTimeBarPaint);
		}
	}
	
	private void drawTasks(Canvas canvas)
	{
		int numColumns = columns.size();
		for (int col = 0; col < numColumns; ++col) {
			for (Task t : columns.get(col)) {
				int color = colors[Subject.getColorIndex(t.getSubject())];
				taskBackgroundPaint.setColor(color);
				// Draw the task
				drawTask(canvas, numColumns, col, t.getStartMillis(), 
						t.getEndMillis(), t.getName());
				// Draw the task's work intervals: for now, this is done by 
				// drawing another "task" bar on top of the real task bar.
				for (TaskWorkInterval twi : t.getWorkIntervals()) {
					drawTask(canvas, numColumns, col, twi.getStartMillis(),
							twi.getEndMillis(), null);
					// certain work intervals should be extra dark
					if (twi.isCertain())
						drawTask(canvas, numColumns, col, twi.getStartMillis(),
								twi.getEndMillis(), null);
				}
			}
		}
	}
	

	private void drawBackground(Canvas canvas)
	{
		MutableDateTime day = new MutableDateTime(startDate);
		// Get the number of days between start and end, handling leap years.
		// (We can't just use (startTime - endTime) / DAY_MILLIS because of
		// daylight savings time, though probably other things will break
		// because of DST even if this doesn't...)
		int numDays = endDate.getDayOfYear() - startDate.getDayOfYear();
		if (startDate.getYear() != endDate.getYear())
			numDays += (startDate.year().isLeap() ? 366 : 355) - startDate.getDayOfYear();
		float dayHeight = (float)canvas.getHeight() / numDays;
		int width = canvas.getWidth();
		for (int currentDay = 0; currentDay < numDays; 
				++currentDay, day.addDays(1))
		{
			//Draw separators
			canvas.drawRect(0, dayHeight*currentDay, width,  
					dayHeight*currentDay + 2.5f*scale, daySeparatorPaint);
			//Draw text for each day
			canvas.drawText(day.toString("EEE"),
					3*scale, dayHeight*currentDay + 15*scale, dayNameTextPaint);
			canvas.drawText(day.getDayOfMonth() + "",
					3*scale, dayHeight*currentDay + 37*scale, dayNumberTextPaint);
		}
	}
}
