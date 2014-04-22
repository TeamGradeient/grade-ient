package edu.ou.gradeient;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInterval;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import android.text.format.DateFormat;
import android.widget.TextView;

/**
 * Handling time is fun! Let's make utility methods for it!
 */
public class TimeUtils {
	private static final DateTimeFormatter MONTH_DAY_NUMERIC =
			DateTimeFormat.forPattern("M/d");
	private static final DateTimeFormatter TIME_HOUR_MIN_AM =
			DateTimeFormat.forPattern("h:mma");
	private static final DateTimeFormatter TIME_HOUR_MIN_24_HOUR =
			DateTimeFormat.forPattern("HH:mm");
	private static final DateTimeFormatter WEEKDAY_MONTH_DAY =
			new DateTimeFormatterBuilder().appendDayOfWeekText()
			.appendLiteral(", ").appendMonthOfYearShortText().appendPattern(". d")
			.toFormatter();
	private static final DateTimeFormatter WEEKDAY_MONTH_DAY_SHORT =
			new DateTimeFormatterBuilder().appendDayOfWeekShortText()
			.appendLiteral(", ").appendMonthOfYearShortText().appendPattern(". d")
			.toFormatter();
	private static final DateTimeFormatter WEEKDAY_MONTH_DAY_YEAR =
			new DateTimeFormatterBuilder().appendPattern("EEE, ")
			.append(DateTimeFormat.mediumDate()).toFormatter();
	private static final ISOChronology chron = ISOChronology.getInstance();
	//TODO will break with time zones
	private static long todayEnd = 0;
	private static long tomorrowEnd = 0;
	static {
		updateTodayEnd();
	}
	
	private static void updateTodayEnd() {
		if (System.currentTimeMillis() > todayEnd) {
			DateTime today = LocalDate.now().toDateTimeAtStartOfDay(
					DateTimeZone.getDefault());
			todayEnd = today.plusDays(1).getMillis() - 1;
			tomorrowEnd = today.plusDays(2).getMillis() - 1;
		}
	}
	
	/** Returns a m/d date. */
	public static String formatMonthDayNumeric(long millis) {
		return MONTH_DAY_NUMERIC.print(millis);
	}
	
	/** Returns 12-hour time with am/pm or 24-hour time, as appropriate. */
	public static String formatTime(long millis) {
		if (DateFormat.is24HourFormat(GradeientApp.getAppContext()))
			return TIME_HOUR_MIN_24_HOUR.print(millis);
		return TIME_HOUR_MIN_AM.print(millis).toLowerCase();
	}
	
	/** Returns "today", "tomorrow", or a m/d date. */
	public static String formatMonthDayTodayTomorrow(long millis) {
		updateTodayEnd();
		if (millis <= todayEnd)
			return "today";
		if (millis <= tomorrowEnd)
			return "tomorrow";
		return formatMonthDayNumeric(millis);
	}
	
	/** Returns date in format Wednesday, Sept. 20 */
	public static String formatWeekdayMonthDay(long millis) {
		return WEEKDAY_MONTH_DAY.print(millis);
	}
	
	/** Returns date in format Wed, Sept. 20 */
	public static String formatWeekdayMonthDayShorter(long millis) {
		return WEEKDAY_MONTH_DAY_SHORT.print(millis);
	}
	
	/** Returns date in format Wed, Sept. 20, 2013 */
	public static String formatWeekdayMonthDayYear(long millis) {
		return WEEKDAY_MONTH_DAY_YEAR.print(millis);
	}
	
	/** Set the date/time text on some TextViews (or Buttons) */
	public static void setDateText(ReadableInterval interval, TextView startTime,
			TextView startDate, TextView endTime, TextView endDate) {
		long startMillis = interval.getStartMillis();
		long endMillis = interval.getEndMillis();
		startDate.setText(TimeUtils.formatWeekdayMonthDayYear(startMillis));
		startTime.setText(TimeUtils.formatTime(startMillis));
		endDate.setText(TimeUtils.formatWeekdayMonthDayYear(endMillis));
		endTime.setText(TimeUtils.formatTime(endMillis));
	}
}
