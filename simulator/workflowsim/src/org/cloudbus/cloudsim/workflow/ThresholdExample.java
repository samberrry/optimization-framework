package org.cloudbus.cloudsim.workflow;

public abstract class ThresholdExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double LTO = 5000;
		double CT = 0;
		
		double sigma = -0.00003645;
		double threshold = 1;
		
		double gamma = threshold * Math.exp(sigma*(LTO-CT));
		System.out.println("Gamma at CT=0 >>> " + gamma);
		double relaxedLTO = gamma*LTO;
		System.out.println("Relaxed LTO "+ relaxedLTO);
		
		CT = LTO/8;
		gamma = threshold * Math.exp(sigma*(LTO-CT));
		System.out.println("Gamma at CT=LTO/8 >>>" + gamma);
		relaxedLTO = gamma*LTO;
		System.out.println("Relaxed LTO "+ relaxedLTO);
		
		CT = LTO/4;
		gamma = threshold * Math.exp(sigma*(LTO-CT));
		System.out.println("Gamma at CT=LTO/4 >>>" + gamma);
		relaxedLTO = gamma*LTO;
		System.out.println("Relaxed LTO "+ relaxedLTO);
		
		CT = LTO/2;
		gamma = threshold * Math.exp(sigma*(LTO-CT));
		System.out.println("Gamma at CT=LTO/2 >>>" + gamma);
		relaxedLTO = gamma*LTO;
		System.out.println("Relaxed LTO "+ relaxedLTO);
		
		CT = LTO;
		gamma = threshold * Math.exp(sigma*(LTO-CT));
		System.out.println("Gamma at CT=LTO >>>" + gamma);
		relaxedLTO = gamma*LTO;
		System.out.println("Relaxed LTO "+ relaxedLTO);
		
	}

}
