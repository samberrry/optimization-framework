package org.cloudbus.spotsim.main;

public class VolatilityCombo implements Comparable<VolatilityCombo> {

    private final double volat;

    private final double priceMuMult;

    private final double priceSigmaMult;

    private final double timeMuMult;

    public VolatilityCombo(double volat, double priceMuMult, double priceSigmaMult,
	    double timeMuMult) {
	super();
	this.volat = volat;
	this.priceMuMult = priceMuMult;
	this.priceSigmaMult = priceSigmaMult;
	this.timeMuMult = timeMuMult;
    }

    public double getVolat() {
	return this.volat;
    }

    public double getPriceMuMult() {
	return this.priceMuMult;
    }

    public double getPriceSigmaMult() {
	return this.priceSigmaMult;
    }

    public double getTimeMuMult() {
	return this.timeMuMult;
    }

    @Override
    public int compareTo(VolatilityCombo o) {
	return Double.compare(this.volat, o.volat);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	long temp;
	temp = Double.doubleToLongBits(this.priceMuMult);
	result = prime * result + (int) (temp ^ temp >>> 32);
	temp = Double.doubleToLongBits(this.priceSigmaMult);
	result = prime * result + (int) (temp ^ temp >>> 32);
	temp = Double.doubleToLongBits(this.timeMuMult);
	result = prime * result + (int) (temp ^ temp >>> 32);
	temp = Double.doubleToLongBits(this.volat);
	result = prime * result + (int) (temp ^ temp >>> 32);
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (this.getClass() != obj.getClass())
	    return false;
	VolatilityCombo other = (VolatilityCombo) obj;
	if (Double.doubleToLongBits(this.priceMuMult) != Double.doubleToLongBits(other.priceMuMult))
	    return false;
	if (Double.doubleToLongBits(this.priceSigmaMult) != Double
	    .doubleToLongBits(other.priceSigmaMult))
	    return false;
	if (Double.doubleToLongBits(this.timeMuMult) != Double.doubleToLongBits(other.timeMuMult))
	    return false;
	if (Double.doubleToLongBits(this.volat) != Double.doubleToLongBits(other.volat))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return "VolatPOJO [priceMuMult="
		+ this.priceMuMult
		+ ", priceSigmaMult="
		+ this.priceSigmaMult
		+ ", timeMuMult="
		+ this.timeMuMult
		+ ", volat="
		+ this.volat
		+ "]";
    }
}