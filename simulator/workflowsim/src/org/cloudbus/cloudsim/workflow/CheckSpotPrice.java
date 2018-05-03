package org.cloudbus.cloudsim.workflow;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TimeZone;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.spotsim.enums.AZ;
import org.cloudbus.spotsim.enums.InstanceType;
import org.cloudbus.spotsim.enums.OS;
import org.cloudbus.spotsim.enums.Region;
import org.cloudbus.spotsim.main.config.Config;
import org.cloudbus.spotsim.main.config.SimProperties;
import org.cloudbus.spotsim.pricing.PriceRecord;
import org.cloudbus.spotsim.pricing.PriceTraceGen;
import org.cloudbus.spotsim.pricing.SpotPriceHistory;
import org.cloudbus.spotsim.pricing.db.PriceDB;
import org.cloudbus.spotsim.pricing.db.TheGrabber;
import org.cloudbus.spotsim.spothistory.SpotPriceRecord;



public class CheckSpotPrice {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		try {
			Log.init(null);
			Config.load();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		SimProperties.PRICING_TRACE_GEN.set(PriceTraceGen.HISTORY);

		PriceDB.flushPriceDB();
		FileWriter fw=null;
	 	PrintWriter out = null;
		
		 try {
				fw = new FileWriter("C:\\Documents and Settings\\deepakc\\My Documents\\Dropbox\\Spot Market paper\\results\\expr_29oct\\CheckSpotPricesM!SMALL.csv");
				out = new PrintWriter(fw);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		SpotPriceHistory spotHist = PriceDB.getPriceTrace(Region.DEEPAK_TEST, AZ.ANY);
		if (spotHist.areTherePricesForType(InstanceType.M1SMALL, OS.LINUX)) {
			final NavigableSet<PriceRecord> list = spotHist.getPricesForType(InstanceType.M1SMALL, OS.LINUX);
			for(PriceRecord pr : list){
				long time = (pr.getTime()*1000 + Config.getSimPeriodStart().getTimeInMillis());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS",Locale.US);

		        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Central"));
		        calendar.setTimeInMillis(time);
				out.print(calendar.getTime());
				out.print(",");
				out.println(pr.getPrice());
			}
		}
		try {
			fw.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*int totalGaps = 0;

		SimProperties.PRICING_TRACE_GEN.set(PriceTraceGen.HISTORY);

			final SpotPriceHistory db = PriceDB.getPriceTrace(Region.US_EAST, AZ.C);
				final SortedSet<PriceRecord> list = db.getPricesForType(InstanceType.M1SMALL, OS.LINUX);
				if (list != null && !list.isEmpty()) {
				    long previousDate = list.first().getDate();
				    for (final PriceRecord spotPriceRecord : list) {
					final long currentDate = spotPriceRecord.getDate();
					if (currentDate - previousDate > 3 * 24 * 3600) {
					    final SortedSet<SpotPriceRecord> newPriceHist = TheGrabber
						.fetchSpotHistory(Region.US_EAST, AZ.C,  InstanceType.M1SMALL, OS.LINUX, previousDate,
						    currentDate);
					    PriceDB.addToDb(Region.US_EAST, AZ.C,  InstanceType.M1SMALL, OS.LINUX, newPriceHist);
					    System.err.println("Gap greater than 3 days from "
						    + Config.formatDate(previousDate)
						    + " to "
						    + Config.formatDate(currentDate)
						    + ". records returned: "
						    + newPriceHist.size());
					    totalGaps++;
					}
					previousDate = currentDate;
				    }
				}
		System.out.println("TOTAL GAPS " + totalGaps);
		*/
		
		
	}

}
