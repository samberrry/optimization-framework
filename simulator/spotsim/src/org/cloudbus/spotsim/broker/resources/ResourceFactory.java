package org.cloudbus.spotsim.broker.resources;

import java.util.LinkedHashSet;
import java.util.Set;

import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.PriceModel;
import org.cloudbus.spotsim.enums.Region;

public class ResourceFactory {

    private final Set<Resource> usableResources;

    private final Set<Resource> nonUsableResources;

    public ResourceFactory() {
	this.usableResources = new LinkedHashSet<Resource>();
	this.nonUsableResources = new LinkedHashSet<Resource>();
    }

    public void destroy(final Resource resource) {
	boolean removed = this.nonUsableResources.remove(resource);
	if (!removed) {
	    throw new IllegalStateException("Resource "
		    + resource.getId()
		    + " of status "
		    + resource.getState()
		    + " was not found");
	}
	resource.setState(ResourceState.DESTROYED);
    }

    public Set<Resource> getAllUsableResources() {
	return this.usableResources;
    }

    public Set<Resource> getResourcesByState(final ResourceState status) {
	Set<Resource> ret = new LinkedHashSet<Resource>();
	for (Resource res : this.usableResources) {
	    if (res.getState() == status) {
		ret.add(res);
	    }
	}
	return ret;
    }

    public Resource newResource(Region region, AZ az, final InstanceType type, OS os,
	    final double bid) {
	final Resource resource = new Resource(type, os, PriceModel.SPOT, bid, az, region);
	this.usableResources.add(resource);
	return resource;
    }
    
    public Resource newResource(Region region, AZ az, final InstanceType type, OS os,
    	    final double bid, PriceModel pm) {
    	final Resource resource = new Resource(type, os,pm, bid, az, region);
    	this.usableResources.add(resource);
    	return resource;
        }

    public void updateResourceState(final Resource r, final ResourceState newState) {
	if (r.getState() != newState) {
	    if (newState == ResourceState.IDLE && r.hasTasks()) {
		throw new IllegalStateException("Cannot set resource status to IDLE as there are "
			+ r.getNumberOfRunningTasks()
			+ " running tasks and "
			+ r.getNumberOfScheduledTasks()
			+ " scheduled tasks");
	    }
	    if (!newState.isUsable()) {
		this.usableResources.remove(r);
		this.nonUsableResources.add(r);
	    }
	    r.setState(newState);
	}
    }

    public int getTotalNumberOfResources() {
	return this.usableResources.size();
    }
}
