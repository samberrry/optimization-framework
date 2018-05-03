package org.cloudbus.spotsim.main;

import java.util.ArrayList;
import java.util.EnumSet;

import org.cloudbus.spotsim.enums.BiddingStrategy;
import org.cloudbus.spotsim.enums.FaultToleranceMethod;
import org.cloudbus.spotsim.enums.PriceForecastingMethod;
import org.cloudbus.spotsim.enums.RuntimeEstimationMethod;

public class Sweep {

    public static final VolatilityCombo VOL_M_30_PCT = new VolatilityCombo(-1D, 1D, 1D, 2D);

    public static final VolatilityCombo VOL_M_20_PCT = new VolatilityCombo(-1D, 1D, 1.4, 2D);

    public static final VolatilityCombo VOL_M_10_PCT = new VolatilityCombo(-1D, 1D, 1.8, 1.8);

    public static final VolatilityCombo VOL_P_10_PCT = new VolatilityCombo(1.441, 1D, 1.8, 1.4);

    public static final VolatilityCombo VOL_P_20_PCT = new VolatilityCombo(-1D, 1D, 2.9, 1.6);

    public static final VolatilityCombo VOL_P_30_PCT = new VolatilityCombo(-1D, 1D, 1.6, 0.3);

    public static final VolatilityCombo VOL_P_40_PCT = new VolatilityCombo(-1D, 1D, 1.8, 0.4);

    public static final VolatilityCombo VOL_P_50_PCT = new VolatilityCombo(-1D, 1D, 2.6, 0.6);

    public static final VolatilityCombo VOL_P_60_PCT = new VolatilityCombo(-1D, 1D, 2.7, 0.5);

    public static final VolatilityCombo VOL_P_70_PCT = new VolatilityCombo(-1D, 1D, 2.9, 0.2);

    public static final EnumSet<FaultToleranceMethod> ft = EnumSet
	.of(FaultToleranceMethod.CHKPT_PERFECT);

    public static final EnumSet<BiddingStrategy> bidd = EnumSet.of(BiddingStrategy.MEAN);

    public static final RuntimeEstimationMethod[] rt = { RuntimeEstimationMethod.RECENT_AVERAGE };

    public static final PriceForecastingMethod[] fc = { PriceForecastingMethod.PAST_N_DAYS_MEAN };

    public static final Integer[] jobs = { 100000 };

    public static final VolatilityCombo[] volats = { VOL_M_30_PCT, VOL_M_20_PCT, VOL_M_10_PCT,
	    VOL_P_10_PCT, VOL_P_20_PCT, VOL_P_30_PCT, VOL_P_40_PCT, VOL_P_50_PCT, VOL_P_60_PCT,
	    VOL_P_70_PCT };

    public static final Integer[] pess = { 1 };

    public static ArrayList<Long> seeds = new ArrayList<Long>();

    public static Integer[] multipliers = { 1 };

    static {
	for (int i = 10; i <= 40; i++) {
	    seeds.add((long) i);
	}
    }
}
