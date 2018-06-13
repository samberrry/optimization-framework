package org.optframework.core.hbmo;

import java.util.ArrayList;
import java.util.List;

public class Spermatheca {
    List<Chromosome> chromosomeList;

    public Spermatheca() {
        chromosomeList = new ArrayList<>();
    }

    public List<Chromosome> getChromosomeList() {
        return chromosomeList;
    }

    public void setChromosomeList(List<Chromosome> chromosomeList) {
        this.chromosomeList = chromosomeList;
    }
}
