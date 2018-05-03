package org.cloudbus.spotsim.pricing.db;

import static org.cloudbus.spotsim.main.config.SimProperties.SIM_END_TIME;
import static org.cloudbus.spotsim.main.config.SimProperties.SIM_START_TIME;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.workflow.our.HistoryPriceManagement;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.spothistory.SpotPriceRecord;
import org.cloudbus.utils.data.enummaps.DoubleEnumMap;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.persistence.FilePersistenceStrategy;
import com.thoughtworks.xstream.persistence.XmlMap;

/**
 * 
 * XML-backed database of spot price history of Amazon EC2.
 * 
 * @author William Voorsluys - williamvoor@gmail.com
 * 
 */
public class HistoryPersistenceManager {

    private Map<String, SortedSet<SpotPriceRecord>> pricesDB = null;

    private final String dir;

    public HistoryPersistenceManager(String dir) {
	super();
	this.dir = dir;
	loadFromXml();
    }

    public void addToDb(final Region region, final AZ az, final InstanceType type, final OS os,
	    final SortedSet<SpotPriceRecord> priceSet) {
	final String key = key(region, az, type, os);
	SortedSet<SpotPriceRecord> sortedSet = this.pricesDB.get(key);
	if (sortedSet == null) {
	    sortedSet = new TreeSet<>(priceSet);
	} else {
	    sortedSet.addAll(priceSet);
	}
	this.pricesDB.put(key, sortedSet);
    }

    public boolean contains(final Region region, final AZ az, final InstanceType type, final OS os) {
	return this.pricesDB.containsKey(key(region, az, type, os));
    }

    /** NYI */
    public void deleteRange(final Calendar from, final Calendar to) {

    }

    public SortedSet<SpotPriceRecord> getFromDb(final Region region, final AZ az,
	    final InstanceType type, final OS os) {
	final String key = key(region, az, type, os);
	return this.pricesDB.get(key);
    }

    public DoubleEnumMap<InstanceType, OS, NavigableSet<PriceRecord>> getFromDb(
	    final Region region, final AZ az) {

	DoubleEnumMap<InstanceType, OS, NavigableSet<PriceRecord>> ret = new DoubleEnumMap<InstanceType, OS, NavigableSet<PriceRecord>>();

	EnumSet<InstanceType> types = EnumSet.allOf(InstanceType.class);
	EnumSet<OS> oss = EnumSet.allOf(OS.class);
	for (OS os : oss) {
	    for (InstanceType type : types) {
		final String key = key(region, az, type, os);
		if (this.pricesDB.containsKey(key)) {
		    SortedSet<SpotPriceRecord> subSet = this.pricesDB.get(key).subSet(
			new SpotPriceRecord(SIM_START_TIME.asDate(), -1),
			new SpotPriceRecord(SIM_END_TIME.asDate(), -1));
		   /*// if(subSet.isEmpty()){
		    	SpotPriceRecord last = this.pricesDB.get(key).last();
		    	SpotPriceRecord first = this.pricesDB.get(key).first();
		    	System.out.println("Type" + type+ " First " + first.getDate().getTime() + " Last " + last.getDate().getTime());
		   // }
		    System.out.println("HPM Start Date " + SIM_START_TIME.asDate().getTime() + " End Time " + SIM_END_TIME.asDate().getTime());*/
		    NavigableSet<PriceRecord> recordSet = new TreeSet<>();
		    for (SpotPriceRecord spotPriceRecord : subSet) {
			recordSet.add(new PriceRecord(spotPriceRecord));
		    }
		    ret.put(type, os, recordSet);
		}
	    }
	}
	return ret;
    }

    @SuppressWarnings("unchecked")
    private void loadFromXml() {
	System.out.println("Loading prices from XML");
	File dirF = new File(this.dir);
	if (!dirF.exists()) {
	    dirF.mkdirs();
	}

	//HESSAM: to check whether file creation works
//	String newFile = dir+ "/myText.txt";
//	File newf = new File(newFile);
//
//	try {
//		newf.createNewFile();
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
	//END HESSAM


		this.pricesDB = HistoryPriceManagement.populatePriceDB();

//	COMMENTTEED
//		final XStream xstream = new XStream();
//	xstream.processAnnotations(SpotPriceRecord.class);
//	this.pricesDB = new XmlMap(new FilePersistenceStrategy(dirF, xstream));
//		Map<String, SortedSet<SpotPriceRecord>> pricesDB
		//COMMENTED

    }

    public static String key(final Region region, final AZ availabilityZone,
	    final InstanceType type, final OS os) {
	return region.getAmazonName()
		+ (availabilityZone == null ? "" : availabilityZone.toString().toLowerCase())
		+ '.'
		+ os.getAmazonName()
		+ '.'
		+ type.getName();
    }
}
