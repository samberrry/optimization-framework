package org.cloudbus.spotsim.pricing.distr;

import static org.cloudbus.spotsim.main.config.SimProperties.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import org.apache.commons.math.random.RandomGenerator;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.TraceDistribution;
import org.cloudbus.utils.data.enummaps.TripleEnumMap;

public class RandomDistributionManager {

    private static TripleEnumMap<Region, AZ, InstanceType, RandomDistribution> priceDistMap;

    private static TripleEnumMap<Region, AZ, InstanceType, RandomDistribution> interPriceDistMap;

    public static RandomDistribution getInterPriceDistr(final Region region, AZ az,
	    InstanceType type, RandomGenerator rng) {
	return getInterPriceDistr(region, az, rng).get(type);
    }

    public static Map<InstanceType, RandomDistribution> getInterPriceDistr(final Region region,
	    AZ az, RandomGenerator rng) {
	if (interPriceDistMap == null || interPriceDistMap.isEmpty()) {
	    load(rng);
	}
	return interPriceDistMap.get(region, az);
    }

    public static RandomDistribution getPriceDistr(final Region region, final AZ az,
	    InstanceType type, RandomGenerator rng) {
	return getPriceDistr(region, az, rng).get(type);
    }

    public static Map<InstanceType, RandomDistribution> getPriceDistr(final Region region,
	    final AZ az, RandomGenerator rng) {
	if (priceDistMap == null || priceDistMap.isEmpty()) {
	    load(rng);
	}
	if (!priceDistMap.containsKeys(region, az)) {
	    throw new IllegalArgumentException("There are no random prices for "
		    + region
		    + "."
		    + az);
	}
	return priceDistMap.get(region, az);
    }

    public static void load(RandomGenerator rng) {

	priceDistMap = new TripleEnumMap<Region, AZ, InstanceType, RandomDistribution>();
	interPriceDistMap = new TripleEnumMap<Region, AZ, InstanceType, RandomDistribution>();

	final Region reg = (Region) DC_DEFAULT_REGION.asEnum();
	EnumSet<AZ> azs = reg.getAvailabilityZones();
	for (AZ az : azs) {
	    try {
		switch ((TraceDistribution) PRICING_PRICE_DISTR.asEnum()) {
		case EXPONENTIAL:
		    priceDistMap.put(
			reg,
			az,
			genExpDistributionMap(PRICING_PRICE_DISTR_FILE.asString(),
			    PRICING_PRICE_MU_MULT.asDouble(), rng));
		    break;
		case MIXTURE_OF_GAUSSIANS:
		    priceDistMap.put(
			reg,
			az,
			genGaussianDistributionMap(PRICING_PRICE_DISTR_FILE.asString(),
			    PRICING_PRICE_MU_MULT.asDouble(), PRICING_PRICE_SIGMA_MULT.asDouble(),
			    rng));
		    break;
		}

		switch ((TraceDistribution) PRICING_INTER_PRICE_DISTR.asEnum()) {
		case EXPONENTIAL:
		    interPriceDistMap.put(
			reg,
			az,
			genExpDistributionMap(PRICING_INTER_PRICE_DISTR_FILE.asString(),
			    PRICING_TIME_MU_MULT.asDouble(), rng));
		    break;
		case MIXTURE_OF_GAUSSIANS:
		    interPriceDistMap.put(
			reg,
			az,
			genGaussianDistributionMap(PRICING_INTER_PRICE_DISTR_FILE.asString(),
			    PRICING_TIME_MU_MULT.asDouble(), PRICING_TIME_SIGMA_MULT.asDouble(),
			    rng));
		    break;
		}
	    } catch (IOException ioe) {
		throw new RuntimeException(ioe);
	    }
	}
    }

    private static EnumMap<InstanceType, RandomDistribution> genGaussianDistributionMap(
	    final String file, double muMult, double sigmaMult, RandomGenerator rng)
	    throws FileNotFoundException, IOException {
	final EnumMap<InstanceType, RandomDistribution> distMap = new EnumMap<InstanceType, RandomDistribution>(
	    InstanceType.class);
	final BufferedReader r = new BufferedReader(new FileReader(file));
	String line = r.readLine();
	while (line != null) {
	    final String[] split = line.split(" ");
	    final InstanceType type = InstanceType.fromString(split[0]);
	    if (type == null) {
		throw new IllegalArgumentException("Unknown instance type " + split[0]);
	    }
	    final String[] params = new String[split.length - 1];
	    System.arraycopy(split, 1, params, 0, split.length - 1);
	    final MixtureModel mixtureModel = new MixtureModel(3, params);
	    distMap.put(type, new MixtureOfGaussians(mixtureModel, muMult, sigmaMult, rng));
	    line = r.readLine();
	}
	return distMap;
    }

    private static EnumMap<InstanceType, RandomDistribution> genExpDistributionMap(
	    final String file, double muMult, RandomGenerator rng) throws FileNotFoundException,
	    IOException {
	final EnumMap<InstanceType, RandomDistribution> distMap = new EnumMap<InstanceType, RandomDistribution>(
	    InstanceType.class);
	final BufferedReader r = new BufferedReader(new FileReader(file));
	String line = r.readLine();
	while (line != null) {
	    final String[] split = line.split(" ");
	    final InstanceType type = InstanceType.fromString(split[0]);
	    if (type == null) {
		throw new IllegalArgumentException("Unknown instance type " + split[0]);
	    }
	    final String params = split[1];
	    distMap.put(type, new Exponential(Double.parseDouble(params), muMult, rng));
	    line = r.readLine();
	}
	return distMap;
    }
    
    public static void flush(){
    	interPriceDistMap.clear();
    	priceDistMap.clear();
    }
}
