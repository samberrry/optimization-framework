package org.cloudbus.cloudsim.util.workload;

import java.util.Comparator;

public class LSTcomparator implements Comparator<Job> {

	@Override
	public int compare(Job jobA, Job jobB) {
		long jobALST = jobA.getLatestStartTime();
		long jobBLST = jobB.getLatestStartTime();
		
		if (jobALST == jobBLST) {
			return 0;
		}
		if (jobALST < jobBLST) {
			return -1;
		} else {
			return +1;
		}
	}

}
