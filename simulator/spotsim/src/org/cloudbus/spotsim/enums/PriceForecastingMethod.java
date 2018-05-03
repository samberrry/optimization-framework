package org.cloudbus.spotsim.enums;

/**
 * Defines which kind of price forecasting method will be used by the broker to
 * estimated costs.
 * 
 * @author William Voorsluys - williamvoor@gmail.com
 * 
 */
public enum PriceForecastingMethod {
    // will use actual price traces from Amazon EC2 to "predict" future prices
    OPTIMAL("OP", true),

    MM("MM", true),

    ON_DEMAND("DEMAND", false),

    PAST_N_DAYS_MEAN("PW", false),

    CURRENT("CR", false);

    private final String shortName;

    private final boolean isFuturistic;

    private PriceForecastingMethod(final String shortName, final boolean futuristic) {
	this.shortName = shortName;
	this.isFuturistic = futuristic;
    }

    public boolean isFuturistic() {
	return this.isFuturistic;
    }

    public String shortName() {
	return this.shortName;
    }
}
