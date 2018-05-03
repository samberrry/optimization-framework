package org.cloudbus.spotsim.broker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.util.workload.Job;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.db.PriceDB;

/**
 * Utility class implementing Downey's speed up model of parallel programs, as
 * well as budget and deadline generation *
 * 
 * Speedup model: Allen Downey. A model for speedup of parallel programs. U.C.
 * Berkeley Technical Report CSD- 97-933, January 1997.
 * http://www.sdsc.edu/~downey/model/
 * 
 * Ported from C++ originally implemented by Cirne, and described In: W. Cirne
 * and F. Berman, A model for moldable supercomputer jobs, in Proceedings of the
 * IPDPS 2001: International Parallel and Distributed Processing Symposium, San
 * Francisco, CA, April 2001,
 * 
 * Code: http://www.cs.huji.ac.il/labs/parallel/workload/models.html#cirne01
 * 
 * 
 * @author William Voorsluys - williamvoor@gmail.com
 * 
 */
public class ModelParameters {

    private static final double DDL_HIGHER = 4D;

    private static final double DDL_LOWER = 1.5D;

    private static boolean linear = false;

    public static final String DOWNEY_JOBS = SimProperties.MODEL_PARAM_DOWNEY_JOBS_FILE.asString();

    public static double computeBudget(final Job j) {
	final double hours = Math.max(1D, j.getLength() / 3600D);
	final double onDemandPrice = PriceDB.getOnDemandPrice(Region.getDefault(),
	    InstanceType.M1SMALL, OS.getDefault());
	final double minimumSpotPrice = onDemandPrice / 3D;

	return hours * ModelParameters.randomInInterval(minimumSpotPrice, onDemandPrice);
    }

    public static long computeDeadline(final Job j) {

	final double rn = ModelParameters.randomInInterval(DDL_LOWER, DDL_HIGHER);
	return (long) Math.ceil(j.getSubmitTime() + Math.max(600, j.getReqRunTime() * rn));
    }

    /**
     * Execution time in parallel on 'n' nodes, given a known serial time
     */
    public static long execTimeParallel(final double A, final double sigma, final int n,
	    final long serialTime) {
	if (n == 1) {
	    return serialTime;
	}
	return (long) Math.ceil(serialTime / speedUp(A, sigma, n));
    }

    /**
     * Serial execution time on 1 node, given a known parallel execution time in
     * n nodes
     */
    public static long execTimeSerial(final double A, final double sigma, final int n,
	    final double parallelTime) {
	return Math.round(parallelTime * speedUp(A, sigma, n));
    }

    public static void generateRandomParams(final List<Job> jobs) throws IOException {
	int i = 0;
	final List<DowneyParams> downeyJobs = readDowneyJobs(new File(ModelParameters.DOWNEY_JOBS));
	for (final Job j : jobs) {
	    final long cloudletLength = j.getLength();
	    final long newLength = cloudletLength * j.getNumProc() * SimProperties.WORKLOAD_LENGTH_MULTIPLIER.asInt();
	    j.setLength(newLength);
	    j.setNumProc(1);
	    j.setReqRunTime(j.getReqRunTime() * SimProperties.WORKLOAD_LENGTH_MULTIPLIER.asInt());
	    j.setDeadline(computeDeadline(j));
	    j.setBudget(computeBudget(j));
	    j.setA(downeyJobs.get(i).getA());
	    j.setSigma(downeyJobs.get(i++).getSigma());
	}
    }

    /**
     * Maximum number of nodes that a Downey job can use
     */
    public static int maxNodes(final double A, final double sigma) {
	if (sigma <= 1.0) {
	    return (int) Math.ceil(2.0 * A - 1.0);
	}
	return (int) Math.ceil(A + A * sigma - sigma);
    }

    public static double randomInInterval(final double min, final double max) {
	return min + Config.RNG.nextDouble() * (max - min);
    }

    public static List<DowneyParams> readDowneyJobs(final File f) throws IOException {

	final List<DowneyParams> l = new ArrayList<DowneyParams>();

	final BufferedReader reader = new BufferedReader(new FileReader(f));

	String line = reader.readLine();
	while (line != null) {
	    final String[] split = line.split(",");
	    l.add(new DowneyParams(Double.parseDouble(split[0]), Double.parseDouble(split[1])));
	    line = reader.readLine();
	}
	reader.close();
	return l;
    }

    /**
     * Computes the speedup of a Downey job with average parallelism A and std
     * dev sigma on 'n' nodes
     */
    public static double speedUp(final double A, final double sigma, final int n) {
	if (n == 1) {
	    return 1D;
	}

	if (linear) {
	    return n;
	}

	if (sigma <= 1D) {
	    if (n <= A) {
		return A * n / (A + sigma / 2D * (n - 1D));
	    }
	    if (n <= 2D * A - 1D) {
		return A * n / (sigma * (A - 0.5) + n * (1D - sigma / 2D));
	    }
	    return A;
	}
	if (n <= A + A * sigma - sigma) {
	    return n * A * (sigma + 1D) / (sigma * (n + A - 1D) + A);
	}
	return A;
    }
}
