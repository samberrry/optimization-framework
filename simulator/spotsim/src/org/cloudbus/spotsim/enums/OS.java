package org.cloudbus.spotsim.enums;

public enum OS {
    LINUX {
	@Override
	public String getAmazonName() {
	    return "Linux/UNIX";
	}

	@Override
	public String getNameForFile() {
	    return "linux";
	}
    };

    public abstract String getAmazonName();

    public abstract String getNameForFile();

    public static OS getDefault() {
	return LINUX;
    }
}