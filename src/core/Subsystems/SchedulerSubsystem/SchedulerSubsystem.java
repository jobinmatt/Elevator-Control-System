//****************************************************************************
//
// Filename: SchedulerSubsystem.java
//
// Description: Scheduler Subsystem Class
//
// @author Jobin Mathew
//***************************************************************************

package core.Subsystems.SchedulerSubsystem;

import java.util.Queue;

import core.LoggingManager;
import core.Exceptions.SchedulerPipelineException;

/**
 * This creates SchedulerThreads based on the number of elevators and floors and starts it.
 * */
public class SchedulerSubsystem {

	private SchedulerPipeline[] listeners;
	private static Queue<SchedulerRequest> events;
	private static int numberOfElevators;
	private static int numberOfFloors;

	public SchedulerSubsystem(int numElevators, int numFloors) throws SchedulerPipelineException {
		
		numberOfElevators = numElevators;
		numberOfFloors = numFloors;	
		this.listeners = new SchedulerPipeline[numberOfElevators];
	
		for (int i = 0; i < numberOfElevators; i++) {
			this.listeners[i]= new SchedulerPipeline();
		}
		
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	for (SchedulerPipeline listener: listeners) {
                    if (listener != null) {
                    	listener.terminate();
                    }
            	}
                LoggingManager.terminate();
            }
        });
	}

	public void startListeners() {
		
		for (int i = 0; i < numberOfElevators; i++) {
			this.listeners[i].start();
		}
	}

	public synchronized static void addEvent(SchedulerRequest e) {

		events.add(e);
	}
}