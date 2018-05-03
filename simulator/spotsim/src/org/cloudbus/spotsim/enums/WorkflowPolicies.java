/**
 * 
 */
package org.cloudbus.spotsim.enums;

/**
 * @author deepakc
 *
 */
public enum WorkflowPolicies {

	//CONSERVATIVE("org.cloudbud.cloudsim.workflow.policies.ConservativeDynamicPolicy", "conservative"),
//	LIBERAL("org.cloudbud.cloudsim.workflow.policies.ModLiberalDynamicPolicy", "liberal"),
	//BASELINE("org.cloudbud.cloudsim.workflow.policies.BaselinePolicy", "baseline"),
	
	NEW_CONSERVATIVE("org.cloudbus.cloudsim.workflow.Newpolicies.NewConservativePolicy", "NewConservative"),
	NEW_LIBERAL("org.cloudbus.cloudsim.workflow.Newpolicies.NewLiberalPolicy", "NewLiberal"),
	NEW_BASELINE("org.cloudbus.cloudsim.workflow.Newpolicies.NewBaselinePolicy", "NewBaseline"),
	CONSERVATIVE_NAIVE("org.cloudbus.cloudsim.workflow.Newpolicies.ConservativeNaiveBiddingPolicy", "ConservativeNaiveBidding"),
	LIBERAL_NAIVE("org.cloudbus.cloudsim.workflow.Newpolicies.LiberalNaiveBiddingPolicy", "LiberalNaiveBidding"),
	NEW_BASELINE_SPOT("org.cloudbus.cloudsim.workflow.Newpolicies.NewSpotBaselinePolicy", "NewSpotBaseline"),
	CONSERVATIVE_ODB("org.cloudbus.cloudsim.workflow.Newpolicies.NewConservativeODBPolicy", "NewConservativeODB"),
	LIBERAL_ODB("org.cloudbus.cloudsim.workflow.Newpolicies.NewLiberalODBPolicy", "NewLiberalODB"),
	TASKDUP("org.cloudbus.cloudsim.workflow.Newpolicies.TaskDuplicationSchedulingPol", "TaskDuplicationSchPol"),
	TASKDUPALL("org.cloudbus.cloudsim.workflow.Newpolicies.TaskDuplicationAllSchedulingPol", "TaskDuplicationAllSchPol");
	private final String classNameString;
	private final String policyName;
	
	WorkflowPolicies(String className, String policyName){
		this.classNameString = className;
		this.policyName = policyName;
	}
	
	public String getClassName(){
		return this.classNameString;
	}
	
	public String getPolicyName(){
		return this.policyName;
	}
	@Override
	public String toString() {
		return this.classNameString;
	}
}
