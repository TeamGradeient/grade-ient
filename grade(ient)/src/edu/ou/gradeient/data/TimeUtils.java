package edu.ou.gradeient.data;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInterval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import edu.ou.gradeient.GradeientApp;
import android.text.format.DateFormat;
import android.widget.TextView;

/**
 * Handling time is fun! Let's make utility methods for it!
 */
public class TimeUtils {
	/** M/d (4/25) */
	private static final DateTimeFormatter MONTH_DAY_NUMERIC =
			DateTimeFormat.forPattern("M/d");
	/** h:mma (8:53am, 8:53pm) */
	private static final DateTimeFormatter TIME_HOUR_MIN_AM =
			DateTimeFormat.forPattern("h:mma");
	/** HH:mm (08:53, 20:53) */
	private static final DateTimeFormatter TIME_HOUR_MIN_24 =
			DateTimeFormat.forPattern("HH:mm");
	/** Weekday, Month d (Friday, April 25) */
	private static final DateTimeFormatter WEEKDAY_MONTH_DAY =
			new DateTimeFormatterBuilder().appendDayOfWeekText()
			.appendLiteral(", ").appendMonthOfYearText().appendPattern(" d")
			.toFormatter();
	/** Weekday, Mon d (Friday, Apr 25) */
	private static final DateTimeFormatter WEEKDAY_MON_DAY =
			new DateTimeFormatterBuilder().appendDayOfWeekText()
			.appendLiteral(", ").appendMonthOfYearShortText().appendPattern(" d")
			.toFormatter();
	/** Weekd, Mon d (Fri, Apr 25) */
	private static final DateTimeFormatter WEEKD_MON_DAY =
			new DateTimeFormatterBuilder().appendDayOfWeekShortText()
			.appendLiteral(", ").appendMonthOfYearShortText().appendPattern(" d")
			.toFormatter();
	/** Weekd, Mon d, YYYY (Fri, Apr 25, 2014) */
	private static final DateTimeFormatter WEEKD_MON_DAY_YEAR =
			new DateTimeFormatterBuilder().appendPattern("EEE, ")
			.append(DateTimeFormat.mediumDate()).toFormatter();
	//TODO will break with time zones
	private static long todayBegin = 0;
	private static long todayEnd = 0;
	private static long tomorrowEnd = 0;
	private static long yearBegin = 0;
	private static long yearEnd = 0;
	static {
		updateTodayEnd();
		updateYearEnd();
	}
	
	/** Returns a m/d date. */
	public static String formatMonthDayNumeric(long millis) {
		return MONTH_DAY_NUMERIC.print(millis);
	}
	
	/** Returns 12-hour time with am/pm or 24-hour time, as appropriate. */
	public static String formatTime(long millis) {
		if (DateFormat.is24HourFormat(GradeientApp.getAppContext()))
			return TIME_HOUR_MIN_24.print(millis);
		return TIME_HOUR_MIN_AM.print(millis).toLowerCase();
	}
	
	/** Returns "today", "tomorrow", or a m/d date. */
	public static String formatMonthDayTodayTomorrow(long millis) {
		updateTodayEnd();
		if (millis >= todayBegin) {
			if (millis <= todayEnd)
				return "today";
			if (millis <= tomorrowEnd)
				return "tomorrow";
		}
		return formatMonthDayNumeric(millis);
	}
	
	/** Returns "today" or a m/d date. */
	public static String formatMonthDayToday(long millis) {
		updateTodayEnd();
		if (millis >= todayBegin && millis <= todayEnd)
			return "today";
		return formatMonthDayNumeric(millis);
	}
	
	/** Returns date in format Friday, April 25 */
	public static String formatWeekdayMonthDay(long millis) {
		return WEEKDAY_MONTH_DAY.print(millis);
	}
	
	/** Returns date in format Friday, Apr 25 */
	public static String formatWeekdayMonDay(long millis) {
		return WEEKDAY_MON_DAY.print(millis);
	}
	
	/** Returns date in format Fri, Apr 25 */
	public static String formatWeekdMonDay(long millis) {
		return WEEKD_MON_DAY.print(millis);
	}
	
	/** Returns date in format Fri, Apr 25, 2014 */
	public static String formatWeekdMonDayYear(long millis) {
		return WEEKD_MON_DAY_YEAR.print(millis);
	}
	
	/** Returns date in format Friday, April 25 if date is in current year,
	 * or Friday, April 25, 2014 if date is not in current year. */
	public static String formatTimeDate(long millis) {
		String time = formatTime(millis) + ", ";
		if (millis < yearBegin || millis > yearEnd)
			return time + DateTimeFormat.fullDate().print(millis);
		return time + WEEKDAY_MONTH_DAY.print(millis);
	}
	
	/** Set the date/time text on some TextViews (or Buttons) */
	public static void setDateText(ReadableInterval interval, TextView startTime,
			TextView startDate, TextView endTime, TextView endDate) {
		long startMillis = interval.getStartMillis();
		long endMillis = interval.getEndMillis();
		startDate.setText(TimeUtils.formatWeekdMonDayYear(startMillis));
		startTime.setText(TimeUtils.formatTime(startMillis));
		endDate.setText(TimeUtils.formatWeekdMonDayYear(endMillis));
		endTime.setText(TimeUtils.formatTime(endMillis));
	}
	
	private static void updateTodayEnd() {
		if (System.currentTimeMillis() > todayEnd) {
			DateTime today = LocalDate.now().toDateTimeAtStartOfDay(
					DateTimeZone.getDefault());
			todayBegin = today.getMillis();
			todayEnd = today.plusDays(1).getMillis() - 1;
			tomorrowEnd = today.plusDays(2).getMillis() - 1;
		}
	}
	
	private static void updateYearEnd() {
		if (System.currentTimeMillis() > yearEnd) {
			DateTime year = DateTime.now().withDayOfYear(1).withMillisOfDay(0);
			yearBegin = year.getMillis();
			yearEnd = year.plusYears(1).getMillis() - 1;
		}
	}
}
