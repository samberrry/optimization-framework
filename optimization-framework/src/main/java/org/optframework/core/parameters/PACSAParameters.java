package org.optframework.core.parameters;

import org.cloudbus.cloudsim.util.workload.Workflow;
import org.optframework.core.Log;

public class PACSAParameters extends ProblemParameters {

    public int temperature;

    public PACSAParameters(Workflow workflow) {
        super(workflow);
        Log.logger.info("Initializes PACSA Algorithm Parameters");
    }
}
