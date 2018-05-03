package org.cloudbus.spotsim.pricing;

public class DateUtils {

    public static String getMillisInDays(final long t) {

	int secs = (int) Math.floor(t / 1000D);

	return getSecsInDays(secs);
    }

    public static String getSecsInDays(long t) {
	final StringBuilder b = new StringBuilder();

	long secs = t;
	if (secs >= 86400) {
	    final int hours = (int) Math.floor(secs / 86400);
	    b.append(hours).append('d');
	    secs %= 86400;
	}

	if (secs >= 3600) {
	    final int hours = (int) Math.floor(secs / 3600);
	    b.append(hours).append('h');
	    secs %= 3600;
	}

	if (secs >= 60) {
	    final int minutes = (int) Math.floor(secs / 60);
	    b.append(minutes).append('m');
	    secs %= 60;
	}
	if (secs > 0) {
	    b.append(secs).append('s');
	}

	return b.toString();
    }
}