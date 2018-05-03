package org.cloudbus.cloudsim;

public class DataCloudConstants {

    /** Default Maximum Transmission Unit (MTU) of a link in bytes */
    public static final int DEFAULT_MTU = 1500;

    /** The default packet size (in byte) for sending events to other entity. */
    public static final int PKT_SIZE = DEFAULT_MTU * 100; // in bytes

    /** The default storage size (10 GByte) */
    public static final int DEFAULT_STORAGE_SIZE = 10000000; // 10 GB in bytes
}
