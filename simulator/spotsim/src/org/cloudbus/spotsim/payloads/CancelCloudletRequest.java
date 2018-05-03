package org.cloudbus.spotsim.payloads;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.spotsim.cloudprovider.instance.Instance;

public class CancelCloudletRequest {

    private final Instance instance;

    private final Cloudlet jobReplica;

    public CancelCloudletRequest(final Instance instance, final Cloudlet jobReplica) {
	this.instance = instance;
	this.jobReplica = jobReplica;
    }

    public Instance getInstance() {
	return this.instance;
    }

    public Cloudlet getJobReplica() {
	return this.jobReplica;
    }
}
