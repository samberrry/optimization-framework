package org.cloudbus.cloudsim.core.eventlist;

import org.cloudbus.cloudsim.core.SimEvent;

public abstract class AbstractEventList implements EventList {

    private long serial = 0;

    @Override
    public boolean put(SimEvent newEvent) {
	newEvent.setSerial(this.serial++);
	return insert(newEvent);
    }

    protected abstract boolean insert(SimEvent newEvent);
}
