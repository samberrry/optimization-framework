package org.cloudbus.spotsim.enums;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.spotsim.pricing.distr.MixtureModel;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Describes the possible instance hardware types
 */
@XStreamAlias("itype")
public enum InstanceType {

    // T1MICRO(613, 1, 1, 160, 1000, Bits.B32, "m1.small", 1) {
    //
    // @Override
    // public double getOnDemandPrice(Region r, OS os) {
    // return 0;
    // }
    // },
    // Standard instances
    M1SMALL(1740, 1, 1, 160, 1000, Bits.B32, "m1.small", 1) {
	@Override
	public double getOnDemandPrice(final Region r, final OS os) {
	    return 0.065;
	}

	@Override
	public double getMinimumSpotPossible(Region r, OS os) {
	    return 0.027;
	}

	@Override
	public double getMaximumSpotPossible(Region r, OS os) {
	    return 0.2;
	}
    },
    
    M1MEDIUM(3840, 2, 2, 410, 1000, Bits.B32, "m1.medium", 1) {
    	@Override
    	public double getOnDemandPrice(final Region r, final OS os) {
    	    return 0.130;
    	}

    	@Override
    	public double getMinimumSpotPossible(Region r, OS os) {
    	    return 0.027;
    	}

    	@Override
    	public double getMaximumSpotPossible(Region r, OS os) {
    	    return 0.2;
    	}
        },

    M1LARGE(7680, 4, 2, 850, 1000, Bits.B64, "m1.large", 1) {
	@Override
	public double getOnDemandPrice(final Region r, final OS os) {
	    return 0.260;
	}

	@Override
	public double getMinimumSpotPossible(Region r, OS os) {
	    return 0.108;
	}

	@Override
	public double getMaximumSpotPossible(Region r, OS os) {
	    return 4D;
	}
    },

    M1XLARGE(15360, 8, 4, 1690, 1000, Bits.B64, "m1.xlarge", 1) {
	@Override
	public double getOnDemandPrice(final Region r, final OS os) {
	    return 0.520;
	}

	@Override
	public double getMinimumSpotPossible(Region r, OS os) {
	    return 0.216;
	}

	@Override
	public double getMaximumSpotPossible(Region r, OS os) {
	    return 10D;
	}
    },

    // High-memory instances

    M2XLARGE(17510, 6, 2, 420, 1000, Bits.B64, "m2.xlarge", 1) {

	@Override
	public double getOnDemandPrice(final Region r, final OS os) {
	    return 0.460;
	}

	@Override
	public double getMinimumSpotPossible(Region r, OS os) {
	    return 0.153;
	}

	@Override
	public double getMaximumSpotPossible(Region r, OS os) {
	    return 10;
	}
    },

    M22XLARGE(35020, 13, 4, 850, 1000, Bits.B64, "m2.2xlarge", 1) {

	@Override
	public double getOnDemandPrice(final Region r, final OS os) {
	    return 0.920;
	}

	@Override
	public double getMinimumSpotPossible(Region r, OS os) {
	    return 0.399;
	}

	@Override
	public double getMaximumSpotPossible(Region r, OS os) {
	    return 2.5;
	}
    },

    M24XLARGE(70041, 26, 8, 1690, 1000, Bits.B64, "m2.4xlarge", 1) {

	@Override
	public double getOnDemandPrice(final Region r, final OS os) {
	    return 1.840;
	}

	@Override
	public double getMinimumSpotPossible(Region r, OS os) {
	    return 0.759;
	}

	@Override
	public double getMaximumSpotPossible(Region r, OS os) {
	    return 10;
	}
    },
    
    M3XLARGE(15360, 13, 4, 1690, 1000, Bits.B64, "m3.xlarge", 1) {

    	@Override
    	public double getOnDemandPrice(final Region r, final OS os) {
    	    return 0.550;
    	}

    	@Override
    	public double getMinimumSpotPossible(Region r, OS os) {
    	    return 0.759;
    	}

    	@Override
    	public double getMaximumSpotPossible(Region r, OS os) {
    	    return 10;
    	}
    },
    
    M32XLARGE(30720, 26, 8, 1690, 1000, Bits.B64, "m3.2xlarge", 1) {

    	@Override
    	public double getOnDemandPrice(final Region r, final OS os) {
    	    return 1.100;
    	}

    	@Override
    	public double getMinimumSpotPossible(Region r, OS os) {
    	    return 0.759;
    	}

    	@Override
    	public double getMaximumSpotPossible(Region r, OS os) {
    	    return 10;
    	}
    },

//	******************** HESSAM COMMENTED THESE LINES

