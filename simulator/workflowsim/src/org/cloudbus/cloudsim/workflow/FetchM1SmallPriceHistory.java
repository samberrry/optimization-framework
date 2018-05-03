package org.cloudbus.cloudsim.workflow;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SortedSet;
import java.util.TimeZone;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.db.HistoryPersistenceManager;
import org.cloudbus.spotsim.pricing.db.TheGrabber;
import org.cloudbus.spotsim.spothistory.SpotPriceRecord;

import com.thoughtworks.xstream.XStream;

public class FetchM1SmallPriceHistory {

	private HistoryPersistenceManager historyPersistenceManager;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		try {
			Log.init(null);
			Config.load();
			//fetchOneType();
			GregorianCalendar from = new GregorianCalendar();
			from.set(2014, 6, 15);
			
			GregorianCalendar to = new GregorianCalendar();
			to.set(2014, 9, 15);
			for(InstanceType type : InstanceType.values()){
				fetchOneType(type, from , to);
			}
			/*GregorianCalendar from = new GregorianCalendar();
			from.set(2013, 8, 24);
			
			GregorianCalendar to = new GregorianCalendar();
			to.set(2013, 9, 27);
			
			System.out.println(" from " + from.toString() + " to " + to.toString());
			fetchOneType(InstanceType.M1SMALL, from, to);*/
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

	}
	
	public static void fetchOneType(InstanceType type, GregorianCalendar from, GregorianCalendar to) throws Exception {
		final SortedSet<SpotPriceRecord> newPrices = TheGrabber.fetchSpotHistory(Region.US_EAST,
		    AZ.C, type, OS.LINUX, from, to);

		System.out.println("Returned prices for: "
			+ Region.US_EAST.getAmazonName()
			+ "C"
			+ "-"
			+ type
			+ "-"
			+ OS.LINUX
			+ " from "
			+ Config.formatDate(newPrices.first().getDate())
			+ " to "
			+ Config.formatDate(newPrices.last().getDate()));
		
		XStream x = new XStream();
		x.processAnnotations(SpotPriceRecord.class);

		String fileName = "C:\\Documents and Settings\\deepakc\\My Documents\\TaskDuplicationResults\\LatestSpotPrices\\string@deepak-test-1any.Linux%2FUNIX.";
		fileName += "june15-Sep15";
		fileName += type.toString();
		fileName += ".xml";
		System.out.println("filename " + fileName);
		FileWriter fw = new FileWriter(fileName);
		PrintWriter out = new PrintWriter(fw);
		String xml = x.toXML(newPrices);
		out.println(xml);
		
		fw.close();
		out.close();
	    }
	
	public static void fetchOneType(InstanceType type) throws Exception {
		final SortedSet<SpotPriceRecord> newPrices = TheGrabber.fetchSpotHistory(Region.US_EAST,
		    AZ.A, type, OS.LINUX, SimProperties.SIM_START_TIME.asDate(),
		    SimProperties.SIM_END_TIME.asDate());

		System.out.println("Returned prices for: "
			+ Region.US_EAST.getAmazonName()
			+ "a"
			+ "-"
			+ type
			+ "-"
			+ OS.LINUX
			+ " from "
			+ Config.formatDate(newPrices.first().getDate())
			+ " to "
			+ Config.formatDate(newPrices.last().getDate()));
		
		XStream x = new XStream();
		x.processAnnotations(SpotPriceRecord.class);

		String fileName = "C:\\Documents and Settings\\deepakc\\My Documents\\Dropbox\\Spot Market paper\\prices_csv\\latest\\string@deepak-test-1any.Linux%2FUNIX.";
		fileName += type.toString();
		fileName += ".xml";
		System.out.println("filename " + fileName);
		FileWriter fw = new FileWriter(fileName);
		PrintWriter out = new PrintWriter(fw);
		String xml = x.toXML(newPrices);
		out.println(xml);
		
		fw.close();
		out.close();
	    }

	public static void fetchOneType() throws Exception {
		final SortedSet<SpotPriceRecord> newPrices = TheGrabber.fetchSpotHistory(Region.US_WEST_OREGON,
		    AZ.C, InstanceType.M1SMALL, OS.LINUX, SimProperties.SIM_START_TIME.asDate(),
		    SimProperties.SIM_END_TIME.asDate());

		System.out.println("Returned prices for: "
			+ Region.US_EAST.getAmazonName()
			+ "a"
			+ "-"
			+ InstanceType.M1SMALL
			+ "-"
			+ OS.LINUX
			+ " from "
			+ Config.formatDate(newPrices.first().getDate())
			+ " to "
			+ Config.formatDate(newPrices.last().getDate()));
		//us-west-1A.linux.m1.small.csv
		FileWriter fw = new FileWriter("C:\\Documents and Settings\\deepakc\\My Documents\\Dropbox\\Spot Market paper\\prices_csv\\us-west-2c.linux.m1.small01-042.csv");
		PrintWriter out = new PrintWriter(fw);
		
		for(SpotPriceRecord spr : newPrices){
			out.print(DateFormat.getInstance().format(spr.getDate().getTime()));
			out.print(",");
			out.println(spr.getPrice());
		}
		System.out.println("DOneee");
		
		fw.close();
		out.close();
		XStream x = new XStream();
		x.processAnnotations(SpotPriceRecord.class);

		fw = new FileWriter("C:\\Documents and Settings\\deepakc\\My Documents\\Dropbox\\Spot Market paper\\prices_csv\\string@us-west-2c.Linux%2FUNIX.m1.small01-042.xml");
		out = new PrintWriter(fw);
		String xml = x.toXML(newPrices);
		out.println(xml);
		
		fw.close();
		out.close();
	    }

}
