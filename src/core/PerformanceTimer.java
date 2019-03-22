package core;

import java.util.ArrayList;

import core.Exceptions.ConfigurationParserException;

public class PerformanceTimer {
	
	private long startTime;
	private long endTime;
	private ArrayList<Long> times;
	
	public PerformanceTimer() {
		
		startTime = 0;
		endTime = 0;
		times = new ArrayList<Long>();
	}
	
	public void start() {
		
		startTime = System.nanoTime();
	}
	
	public void end() {
		
		endTime = System.nanoTime();
		times.add(endTime - startTime);
		System.out.println("Delta = " + (endTime - startTime)/1000000);
	}
	
	public long getDelta() {
		
		return (endTime - startTime);
	}
	
	public double getMean() {
		
		long avg =  0; 
		for (long l : times) {
			avg += l;
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
	
		System.out.println(name + " took " + getMean()/1000000 + " miliseconds on Average. The variance is: " + getVariance()/1000000);
	}
	
	public void printMinusTravelTime(String name) throws ConfigurationParserException {
		
		System.out.println(name + " took " + ((getMean()/1000000)-(ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_FLOOR_TRAVEL_TIME_SECONDS)*1000)) + " miliseconds on Average. The variance is: " + getVariance()/1000000);
	}

}
