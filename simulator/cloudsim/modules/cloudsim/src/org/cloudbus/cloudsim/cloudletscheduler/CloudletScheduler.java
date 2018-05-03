package org.cloudbus.cloudsim.cloudletscheduler;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletStatus;

public interface CloudletScheduler {

    /**
     * Updates the processing of cloudlets running under management of this
     * scheduler.
     * 
     * @return time predicted completion time of the earliest finishing
     *         cloudlet, or 0 if there is no next events
     * 
     * @pre currentTime >= 0
     * @post $none
     */
    long updateVmProcessing();

    /**
     * Receives an cloudlet to be executed in the VM managed by this scheduler.
     * 
     * @param gl
     *        the submited cloudlet
     * @return expected finish time of this cloudlet, or 0 if it is in a waiting
     *         queue
     * 
     * @pre gl != null
     * @post $none
     */
    long cloudletSubmit(Cloudlet gl);

    /**
     * Pauses execution of a cloudlet.
     * 
     * @param clId
     *        ID of the cloudlet being paused
     * 
     * @return $true if cloudlet paused, $false otherwise
     * 
     * @pre $none
     * @post $none
     */
    boolean cloudletPause(int clId);

    /**
     * Resumes execution of a paused cloudlet.
     * 
     * @param clId
     *        ID of the cloudlet being resumed
     * 
     * @return expected finish time of the cloudlet, 0.0 if queued
     * 
     * @pre $none
     * @post $none
     */
    long cloudletResume(int clId);

    /**
     * Gets the status of a cloudlet.
     * 
     * @param clId
     *        ID of the cloudlet
     * 
     * @return status of the cloudlet, -1 if cloudlet not found
     * 
     * @pre $none
     * @post $none
     */
    CloudletStatus getCloudletStatus(int clId);

    /**
     * Returns the number of cloudlets runnning in the virtual machine.
     * 
     * @return number of cloudlets runnning
     * 
     * @pre $none
     * @post $none
     */
    int runningCloudlets();

    /**
     * Returns one cloudlet to migrate to another vm.
     * 
     * @return one running cloudlet
     * 
     * @pre $none
     * @post $none
     */
    Cloudlet migrateCloudlet();

    /**
     * Get utilization created by all cloudlets.
     * 
     * @param time
     *        the time
     * 
     * @return total utilization
     */
    double getTotalUtilizationOfCpu(long time);

    /**
     * Gets the current requested mips.
     * 
     * @return the current mips
     */
    List<Double> getCurrentRequestedMips();

    /**
     * Gets the current mips share.
     * 
     * @return the current mips share
     */
    void pauseAllCloudlets();

    void resumeAllCloudlets();

    void cancelAllCloudlets();

    Cloudlet cloudletCancel(int cloudletId);

    Cloudlet cloudletSuspend(int cloudletId);

    /**
     * Informs about completion of some cloudlet in the VM managed by this
     * scheduler.
     * 
     * @return $true if there is at least one finished cloudlet; $false
     *         otherwise
     * 
     * @pre $none
     * @post $none
     */
    boolean areThereFinishedCloudlets();

    List<Cloudlet> getAndRemoveFinishedCloutlets();

    void mipsChanged(List<Double> allocatedMipsForVm);
}