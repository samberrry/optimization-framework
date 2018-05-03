package org.cloudbus.spotsim.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.cloudbus.spotsim.enums.BiddingStrategy;
import org.cloudbus.spotsim.enums.FaultToleranceMethod;
import org.cloudbus.spotsim.main.config.SimProperties;

public class ScenarioMaker {

    public static void main(final String... args) throws Exception {

	// for (String _seed : seeds) {
	// for (String _jobs : jobs) {
	// final String key = "SPOTSIM_"
	// + _seed
	// + '_'
	// + _jobs
	// + '_'
	// + "NO-RT"
	// + '_'
	// + "SINGLE_TYPE";
	// final String pathname = "simprops/" + key + ".properties";
	// System.out.println(pathname);
	// File f = new File(pathname);
	// PrintWriter wr = new PrintWriter(new BufferedWriter(new
	// FileWriter(f)));
	// wr.println(SimProperties.PROP_SEED + '=' + _seed);
	// wr.println(SimProperties.PROP_JOBS + '=' + _jobs);
	// wr.println(SimProperties.PROP_POLICY_CLASS
	// + '='
	// + "org.cloudbus.spotsim.cloud.broker.policies.SingleMachineType");
	// wr.println(SimProperties.PROP_POLICY_NAME + '=' + "SINGLE_TYPE");
	// wr.close();
	// }
	// }

	for (final Long _seed : Sweep.seeds) {
	    for (final Integer _mult : Sweep.multipliers) {
		for (final Integer _jobs : Sweep.jobs) {
		    for (final FaultToleranceMethod _ft : Sweep.ft) {
			for (final BiddingStrategy _bidd : Sweep.bidd) {
			    for (final Integer _pess : Sweep.pess) {
				for (final VolatilityCombo _volat : Sweep.volats) {
				    final String key = "SPOTSIM_"
					    + _seed
					    + '_'
					    + _jobs
					    + '_'
					    + _ft
					    + '_'
					    + _bidd
					    + '_'
					    + _pess
					    + '_'
					    + _mult
					    + "_vol_"
					    + _volat.getPriceMuMult()
					    + '_'
					    + _volat.getPriceSigmaMult()
					    + '_'
					    + _volat.getTimeMuMult();
				    final String pathname = "simprops/" + key + ".properties";
				    System.out.println(pathname);
				    final File f = new File(pathname);
				    final PrintWriter wr = new PrintWriter(new BufferedWriter(
					new FileWriter(f)));
				    wr.println(SimProperties.RNG_SEED.getName() + '=' + _seed);
				    wr.println(SimProperties.WORKLOAD_JOBS.getName() + '=' + _jobs);
				    wr.println(SimProperties.FT_METHOD.getName() + '=' + _ft);
				    final String policyName = SimProperties.SCHED_POLICY_CLASS
					.asString();
				    String name = policyName.substring(policyName.lastIndexOf('.'));
				    wr.println(name + '=' + "ONE");
				    wr.println(SimProperties.SCHED_BIDDING_STRAT.getName()
					    + '='
					    + _bidd);
				    wr.println(SimProperties.SCHED_PESSIMIST_FACTOR.getName()
					    + '='
					    + _pess);
				    wr.println(SimProperties.WORKLOAD_LENGTH_MULTIPLIER.getName()
					    + '='
					    + _mult);

				    // volat
				    wr.println(SimProperties.PRICING_PRICE_MU_MULT.getName()
					    + '='
					    + _volat.getPriceMuMult());
				    wr.println(SimProperties.PRICING_PRICE_SIGMA_MULT.getName()
					    + '='
					    + _volat.getPriceSigmaMult());
				    wr.println(SimProperties.PRICING_TIME_MU_MULT.getName()
					    + '='
					    + _volat.getTimeMuMult());
				    wr.close();
				}
			    }
			}
		    }
		}
	    }
	}
    }
}
