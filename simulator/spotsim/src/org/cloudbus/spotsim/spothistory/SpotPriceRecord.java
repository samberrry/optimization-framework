package org.cloudbus.spotsim.spothistory;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.cloudbus.spotsim.main.Constants;
import org.cloudbus.spotsim.main.config.Config;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("price_record")
public class SpotPriceRecord implements Serializable, Comparable<SpotPriceRecord> {

    private static final long serialVersionUID = 1L;

    private final double price;

    private GregorianCalendar date;

    public SpotPriceRecord(final Date date, final double price) {
	final GregorianCalendar d = new GregorianCalendar();
	d.setTime(date);
	this.date = d;
	this.price = price;
    }

    public SpotPriceRecord(final GregorianCalendar date, final double price) {
	this.date = date;
	this.price = price;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (this.getClass() != obj.getClass()) {
	    return false;
	}
	final SpotPriceRecord other = (SpotPriceRecord) obj;
	if (this.date == null) {
	    if (other.date != null) {
		return false;
	    }
	} else if (!this.date.equals(other.date)) {
	    return false;
	}
	if (Double.doubleToLongBits(this.getPrice()) != Double.doubleToLongBits(other.getPrice())) {
	    return false;
	}
	return true;
    }

    public GregorianCalendar getDate() {
	return this.date;
    }

    public double getPrice() {
	return this.price;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (this.date == null ? 0 : this.date.hashCode());
	long temp;
	temp = Double.doubleToLongBits(this.getPrice());
	result = prime * result + (int) (temp ^ temp >>> 32);
	return result;
    }

    public long millisSinceStart() {
    /*	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS",Locale.US);

        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Central"));
        calendar.setTimeInMillis(this.date.getTimeInMillis());
    	System.out.println("Calender " + calendar.getTime()+ " Config " + Config.getSimPeriodStart().getTime());*/
	return this.date.getTimeInMillis() - Config.getSimPeriodStart().getTimeInMillis();
    }

    public long secondsSinceStart() {
	return (this.date.getTimeInMillis() - Constants.START_TIME.getTimeInMillis()) / 1000;
    }

    public void setDate(final GregorianCalendar date) {
	this.date = date;
    }

    public void setDateDate(final Date date) {
	this.date.setTime(date);
    }

    @Override
    public String toString() {
	return new SimpleDateFormat(Constants.DATE_FORMAT).format(this.date.getTime())
		+ ","
		+ new DecimalFormat("#.####").format(this.getPrice());
    }

    @Override
    public int compareTo(SpotPriceRecord o) {
	return this.date.compareTo(o.date);
    }
}