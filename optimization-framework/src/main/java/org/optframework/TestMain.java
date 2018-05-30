package org.optframework;

import org.cloudbus.spotsim.main.config.SimProperties;

public class TestMain {
    public static void main(String[] args) {
        SimProperties s = SimProperties.PRICING_HISTORY_MAP_DIR;

        System.out.println(SimProperties.PRICING_HISTORY_MAP_DIR.getValue());
        System.out.println();
    }
}
