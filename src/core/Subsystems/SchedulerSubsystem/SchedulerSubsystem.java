//****************************************************************************
//
// Filename: SchedulerSubsystem.java
//
// Description: Scheduler Subsystem Class
//
//***************************************************************************

package core.Subsystems.SchedulerSubsystem;

import java.util.Queue;

import core.Exceptions.SchedulerPipelineException;

/**
 *
 * This creates SchedulerThreads based on the number of elevators and floors and starts it.
 * @author Jobin Mathew
 * */
public class SchedulerSubsystem {

	private Thread[] listeners;
	private static Queue<SchedulerEvent> events;
	private static int numberOfElevators;
	private static int numberOfFloors;

	public SchedulerSubsystem(int numElevators, int numFloors) throws SchedulerPipelineException {
		SchedulerSubsystem.numberOfElevators = numElevators;
		SchedulerSubsystem.numberOfFloors = numFloors;
		this.listeners = new Thread[numberOfElevators+numberOfFloors];
		for (int i = 0; i < numberOfElevators+numberOfFloors; i++) {
			this.listeners[i]= new SchedulerPipeline();
		}
	}

	public void startListeners() {
		for (int i = 0; i < numberOfElevators; i++) {
			this.listeners[i].start();
		}
	}

	public synchronized static void addEvent(SchedulerEvent e) {
		events.add(e);
	}
}