//****************************************************************************
//
// Filename: SchedulerSubsystem.java
//
// Description: Scheduler Subsystem Class
//
//***************************************************************************

package core.Subsystems.SchedulerSubsystem;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.Direction;
import core.LoggingManager;
import core.Exceptions.CommunicationException;
import core.Exceptions.HostActionsException;
import core.Exceptions.SchedulerPipelineException;
import core.Exceptions.SchedulerSubsystemException;
import core.Messages.ElevatorMessage;
import core.Messages.FloorMessage;
import core.Utils.HostActions;
import core.Utils.SubsystemConstants;

/**
 * This creates SchedulerThreads based on the number of elevators and floors and starts it.
 * Schedules requests
 * */
public class SchedulerSubsystem {

	private static Logger logger = LogManager.getLogger(SchedulerSubsystem.class);

	private ElevatorPipeline[] elevatorListeners;
	private FloorPipeline[] floorListeners;
	private static Set<SchedulerRequest> unscheduledEvents = new HashSet<SchedulerRequest>();
	private HashMap<Integer, Elevator> elevatorStatus = new HashMap<Integer, Elevator>();
	private static int numberOfElevators;
	private static int numberOfFloors;
	
	public SchedulerSubsystem(int numElevators, int numFloors,
			InetAddress elevatorSubsystemAddress, InetAddress floorSubsystemAddress,
			int elevatorInitPort, int floorInitPort) throws SchedulerPipelineException, SchedulerSubsystemException {

		numberOfElevators = numElevators;
		numberOfFloors = numFloors;

		this.elevatorListeners = new ElevatorPipeline[numberOfElevators];
		this.floorListeners = new FloorPipeline[numberOfFloors];

		for (int i = 0; i < numberOfElevators; i++) {
			this.elevatorListeners[i] = new ElevatorPipeline(SubsystemConstants.ELEVATOR, i+1, elevatorInitPort, elevatorSubsystemAddress, this);
		}
		for (int i = 0; i < numberOfFloors; i++) {
			this.floorListeners[i] = new FloorPipeline(SubsystemConstants.FLOOR, i+1, floorInitPort, floorSubsystemAddress, this);
		}

		for (int i = 0; i < numberOfElevators; i++) {
			elevatorStatus.put(i+1, new Elevator(i + 1, 1, -1, Direction.STATIONARY));
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (ElevatorPipeline listener: elevatorListeners) {
					if (listener != null) {
						listener.terminate();
					}
				}
				for (FloorPipeline listener: floorListeners) {
					if (listener != null) {
						listener.terminate();
					}
				}
				LoggingManager.terminate();
			}
		});
	}

	/**
	 * Starts the listener threads for the scheduler
	 * @throws InterruptedException
	 */
	public void startListeners() throws InterruptedException {

		logger.info("Starting listeners...");
		for (int i = 0; i < elevatorListeners.length; i++) {
			this.elevatorListeners[i].start();
			Thread.sleep(100);
		}
		
		for (int i = 0; i < floorListeners.length; i++) {
			this.floorListeners[i].start();
			Thread.sleep(100);
		}
		logger.log(LoggingManager.getSuccessLevel(), LoggingManager.SUCCESS_MESSAGE);
	}
	
	/** @formatter:off
	 * Continuous loop that keeps checking to see if there is a new event and
	 * creates and sends SchedulerRequest
	 * 
	 * @throws SchedulerSubsystemException
	 * 
	 * 1) check if elev is empty, if empty get the highest priority assign it to closest elev
	 * 2) Remove event from events queue, set elevator direction
	 * 3) If elev has events in it, check if added event is in the same direction
	 * 4) If it is and Elev going down check if floor is below current floor; if going up check if floor is above
	 * 			current floor; if so add it to the elevator list
	 * 5) If not keep in general events queue
	 * 6) Send updated dest floor message to elevator
	 * @throws CommunicationException
	 * @formatter:on
	 */
	
	public synchronized void scheduleEvent(SchedulerRequest request) throws SchedulerSubsystemException, CommunicationException {
		if(request != null) {
			Elevator selectedElevator = getBestElevator(request);
			if(selectedElevator != null) {
				request.setElevatorNumber(selectedElevator.getElevatorId());
				if(selectedElevator.getCurrentFloor() != request.getSourceFloor()) {
					SchedulerRequest tempRequest = new SchedulerRequest(request.getReceivedAddress(), request.getReceivedPort(), SubsystemConstants.FLOOR, 
							selectedElevator.getCurrentFloor(), request.getRequestDirection(), request.getSourceFloor(), selectedElevator.getElevatorId(), request.getTargetFloor());
					elevatorListeners[selectedElevator.getElevatorId()].addEvent(tempRequest);
				}
				logger.debug("Event added " + request.toString());
				elevatorListeners[selectedElevator.getElevatorId() - 1].addEvent(request);
			}
		} else {
			unscheduledEvents.add(request);
		}
	}
	
	public synchronized void reEvaluateEvents() throws SchedulerSubsystemException, CommunicationException {
		if(!unscheduledEvents.isEmpty()) {
			for(SchedulerRequest req : unscheduledEvents) {
				scheduleEvent(req);
			}
		}
	}

	private Elevator getBestElevator(SchedulerRequest request) {
		Elevator tempElevator = null;
		for (int i = 1; i <= numberOfElevators; i++) {
			Elevator elevator = elevatorStatus.get(i);
			if (elevator.getNumRequests() == 0) {
				return elevator;
			} else {
				if (elevator.getCurrentDirection().equals(request.getRequestDirection())) {
					if(tempElevator == null) {
						tempElevator = elevator;
					}
					if (elevator.getCurrentDirection().equals(Direction.DOWN)
							&& elevator.getCurrentFloor() > request.getSourceFloor()) {
						if(elevator.getNumRequests() < tempElevator.getNumRequests()) {
							tempElevator = elevator;
						}
					} else if (elevator.getCurrentDirection().equals(Direction.UP)
							&& elevator.getCurrentFloor() < request.getDestFloor()) {
						if(elevator.getNumRequests() < tempElevator.getNumRequests()) {
							tempElevator = elevator;
						}
					}
				}
			}
		}
		return tempElevator;
	}

	public synchronized void updateElevatorState(Elevator elevator) {
		logger.debug("Elevator states being updated " + elevator.toString());
		elevatorStatus.put(elevator.getElevatorId(), elevator);
		logger.debug("Elevator states updated " + elevator.toString());
	}
}