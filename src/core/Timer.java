package core;

public class Timer {
	
	private long startTime;
	private long endTime;
	
	public Timer( ) {
		
		startTime = 0;
		endTime = 0;
	}
	
	public void start() {
		
		startTime = System.nanoTime();
	}
	
	public void end() {
		
		endTime = System.nanoTime();
	}
	
	public long getDelta() {
		
		return (endTime - startTime);
	}
	
	public void print(String name) {
	
		System.out.println(name + " took " + (endTime - startTime) + " nanoseconds.");
	}

}
