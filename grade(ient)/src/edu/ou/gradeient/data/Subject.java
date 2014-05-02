package edu.ou.gradeient.data;

import java.util.regex.Pattern;

import android.text.TextUtils;
import edu.ou.gradeient.R;

/**
 * Represents a subject
 */
public class Subject {
//	public static final class Schema implements BaseColumns {
//		/** URI for the Subject table */
//		public static final Uri CONTENT_URI = Uri.withAppendedPath(
//				TaskProvider.CONTENT_URI, "subjects");
//		/** MIME type for a list of Subjects */
//		public static final String CONTENT_TYPE = 
//				ContentResolver.CURSOR_DIR_BASE_TYPE + 
//				"/vnd." + TaskProvider.AUTHORITY + "_subjects";
//		/** MIME type for a single Subject */
//		public static final String CONTENT_ITEM_TYPE = 
//				ContentResolver.CURSOR_ITEM_BASE_TYPE + 
//				"/vnd." + TaskProvider.AUTHORITY + "_subjects";
//
//		/* default */ static final String TABLE = "Subject";
//		public static final String SEMESTER_ID = "semester_id";
//		public static final String NAME = "name";
//		public static final String ABBREVIATION = "abbreviation";
//		public static final String START_DATE = "start_date";
//		public static final String END_DATE = "end_date";
//
//		//TODO tasks? semester object?
//
//		public static final String SORT_ORDER_DEFAULT = END_DATE + " ASC";
//	}

	private static final Pattern SUBJ_ABBREV = 
			Pattern.compile("(.)\\S*\\s+(.)\\S*\\s*(.?).*\\s*(.?).*");
	private static final String SUBJ_REPLACE = "[$1$2$3$4]";
	
	private static final int[] COLORS = { R.color.holo_blue_dark, 
		R.color.holo_green_dark, R.color.holo_orange_dark, R.color.holo_purple,
		R.color.holo_red_dark };
	
	/**A string containing the name of the subject*/
	private String name;
	
	/**
	 * Returns an abbreviation of the name surrounded by square brackets.
	 * If input was null or (with spaces stripped) was empty, returns null.
	 */
	public static String abbreviateName(String subjectName) {
		if (subjectName != null)
			subjectName = subjectName.trim();
		if (subjectName == null || subjectName.length() == 0)
			return null;
		if (subjectName.indexOf(' ') != -1)
			return SUBJ_ABBREV.matcher(subjectName).replaceFirst(SUBJ_REPLACE);
		if (subjectName.length() > 4)
			return '[' + subjectName.substring(0, 4) + ']';
		return '[' + subjectName + ']';
	}
	
	/**
	 * Get a color for the subject based on the name's hash code. 
	 * If the name is null or empty, returns gray.
	 */
	public static int getColor(String subjectName) {
		if (TextUtils.isEmpty(subjectName)) return R.color.dark_gray;
		int hashCode = subjectName.hashCode();
		if (hashCode < 0) hashCode = -hashCode;
		return COLORS[hashCode % COLORS.length];
	}
}
