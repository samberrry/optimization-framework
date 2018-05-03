package org.cloudbus.cloudsim.workflow.our;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SpotPriceHistory implements Serializable{
    private static final long serialVersionUID = 3867028786237534566L;

    @JsonProperty("SpotPriceHistory")
    private List<SpotPriceItem> priceList = new ArrayList<>();

    public List<SpotPriceItem> getPriceList() {
        return priceList;
    }

    public void setPriceList(List<SpotPriceItem> priceList) {
        this.priceList = priceList;
    }
}
