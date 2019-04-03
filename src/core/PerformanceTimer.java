package core;

import java.util.ArrayList;

import core.Exceptions.ConfigurationParserException;

public class PerformanceTimer {
	
	private long startTime;
	private long endTime;
	private ArrayList<Long> times;
	private int floorTravelTime = 0;
	
	public PerformanceTimer() {
		
		startTime = 0;
		endTime = 0;
		times = new ArrayList<Long>();
		try {
			floorTravelTime = ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_FLOOR_TRAVEL_TIME_SECONDS);
			floorTravelTime = floorTravelTime*1000;
		} catch (ConfigurationParserException e) {

		}
	}
	
	public void start() {
		
		startTime = System.nanoTime();
	}
	
	public void end() {
		
		endTime = System.nanoTime();
		times.add(((endTime - startTime)/1000000));
		System.out.println("Delta = " + (endTime - startTime));
	}
	
	public long getDelta() {
		
		return (endTime - startTime);
	}
	
	public double getMean() {
		
		long avg =  0; 
		for (long l : times) {
			avg += l;
		}
		
		if (times.size() == 0) {
			return 0;
		}
		avg = avg/times.size();
		return avg;
	}
	
	public double getVariance() {
		
		double mean = getMean();
		long variance  = 0; 
		
		for (long l: times) {
			variance += (l - mean) *  (l - mean);
		}
		
		if (times.size() == 1) {
			return  variance;
		}
		
		variance = variance/(times.size() -1);
		
		return variance;
	}
	
	public void print(String name) {
	
		System.out.println(name + " took " + getMean() + " milliseconds on Average. The variance is: " + getVariance());
	}
	
	public void printMinusTravelTime(String name) throws ConfigurationParserException {
		
		System.out.println(name + " took " + (getMean()-floorTravelTime) + " milliseconds on Average. The variance is: " + getVariance());
	}

}
