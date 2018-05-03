package org.cloudbus.cloudsim.workflow.our;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

public class SpotPriceItem implements Serializable{
    private static final long serialVersionUID = -7175282494221572900L;

    @JsonProperty("Timestamp")
    private Date timestamp;

    @JsonProperty("AvailabilityZone")
    private String availabilityZone;

    @JsonProperty("InstanceType")
    private String instanceType;

    @JsonProperty("ProductDescription")
    private String productDescription;

    @JsonProperty("SpotPrice")
    private double spotPrice;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public double getSpotPrice() {
        return spotPrice;
    }

    public void setSpotPrice(double spotPrice) {
        this.spotPrice = spotPrice;
    }
}
