package edu.ou.gradeient;

import java.io.Serializable;

import android.text.format.Time;

/**
 * Temporary solution to allow Time class to be serializable.
 */
public class Time2 extends Time implements Serializable {
	public Time2(Time time) {
		super(time);
	}
}
