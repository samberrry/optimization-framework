package org.cloudbus.spotsim.payloads;

public class CloudRequest {

    private static long nextToken = 0;

    private final long token;

    public CloudRequest() {
	this(nextToken++);
    }

    public CloudRequest(final long token) {
	super();
	this.token = token;
    }

    public long getToken() {
	return this.token;
    }
    
    public void resetCloudRequestToken(){
    	CloudRequest.nextToken = 0;
    }
}
