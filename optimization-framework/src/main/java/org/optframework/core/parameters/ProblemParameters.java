package org.optframework.core.parameters;

import org.cloudbus.cloudsim.util.workload.Workflow;
import java.util.List;

    /**
     * This Class contains Parameters related to the problem
     * */

public abstract class ProblemParameters {

    /**
     * A workflow is represented by a Directed Acyclic Graph (DAG), G = (T;E), where T indicates a set of
     * nodes, and each node is a task.
     * */
    public Workflow workflow;

        public ProblemParameters(Workflow workflow) {
            this.workflow = workflow;
        }
    }
