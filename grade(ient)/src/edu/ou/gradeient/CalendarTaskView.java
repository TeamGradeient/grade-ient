package edu.ou.gradeient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CalendarTaskView extends View {
	
	Paint myPaint;

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
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		canvas.drawCircle(canvas.getWidth()/2,  canvas.getHeight()/2,
				canvas.getWidth()/4, myPaint);
	}
}
