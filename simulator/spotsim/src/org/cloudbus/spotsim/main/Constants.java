package org.cloudbus.spotsim.main;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Constants {

    public static final GregorianCalendar START_TIME = new GregorianCalendar(2014, Calendar.JUNE, 15);

    public static final GregorianCalendar END_TIME = new GregorianCalendar(2014, Calendar.SEPTEMBER,
	15);

    public static final String ACCESS_KEY = "AKIAJ4HZ5EMUVVXIBCJA";

    public static final String SECRET_KEY = "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";

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final int MIN_INTERVAL_UNIT = Calendar.HOUR;

    public static final int MIN_INTERVAL_VALUE = 1;
}
