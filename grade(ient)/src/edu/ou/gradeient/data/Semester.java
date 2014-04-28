package edu.ou.gradeient.data;

import org.joda.time.LocalDate;

/**
 * Represents a semester
 */
public class Semester {

//	public static final class Schema implements BaseColumns {
//		/** The URI for the Semester table */
//		public static final Uri CONTENT_URI = Uri.withAppendedPath(
//				TaskProvider.CONTENT_URI, "semesters");
//		/** MIME type for a list of Semesters */
//		public static final String CONTENT_TYPE = 
//				ContentResolver.CURSOR_DIR_BASE_TYPE + 
//				"/vnd." + TaskProvider.AUTHORITY + "_semesters";
//		/** MIME type for a single Semester */
//		public static final String CONTENT_ITEM_TYPE = 
//				ContentResolver.CURSOR_ITEM_BASE_TYPE + 
//				"/vnd." + TaskProvider.AUTHORITY + "_semesters";
//
//		/* default */ static final String TABLE = "Semester";
//		public static final String NAME = "name";
//		public static final String START_DATE = "start_date";
//		public static final String END_DATE = "end_date";
//
//		//TODO subjects?
//
//		public static final String SORT_ORDER_DEFAULT = END_DATE + " ASC";
//	}
	
	/** Semester ID (auto-generated; do not modify) */
	private Long id;
	
	/** The semester name */
	private String name;
	
	/** The semester start date */
	private LocalDate startDate; //TODO allow null? (currently: yes)
	
	/** The semester end date (inclusive) */
	private LocalDate endDate; //TODO allow null? (currently: yes)
	
	public Semester(String name) {
		this.name = name;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 * @throws IllegalArgumentException if name is null
	 */
	public void setName(String name) {
		if (name == null)
			throw new IllegalArgumentException("Semester name cannot be null.");
		this.name = name;
	}

	/**
	 * @return the startDate
	 */
	public LocalDate getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set. To set a null start date,
	 * use setDates.
	 * @throws IllegalArgumentException start date is after current end date,
	 * start date is null, or current start/end dates are null.
	 */
	public void setStartDate(LocalDate startDate) {
		if (this.startDate == null)
			throw new IllegalArgumentException("Semester start and end dates "
					+ "are null. Reset them together using setDates.");
		if (startDate == null)
			throw new IllegalArgumentException("To set semester start and end "
					+ "dates to null, use setDates.");
		if (startDate.isAfter(this.endDate))
			throw new IllegalArgumentException("Semester start date "
					+ "must be before semester end date.");
		this.startDate = startDate;
	}

	/**
	 * @return the endDate, inclusive
	 */
	public LocalDate getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set. To set a null end date, use setDates.
	 * @throws IllegalArgumentException end date is before current start date,
	 * end date is null, or current start/end dates are null.
	 */
	public void setEndDate(LocalDate endDate) {
		if (this.endDate == null)
			throw new IllegalArgumentException("Semester start and end dates "
					+ "are null. Reset them together using setDates.");
		if (endDate == null)
			throw new IllegalArgumentException("To set semester start and end "
					+ "dates to null, use setDates.");
		if (endDate.isBefore(this.startDate))
			throw new IllegalArgumentException("Semester end date must be "
					+ "after semester start date.");
		this.endDate = endDate;
	}
	
	/**
	 * Set the start and end dates together. This method must be used if
	 * the start and end dates are currently null.
	 * @param startDate the start date to set
	 * @param endDate the end date to set
	 * @throws IllegalArgumentException if end is before start or one but
	 * not both dates are null
	 */
	public void setDates(LocalDate startDate, LocalDate endDate) {
		if (!((startDate == null) == (endDate == null)))
			throw new IllegalArgumentException("To set null start or end "
					+ "date, both dates must be null.");
		if (startDate != null && startDate.isAfter(endDate))
			throw new IllegalArgumentException("Semester start date must be "
					+ "before semester end date.");
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	//TODO: We could add a method to get an interval representing this semester,
	// but that gets into the question of time zones.

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((startDate == null) ? 0 : startDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || !(obj instanceof Semester))
			return false;
		Semester other = (Semester) obj;
		if ((endDate == null && other.endDate != null)
				|| (id == null && other.id != null)
				|| (name == null && other.name != null)
				|| (startDate == null && other.startDate != null))
			return false;
		return endDate.equals(other.endDate) 
				&& id.equals(other.id)
				&& name.equals(other.name)
				&& startDate.equals(other.startDate);
	}
}
