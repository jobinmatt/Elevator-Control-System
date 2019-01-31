//****************************************************************************
//
// Filename: SchedulerSubsystem.java
//
// Description: Scheduler Subsystem Class
//
// @author Jobin Mathew
//***************************************************************************

package core.Subsystems.SchedulerSubsystem;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.Direction;
import core.ElevatorPacket;
import core.FloorPacket;
import core.LoggingManager;
import core.Exceptions.CommunicationException;
import core.Exceptions.HostActionsException;
import core.Exceptions.SchedulerPipelineException;
import core.Exceptions.SchedulerSubsystemException;
import core.Utils.HostActions;
import core.Utils.SubsystemConstants;

/**
 * This creates SchedulerThreads based on the number of elevators and floors and starts it.
 * Schedules requests
 * */
public class SchedulerSubsystem {

	private static Logger logger = LogManager.getLogger(SchedulerSubsystem.class);

	private SchedulerPipeline[] listeners;
	private static Map<Integer,SchedulerRequest> events = new ConcurrentHashMap<Integer,SchedulerRequest>();
	public Map<Elevator, TreeSet<SchedulerRequest>> elevatorEvents = new HashMap<>();
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
			this.listeners[i]= new SchedulerPipeline(SubsystemConstants.ELEVATOR, i+1, elevatorInitPort, floorInitPort, this);
		}
		for (int i = 0; i < numberOfFloors; i++) {
			this.listeners[numberOfElevators + i]= new SchedulerPipeline(SubsystemConstants.FLOOR, i+1, elevatorInitPort, floorInitPort, this);
		}

		for (int i = 0; i < numberOfElevators; i++) {
			elevatorEvents.put(new Elevator(i + 1, 1, -1, Direction.STATIONARY),
					new TreeSet<>(SchedulerRequest.BY_ASCENDING));
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
	public synchronized void startScheduling() throws SchedulerSubsystemException, CommunicationException {
		while(true) {
			if(!events.isEmpty()) {
				for(int i = 1; i <= events.size(); i++) {
					SchedulerRequest r = events.get(i);
					if (r.getType().equals(SubsystemConstants.FLOOR)) {
						Elevator lSelectedElevator = getBestElevator(r);
						if (lSelectedElevator != null) {
							r.setElevatorNumber(lSelectedElevator.getElevatorId());
							elevatorEvents.get(lSelectedElevator).add(r);
							lSelectedElevator
							.setDestFloor(elevatorEvents.get(lSelectedElevator).first().getDestFloor());
							events.remove(i);
							elevatorEvents.get(lSelectedElevator).stream()
							.forEach(x -> logger.debug("Added Events Floor: ".concat(x.toString())));
							sendRequest(r);
						}
					}
					else if (r.getType().equals(SubsystemConstants.ELEVATOR)) {
						Elevator lSelectedElevator = getElevator(r);
						if (lSelectedElevator != null) {
							elevatorEvents.get(lSelectedElevator).add(r);
							events.remove(i);
							elevatorEvents.get(lSelectedElevator).stream().forEach(x -> "Added Events Elevator: ".concat(x.toString()));
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
		if(request.getType().equals(SubsystemConstants.FLOOR)) {
			Elevator elev = getElevator(elevatorEvents.keySet(), request);
			if(request.getRequestDirection().equals(Direction.DOWN)) {
				elevatorEvents.get(elev).descendingSet();
				elevatorEvents.get(elev).stream().forEach(x -> "Events sorted D: ".concat(x.toString()));
			}
			ElevatorPacket p = new ElevatorPacket(elev.getCurrentFloor(), elevatorEvents.get(elev).first().getDestFloor(), elevatorEvents.get(elev).first().getCarButton());
			logger.debug("Elevator Packet generated: " + request.getType() + ": " + Arrays.toString(p.generatePacketData()));
			return p.generatePacketData();
		} else {
			FloorPacket p = new FloorPacket(request.getRequestDirection(), request.getCurrentFloor(), request.getDestFloor());
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
			request.setReceivedPort(request.getCurrentFloor() + 40000);
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
	private SchedulerRequest sendRequest(SchedulerRequest request) throws SchedulerSubsystemException, CommunicationException {

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
		logger.debug("Data sending to " + request.getType() + ": " + Arrays.toString(packet.getData()));
		logger.debug("Request sent to " + request.getType() + ": " + request.toString());
		try {
			HostActions.send(packet, Optional.of(sendSocket));
		} catch (HostActionsException e) {
			throw new SchedulerSubsystemException("Unable to send a DatagramPacket from SchedulerSubsystem", e);
		}
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

	public void updateStates(ElevatorPacket packet)
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
				int elevCurrentFloor = elev.getCurrentFloor();
				Predicate<SchedulerRequest> requestPredicate = r -> r.getCurrentFloor() == elevCurrentFloor;
				elevatorEvents.get(elev).removeIf(requestPredicate);
				ElevatorPacket sendPacket = new ElevatorPacket(elev.getCurrentFloor(), elev.getDestFloor(),
						packet.getRequestedFloor());
				logger.debug("Elevator update packet created: " + sendPacket.toString());
				logger.debug("Elev send packet data: " + Arrays.toString(sendPacket.generatePacketData()));
				DatagramPacket sendElevPacket = new DatagramPacket(sendPacket.generatePacketData(), 0,sendPacket.generatePacketData().length, 
						elevatorSubsystemAddress, elev.getElevatorId() + 50000);//TODO GET RID OF THE CONSTANT
				sendElevPacket.setData(sendPacket.generatePacketData());
				HostActions.send(sendElevPacket, Optional.of(sendSocket));
				logger.debug("Elevator state updated: " + elev.toString());
			} else {
				throw new SchedulerSubsystemException("Elevator not found: " + packet.toString());
			}
		}
	}
}