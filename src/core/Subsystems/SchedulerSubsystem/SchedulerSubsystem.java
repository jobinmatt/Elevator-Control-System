//****************************************************************************
//
// Filename: SchedulerSubsystem.java
//
// Description: Scheduler Subsystem Class
//
// @author Jobin Mathew
//***************************************************************************

package core.Subsystems.SchedulerSubsystem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.Direction;
import core.ElevatorPacket;
import core.FloorPacket;
import core.LoggingManager;
import core.Exceptions.CommunicationException;
import core.Exceptions.SchedulerPipelineException;
import core.Exceptions.SchedulerSubsystemException;
import core.Utils.SubsystemConstants;

/**
 * This creates SchedulerThreads based on the number of elevators and floors and starts it.
 * Schedules requests
 * */
public class SchedulerSubsystem {

	private static Logger logger = LogManager.getLogger(SchedulerSubsystem.class);

	private SchedulerPipeline[] listeners;
	private static Queue<SchedulerRequest> events = new PriorityQueue<SchedulerRequest>();
	private Map<Elevator, Set<SchedulerRequest>> elevatorEvents = new HashMap<>();
	private static int numberOfElevators;
	private static int numberOfFloors;
	private InetAddress elevatorSubsystemAddress;
	private InetAddress floorSubsystemAddress;

	private DatagramSocket sendSocket;

	public SchedulerSubsystem(int numElevators, int numFloors,
			InetAddress elevatorSubsystemAddress, InetAddress floorSubsystemAddress,
			int elevatorInitPort, int floorInitPort) throws SchedulerPipelineException, SchedulerSubsystemException {

		numberOfElevators = numElevators;
		numberOfFloors = numFloors;

		this.elevatorSubsystemAddress = elevatorSubsystemAddress;
		this.floorSubsystemAddress = floorSubsystemAddress;

		this.listeners = new SchedulerPipeline[numberOfElevators + numberOfFloors];

		for (int i = 0; i < numberOfElevators; i++) {
			this.listeners[i]= new SchedulerPipeline(SubsystemConstants.ELEVATOR, i+1, elevatorInitPort, floorInitPort);
		}
		for (int i = 0; i < numberOfFloors; i++) {
			this.listeners[numberOfElevators + i]= new SchedulerPipeline(SubsystemConstants.FLOOR, i+1, elevatorInitPort, floorInitPort);
		}

		for (int i = 0; i < numberOfElevators; i++) {
			elevatorEvents.put(new Elevator(i, -1, Direction.STATIONARY),
					new LinkedHashSet<>());
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (SchedulerPipeline listener: listeners) {
					if (listener != null) {
						listener.terminate();
					}
				}
				LoggingManager.terminate();
			}
		});

		try {
			this.sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			throw new SchedulerSubsystemException("Unable to create a DatagramSocket on in SchedulerSubsystem", e);
		}
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
	public void startScheduling() throws SchedulerSubsystemException, CommunicationException {
		while(true) {
			if(!events.isEmpty()) {
				for (SchedulerRequest r : events) {
					if (r.getType().equals(SubsystemConstants.FLOOR)) {
						Elevator lSelectedElevator = getBestElevator(r);
						if (lSelectedElevator != null) {
							elevatorEvents.get(lSelectedElevator).add(r);
							events.remove(r);
							sendRequest(r);
						}
					}
					else if (r.getType().equals(SubsystemConstants.ELEVATOR)) {
						Elevator lSelectedElevator = getElevator(r);
						if (lSelectedElevator != null) {
							elevatorEvents.get(lSelectedElevator).add(r);
							events.remove(r);
							sendRequest(r);
						} else {
							throw new CommunicationException("Elevator not found: " + r.getElevatorNumber());
						}
					}
				}
			}
		}
	}

	private Elevator getBestElevator(SchedulerRequest request) {
		for (Elevator e : elevatorEvents.keySet()) {
			if (elevatorEvents.get(e).isEmpty()) {
				return e;
			} else {
				if (e.getCurrentDirection().equals(request.getRequestDirection())) {
					if (e.getCurrentDirection().equals(Direction.DOWN)
							&& e.getCurrentFloor() > request.getDestFloor()) {
						return e;
					} else if (e.getCurrentDirection().equals(Direction.UP)
							&& e.getCurrentFloor() < request.getDestFloor()) {
						return e;
					}
				}
			}
		}
		return null;
	}

	private Elevator getElevator(SchedulerRequest request) {
		for (Elevator e : elevatorEvents.keySet()) {
			if (e.getElevatorId() == request.getElevatorNumber()) {
				return e;
			}
		}
		return null;
	}


	/**
	 * Creates the byte array that needs to be send via the DatagramPacket
	 * 
	 * @param SchedulerRequest
	 * @throws CommunicationException
	 */
	private byte[] createDataArray(SchedulerRequest request) throws CommunicationException {
		if(request.getType().equals(SubsystemConstants.ELEVATOR)) {
			ElevatorPacket p = new ElevatorPacket(request.getCurrentFloor(), request.getDestFloor(), -1,
					request.getElevatorNumber());
			return p.generatePacketData();
		} else {
			FloorPacket p = new FloorPacket(request.getRequestDirection(), request.getCurrentFloor(),
					request.getEventTime(), request.getDestFloor());
			return p.generatePacketData();
		}

	}


	/**
	 * Send the created DatagramPacket to the appropriate address and port through
	 * the DatagramSocket
	 * 
	 * @param SchedulerRequest
	 * @throws SchedulerSubsystemException
	 * @throws CommunicationException
	 */
	private void sendRequest(SchedulerRequest request) throws SchedulerSubsystemException, CommunicationException {

		byte sendingData[] = createDataArray(request);

		InetAddress address;
		if(request.getType() == SubsystemConstants.ELEVATOR) {
			address = this.elevatorSubsystemAddress;
		}
		else {
			address = this.floorSubsystemAddress;
		}
		DatagramPacket packet = new DatagramPacket(sendingData, sendingData.length, address, request.getReceivedPort());
		try {
			this.sendSocket.send(packet);
		} catch (IOException e) {
			throw new SchedulerSubsystemException("Unable to send a DatagramPacket from SchedulerSubsystem", e);
		}
	}


	/**
	 * Starts the listener threads for the scheduler
	 * @throws InterruptedException
	 */
	public void startListeners() throws InterruptedException {

		logger.info("Starting listeners...");
		for (int i = 0; i < listeners.length; i++) {
			this.listeners[i].start();
			Thread.sleep(100);
		}
		logger.log(LoggingManager.getSuccessLevel(), LoggingManager.SUCCESS_MESSAGE);
	}

	public synchronized static void addEvent(SchedulerRequest e) {

		events.add(e);
	}
}