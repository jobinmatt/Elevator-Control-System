package core;

import java.util.ArrayList;

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
	
		System.out.println(name + " took " + getMean() + " nanoseconds on Average. The variance is: " + getVariance());
	}

}
