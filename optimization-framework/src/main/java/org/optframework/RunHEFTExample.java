package org.optframework;

import org.optframework.config.Config;
import org.optframework.core.*;
import org.optframework.core.heft.HEFTExampleAlgorithm;
import org.optframework.core.utils.PopulateWorkflow;
import org.optframework.core.utils.PreProcessor;
import org.optframework.core.utils.Printer;

public class RunHEFTExample {
    public static final int M_NUMBER = Config.global.m_number;

    public static void runHEFTExample(){

        Config.global.bandwidth = 1L;

        Workflow workflow = PreProcessor.doPreProcessingForHEFTExample(PopulateWorkflow.populateHEFTExample(Config.global.budget, 0));

        HEFTExampleAlgorithm heftExampleAlgorithm = new HEFTExampleAlgorithm(workflow);

        long start = System.currentTimeMillis();
        heftExampleAlgorithm.runAlgorithm();
        long stop = System.currentTimeMillis();

        Printer.printTime(stop-start);
    }
}