    // High-CPU instances
//    C1MEDIUM(1740, 5, 2, 320, 1000, Bits.B32, "c1.medium", 1) {
//	@Override
//	public double getOnDemandPrice(final Region r, final OS os) {
//	    return 0.165;
//	}
//
//	@Override
//	public double getMinimumSpotPossible(Region r, OS os) {
//	    return 0.06;
//	}
//
//	@Override
//	public double getMaximumSpotPossible(Region r, OS os) {
//	    return 1D;
//	}
//    },

//    C1XLARGE(7189, 20, 8, 1690, 1000, Bits.B64, "c1.xlarge", 1) {
//	@Override
//	public double getOnDemandPrice(final Region r, final OS os) {
//	    return 0.660;
//	}
//
//	@Override
//	public double getMinimumSpotPossible(Region r, OS os) {
//	    return 0.24;
//	}
//
//	@Override
//	public double getMaximumSpotPossible(Region r, OS os) {
//	    return 2D;
//	}
//    },

//	********************************* END OF HESSAM COMMENTS



    // Cluster compute instances

    // CC14XLARGE(23552, 33, 8, 1690, 10000, Bits.B64, "cc1.4xlarge", 1) {
    // @Override
    // public double getOnDemandPrice(Region r, OS os) {
    // return 0.68;
    // }
    // },
    //
    // // Cluster GPU instances
    //
    // CG14XLARGE(22528, 33, 8, 1690, 10000, Bits.B64, "cg1.4xlarge", 1) {
    // @Override
    // public double getOnDemandPrice(Region r, OS os) {
    // return 0.68;
    // }
    // }
    ;

    public static final InstanceType maxCPUUnitsInstance = M24XLARGE;

    public static final InstanceType maxCoresUnitsInstance = M24XLARGE;

    public static final InstanceType maxMemoryInstance = M24XLARGE;

    /** Memory in MB */
    private final int mem;

    /**
     * Total number of EC2 compute units (equivalent to Number of cores * EC2
     * compute units per core
     */
    private final int ec2units;

    /** Number of CPU cores */
    private final int cores;

    /** Local disk storage in GB */
    private final long storage;

    /** 32 or 64 bits */
    private final Bits bits;

    /** Network bandwidth */
    private final long bw;

    private final String name;

    private final double computePowerPerUnit;

    private final long suspendOverhead;

    private final long resumeOverhead;

    /**
     * The rates, in MB/s that this datacenter takes to suspend, or resume, a VM
     * by serializing its memory to network storage
     */
    public static final double S_RATE = 63.67 * 4;

    public static final double R_RATE = 81.27;

    InstanceType(final int mem, final int ec2units, final int cores, final long storage,
	    final long bw, final Bits bits, final String name, final double gFlopsPerUnit) {
	this.mem = mem;
	this.ec2units = ec2units;
	this.cores = cores;
	this.storage = storage;
	this.bw = bw;
	this.bits = bits;
	this.name = name;
	this.computePowerPerUnit = gFlopsPerUnit;
	this.suspendOverhead = (long) Math.ceil(mem / S_RATE);
	this.resumeOverhead = (long) Math.ceil(mem / R_RATE);
    }

    public static InstanceType fromString(final String s) {

	for (final InstanceType instanceType : values()) {
	    if (instanceType.getName().equals(s)) {
		return instanceType;
	    }
	}
	return null;
    }

    public static List<String> getAllTypeNames() {
	final List<String> allTypes = new ArrayList<String>();
	for (final InstanceType type : InstanceType.values()) {
	    allTypes.add(type.getName());
	}
	return allTypes;
    }

    public static InstanceType mostEconomical(final int maxParallelism, final long length,
	    final Region region, final OS os) {
	InstanceType cheapest = M1SMALL;
	for (final InstanceType t : values()) {
	    final double pricePerUnit = t.getOnDemandPrice(region, os) / t.getEc2units();
	    final double cheapPricePerUnit = cheapest.getOnDemandPrice(region, os)
		    / cheapest.getEc2units();
	    if (maxParallelism >= t.getEc2units() && pricePerUnit <= cheapPricePerUnit) {
		cheapest = t;
	    }
	}
	return cheapest;
    }

    public long getBandwidth() {
	return this.bw;
    }

    public Bits getBits() {
	return this.bits;
    }

    public double getComputePower() {
	return getEc2units() * getComputePowerPerUnit();
    }

    public double getComputePowerPerCore() {
	return getComputePower() / getCores();
    }

    public double getComputePowerPerUnit() {
	return this.computePowerPerUnit;
    }

    public int getCores() {
	return this.cores;
    }

    public int getEc2units() {
	return this.ec2units;
    }

    public int getMem() {
	return this.mem;
    }

    public String getName() {
	return this.name;
    }

    public abstract double getMinimumSpotPossible(Region r, OS os);

    public abstract double getMaximumSpotPossible(Region r, OS os);

    public abstract double getOnDemandPrice(Region r, OS os);

    public MixtureModel getPriceMixtureModel(final Region region, final OS os) {
	throw new UnsupportedOperationException();
    }

    public double getReservedPrice(final Region region) {
	throw new UnsupportedOperationException("Reserved prices are not available");
    }

    public long getResumeOverhead() {
	return this.resumeOverhead;
    }

    public long getStorage() {
	return this.storage;
    }

    public long getSuspendOverhead() {
	return 0;
	// return this.suspendOverhead;
    }

    public MixtureModel getTimeMixtureModel(final Region region, final OS os) {
	throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
	return getName();
    }
}