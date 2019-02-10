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

	private ElevatorPipeline[] listeners;
	private static Map<Integer,SchedulerRequest> events = new ConcurrentHashMap<Integer,SchedulerRequest>();
	private Map<Elevator, LinkedList<SchedulerRequest>> elevatorEvents = new HashMap<>();
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

		this.listeners = new ElevatorPipeline[numberOfElevators + numberOfFloors];

		for (int i = 0; i < numberOfElevators; i++) {
			this.listeners[i]= new ElevatorPipeline(SubsystemConstants.ELEVATOR, i+1, elevatorInitPort, floorInitPort, this);
		}
		for (int i = 0; i < numberOfFloors; i++) {
			this.listeners[numberOfElevators + i]= new ElevatorPipeline(SubsystemConstants.FLOOR, i+1, elevatorInitPort, floorInitPort, this);
		}

		for (int i = 0; i < numberOfElevators; i++) {
			elevatorEvents.put(new Elevator(i + 1, 1, -1, Direction.STATIONARY),
					new LinkedList<>());
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (ElevatorPipeline listener: listeners) {
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
	public synchronized void scheduleEvent(SchedulerRequest request) throws SchedulerSubsystemException, CommunicationException {
		if(request != null) {
			Elevator selectedElevator = getBestElevator(request);
			if(selectedElevator != null) {
				if(selectedElevator.getCurrentFloor() != request.getSourceFloor()) {
					
				}
			}
		} else {
			throw new SchedulerSubsystemException("Request recieved was null or invalid");
		}
	}

	private Elevator getBestElevator(SchedulerRequest request) {
		Elevator tempElevator = null;
		for (Elevator e : elevatorEvents.keySet()) {
			if (elevatorEvents.get(e).isEmpty()) {
				return e;
			} else {
				if (e.getCurrentDirection().equals(request.getRequestDirection())) {
					if(tempElevator == null) {
						tempElevator = e;
					}
					if (e.getCurrentDirection().equals(Direction.DOWN)
							&& e.getCurrentFloor() > request.getSourceFloor()) {
						if(e.getNumRequests() < tempElevator.getNumRequests()) {
							tempElevator = e;
						}
					} else if (e.getCurrentDirection().equals(Direction.UP)
							&& e.getCurrentFloor() < request.getDestFloor()) {
						if(e.getNumRequests() < tempElevator.getNumRequests()) {
							tempElevator = e;
						}
					}
				}
			}
		}
		return tempElevator;
	}


	/**
	 * Creates the byte array that needs to be send via the DatagramPacket
	 * 
	 * @param SchedulerRequest
	 * @throws CommunicationException
	 */
	private synchronized byte[] createDataArray(SchedulerRequest request) throws CommunicationException {
		if(request.getType().equals(SubsystemConstants.FLOOR)) {
			Elevator elev = getElevator(elevatorEvents.keySet(), request);
			if (elevatorEvents.get(elev).getFirst() != null) {
				ElevatorMessage p = new ElevatorMessage(elev.getCurrentFloor(),
						elevatorEvents.get(elev).getFirst().getDestFloor(),
						elevatorEvents.get(elev).getFirst().getCarButton());
				return p.generatePacketData();
			}
			throw new CommunicationException(
					"Elevator or event not found: " + elev.toString() + " request: " + request.toString());
		} else {
			FloorMessage p = new FloorMessage(request.getRequestDirection(), request.getSourceFloor(),
					request.getDestFloor());
			return p.generatePacketData();
		}
	}

	private Elevator getElevator(Set<Elevator> e, SchedulerRequest req) {
		for(Elevator elev : e) {
			if(elev.getElevatorId() == req.getElevatorNumber()) {
				return elev;
			}
		}
		return null;
	}

	private SchedulerRequest forwardToRequest(SchedulerRequest request) {
		if (request.getType().equals(SubsystemConstants.ELEVATOR)) {
			request.setType(SubsystemConstants.FLOOR);
			request.setReceivedAddress(floorSubsystemAddress);
			request.setReceivedPort(request.getSourceFloor() + 40000);
		} else {
			request.setType(SubsystemConstants.ELEVATOR);
			request.setReceivedAddress(elevatorSubsystemAddress);
			request.setReceivedPort(request.getElevatorNumber() + 50000);
		}

		logger.debug("Forwarding request: " + request.toString());
		return request;
	}


	/**
	 * Send the created DatagramPacket to the appropriate address and port through
	 * the DatagramSocket
	 * 
	 * @param SchedulerRequest
	 * @throws SchedulerSubsystemException
	 * @throws CommunicationException
	 */
	private SchedulerRequest sendRequest(SchedulerRequest request)
			throws SchedulerSubsystemException, CommunicationException {

		byte sendingData[] = createDataArray(request);
		request = forwardToRequest(request);
		InetAddress address;
		if(request.getType() == SubsystemConstants.ELEVATOR) {
			address = this.elevatorSubsystemAddress;
		}
		else {
			address = this.floorSubsystemAddress;
		}
		DatagramPacket packet = new DatagramPacket(sendingData, sendingData.length, address, request.getReceivedPort());
		packet.setData(sendingData);
		try {
			HostActions.send(packet, Optional.of(sendSocket));
		} catch (HostActionsException e) {
			throw new SchedulerSubsystemException("Unable to send a DatagramPacket from SchedulerSubsystem", e);
		}
		logger.debug("Request sent to " + request.getType() + ": " + request.toString());
		return request;
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

	public void addEvent(SchedulerRequest e) {
		events.put(events.size() + 1, e);
	}

	public Elevator getElevator(int elevatorNumber) {
		for (Elevator e : elevatorEvents.keySet()) {
			if (e.getElevatorId() == elevatorNumber) {
				return e;
			}
		}
		return null;
	}

	public void updateFloors(Elevator elev) {
		if (elev.getCurrentFloor() > elev.getDestFloor()) {
			if (elev.getCurrentFloor() - 1 >= 0) {
				elev.setCurrentFloor(elev.getCurrentFloor() - 1);
			}
			elev.setCurrentDirection(Direction.DOWN);
		} else if (elev.getCurrentFloor() < elev.getDestFloor()) {
			elev.setCurrentDirection(Direction.UP);
			if (elev.getCurrentFloor() + 1 <= numberOfFloors) {
				elev.setCurrentFloor(elev.getCurrentFloor() + 1);
			}
		} else if (elev.getCurrentFloor() == elev.getDestFloor() || elev.getCurrentFloor() == 0) {
			elev.setCurrentDirection(Direction.STATIONARY);
		} // TODO fill the list if elevator has serviced all requests in the direction its
		// currently going in
		if (!elevatorEvents.get(elev).isEmpty()) {
			boolean isSameDirection = false;
			for (SchedulerRequest e : elevatorEvents.get(elev)) {
				if (e.getRequestDirection().equals(elev.getCurrentDirection())) {
					isSameDirection = true;
					break;
				}
			}
			if (isSameDirection == false) {
				Collections.sort(elevatorEvents.get(elev), SchedulerRequest.BY_DECENDING);
			}
		}
		logger.debug("Elevator Packet generated: " + elev.toString());
	}

	public void removeServicedEvents(Elevator elev) {
		List<SchedulerRequest> tempList = new ArrayList<SchedulerRequest>();
		for (SchedulerRequest e : elevatorEvents.get(elev)) {
			if (e.getDestFloor() == elev.getCurrentFloor() && e.getCarButton() == elev.getCurrentFloor()) {
				tempList.add(e);
			}
		}
		elevatorEvents.get(elev).removeAll(tempList);
	}

	public void sendUpdatePacket(ElevatorMessage sendPacket, Elevator elev)
			throws CommunicationException, HostActionsException {
		DatagramPacket sendElevPacket = new DatagramPacket(sendPacket.generatePacketData(), 0,
				sendPacket.generatePacketData().length, elevatorSubsystemAddress, elev.getElevatorId() + 50000);// TODO (50000)change me plssssss
		// GET
		// RID
		// OF
		// THE
		// CONSTANT
		sendElevPacket.setData(sendPacket.generatePacketData());
		HostActions.send(sendElevPacket, Optional.of(sendSocket));
	}

	public synchronized void updateStates(ElevatorMessage packet)
			throws SchedulerSubsystemException, CommunicationException, HostActionsException {
		Elevator elev = null;
		if (!elevatorEvents.isEmpty()) {
			for(Elevator e : elevatorEvents.keySet()) {
				if(e.getElevatorId() == packet.getElevatorNumber()) {
					elev = e;
				}
			}
			if(elev != null) {
				if (elev.getCurrentFloor() > elev.getDestFloor()) {
					if (elev.getCurrentFloor() - 1 >= 0) {
						elev.setCurrentFloor(elev.getCurrentFloor() - 1);
					}
					elev.setCurrentDirection(Direction.DOWN);
				} else if (elev.getCurrentFloor() < elev.getDestFloor()) {
					elev.setCurrentDirection(Direction.UP);
					if (elev.getCurrentFloor() + 1 <= numberOfFloors) {
						elev.setCurrentFloor(elev.getCurrentFloor() + 1);
					}
				}
				else if (elev.getCurrentFloor() == elev.getDestFloor() || elev.getCurrentFloor() == 0) {
					elev.setCurrentDirection(Direction.STATIONARY);
				}
				List<SchedulerRequest> tempList = new ArrayList<SchedulerRequest>();
				for (SchedulerRequest e : elevatorEvents.get(elev)) {
					if (e.getDestFloor() == elev.getCurrentFloor()) {
						tempList.add(e);
					}
				}
				elevatorEvents.get(elev).removeAll(tempList);
				ElevatorMessage sendPacket = new ElevatorMessage(elev.getCurrentFloor(), elev.getDestFloor(),
						packet.getTargetFloor());
				logger.debug("Elevator update packet created: " + sendPacket.toString());
				DatagramPacket sendElevPacket = new DatagramPacket(sendPacket.generatePacketData(), 0,sendPacket.generatePacketData().length, 
						elevatorSubsystemAddress, elev.getElevatorId() + 50000);//TODO GET RID OF THE CONSTANT
				sendElevPacket.setData(sendPacket.generatePacketData());
				HostActions.send(sendElevPacket, Optional.of(sendSocket));

			} else {
				throw new SchedulerSubsystemException("Elevator not found: " + packet.toString());
			}
		}
	}
}