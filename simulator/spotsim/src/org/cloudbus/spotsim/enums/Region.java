package org.cloudbus.spotsim.enums;

import java.util.EnumSet;

public enum Region {

    US_EAST {
	@Override
	public String getAmazonName() {
	    return "us-east-1";
	}

	@Override
	public EnumSet<AZ> getAvailabilityZones() {
	    return EnumSet.of(AZ.A, AZ.B, AZ.C, AZ.D);
	}
    },
    US_WEST {
	@Override
	public String getAmazonName() {
	    return "us-west-1";
	}

	@Override
	public EnumSet<AZ> getAvailabilityZones() {
	    return EnumSet.of(AZ.A, AZ.B, AZ.C);
	}
    },

    US_WEST_OREGON {
	@Override
	public String getAmazonName() {
	    return "us-west-2";
	}

	@Override
	public EnumSet<AZ> getAvailabilityZones() {
	    return EnumSet.of(AZ.A, AZ.B);
	}
    },
    EUROPE {
	@Override
	public String getAmazonName() {
	    return "eu-west-1";
	}

	@Override
	public EnumSet<AZ> getAvailabilityZones() {
	    return EnumSet.of(AZ.A, AZ.B, AZ.C);
	}
    },
    APAC_SING {
	@Override
	public String getAmazonName() {
	    return "ap-southeast-1";
	}

	@Override
	public EnumSet<AZ> getAvailabilityZones() {
	    return EnumSet.of(AZ.A, AZ.B);
	}
    },
    APAC_JAPAN {
	@Override
	public String getAmazonName() {
	    return "ap-northeast-1";
	}

	@Override
	public EnumSet<AZ> getAvailabilityZones() {
	    return EnumSet.of(AZ.A, AZ.B);
	}
    },
    SA_EAST {
	@Override
	public String getAmazonName() {
	    return "sa-east-1";
	}

	@Override
	public EnumSet<AZ> getAvailabilityZones() {
	    return EnumSet.of(AZ.A, AZ.B);
	}
    },
    DEEPAK_TEST {
    @Override
    public String getAmazonName() {
    	return "deepak-test-1";
    }

    @Override
    public EnumSet<AZ> getAvailabilityZones() {
    	return EnumSet.of(AZ.ANY);
    }
    };

    public abstract String getAmazonName();

    public abstract EnumSet<AZ> getAvailabilityZones();

    public static Region getDefault() {
	return US_EAST;
    }
}