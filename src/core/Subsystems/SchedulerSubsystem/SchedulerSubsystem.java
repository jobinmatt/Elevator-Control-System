//****************************************************************************
//
// Filename: SchedulerSubsystem.java
//
// Description: Scheduler Subsystem Class
//
//***************************************************************************

package core.Subsystems.SchedulerSubsystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.Direction;
import core.ElevatorPacket;
import core.FloorPacket;
import core.LoggingManager;
import core.Exceptions.CommunicationException;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.HostActionsException;
import core.Exceptions.SchedulerPipelineException;
import core.Exceptions.SchedulerSubsystemException;
import core.Subsystems.ElevatorSubsystem.ElevatorCarThread;
import core.Utils.HostActions;
import core.Utils.SubsystemConstants;

/**
 * This creates SchedulerThreads based on the number of elevators and floors and starts it.
 * Schedules requests
 * */
public class SchedulerSubsystem {

	private static Logger logger = LogManager.getLogger(SchedulerSubsystem.class);

	private SchedulerPipeline[] listeners;
	private static Map<Integer, SchedulerRequest> events = new ConcurrentHashMap<Integer,SchedulerRequest>();
	private Map<Elevator, TreeSet<SchedulerRequest>> elevatorEvents = new HashMap<>();
	private Map<Integer, Integer> elevatorPorts = new HashMap<>();
	private Map<Integer, Integer> floorPorts = new HashMap<>();
	private final byte SPACER = (byte) 0;
	private static final int DATA_SIZE = 1024;
	private static int numberOfElevators;
	private static int numberOfFloors;
	private InetAddress elevatorSubsystemAddress;
	private InetAddress floorSubsystemAddress;

	private DatagramSocket sendSocket;
	public SchedulerSubsystem(int numElevators, int numFloors,
			int elevatorInitPort, int floorInitPort) throws SchedulerPipelineException, SchedulerSubsystemException, HostActionsException, IOException, ConfigurationParserException {

		numberOfElevators = numElevators;
		numberOfFloors = numFloors;
		
		receiveInitPorts(elevatorInitPort, SubsystemConstants.ELEVATOR);
		receiveInitPorts(floorInitPort, SubsystemConstants.FLOOR);

		this.listeners = new SchedulerPipeline[numberOfElevators + numberOfFloors];

		for (int i = 0; i < numberOfElevators; i++) {
			this.listeners[i]= new SchedulerPipeline(SubsystemConstants.ELEVATOR, i+1, this);
		}
		for (int i = 0; i < numberOfFloors; i++) {
			this.listeners[numberOfElevators + i]= new SchedulerPipeline(SubsystemConstants.FLOOR, i+1, this);
		}
		ConfigurationParser configurationParser = ConfigurationParser.getInstance();
		int initSchedulerPort = configurationParser.getInt(ConfigurationParser.SCHEDULER_INIT_PORT);
		sendSchedulerPorts(initSchedulerPort, SubsystemConstants.ELEVATOR);
		sendSchedulerPorts(initSchedulerPort + 1, SubsystemConstants.FLOOR);
		
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
	
	private void sendSchedulerPorts(int sendPort, SubsystemConstants systemType) throws IOException, HostActionsException {
		byte[] packetData = createPortsArray(listeners, systemType);
		DatagramPacket packet = new DatagramPacket(packetData, packetData.length, InetAddress.getLocalHost(), sendPort);
	    HostActions.send(packet, Optional.empty());
	}
	
	/**
	 * Creates a data array with the port information
	 * @param map
	 * @return
	 * @throws IOException 
	 */
	private byte[] createPortsArray(SchedulerPipeline[] pipelines, SubsystemConstants systemType) throws IOException {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		for (SchedulerPipeline pipe: pipelines) {
			if(pipe.getObjectType() == systemType) {
				data.write(pipe.getPipeNumber());
		        data.write(SPACER);
		        try {
					data.write(ByteBuffer.allocate(4).putInt(pipe.getPort()).array());
				} catch (IOException e) {
					throw new IOException("" + e);
				}
		        data.write(SPACER);
		        data.write(SPACER);
			}
	    }
	    data.write(SPACER);
		return data.toByteArray();
	}	

	private void receiveInitPorts(int listenPort, SubsystemConstants systemType) throws SchedulerSubsystemException {
		try {
			DatagramPacket packet = new DatagramPacket(new byte[DATA_SIZE], DATA_SIZE);
			DatagramSocket receiveSocket = new DatagramSocket(listenPort);
			try {
				logger.info("Waiting to receive port information from " + systemType + "...");
				HostActions.receive(packet, receiveSocket);
				receiveSocket.close();
				if(systemType.equals(SubsystemConstants.ELEVATOR)) {
					this.elevatorSubsystemAddress = packet.getAddress();
				}
				else {
					this.floorSubsystemAddress = packet.getAddress();
				}
				convertPacketToMap(packet.getData(), packet.getLength(), systemType);
			} catch (HostActionsException e) {
				throw new SchedulerSubsystemException("Unable to receive elevator ports packet in SchedulerSubsystem", e);
			}
		} catch (SocketException e) {
			throw new SchedulerSubsystemException("Unable to create a DatagramSocket on in SchedulerSubsystem", e);
		}
	}
	
	private void convertPacketToMap(byte[] data, int length, SubsystemConstants systemType) throws SchedulerSubsystemException {
		if(data != null && data[0] != SPACER) {
			HashMap<Integer, Integer> tempPorts = new HashMap<>();
			for(int i = 0; i < length; i = i + 8) {
				int elevNumber = data[i];
				
				byte[] portNumInByte = {data[i+2], data[i+3], data[i+4], data[i+5]};
				int elevPort = ByteBuffer.wrap(portNumInByte).getInt();
				tempPorts.put(elevNumber, elevPort);
				if(data.length<(i+8) || data[i+8] == SPACER) {
					break;
				}
			}
			if(systemType.equals(SubsystemConstants.ELEVATOR)) {
				this.elevatorPorts = tempPorts;
			}
			else {
				this.floorPorts = tempPorts;
			}
		}
		else throw new SchedulerSubsystemException("Cannot convert null to elevator ports map or invalid data found");
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
		while (true) {
			if(!events.isEmpty()) {
				for (int key : events.keySet()) {
					SchedulerRequest r = events.get(key);
					if (r != null) {
						if (r.getType().equals(SubsystemConstants.FLOOR)) {
							Elevator lSelectedElevator = getBestElevator(r);
							if (lSelectedElevator != null) {
								r.setElevatorNumber(lSelectedElevator.getElevatorId());
								if (lSelectedElevator.getCurrentFloor() != r.getSourceFloor()) { // if not at source
									// floor of request
									// go there
									SchedulerRequest tempRequest = new SchedulerRequest(r.getReceivedAddress(),
											r.getReceivedPort(), r.getType(), lSelectedElevator.getCurrentFloor(),
											r.getRequestDirection(), r.getSourceFloor(),
											lSelectedElevator.getElevatorId(),
											r.getDestFloor());
									elevatorEvents.get(lSelectedElevator).add(tempRequest);
									lSelectedElevator.setCurrentDirection(r.getRequestDirection());
									lSelectedElevator.setDestFloor(r.getSourceFloor());
									sendRequest(tempRequest);
								}
								else {
									//									if (lSelectedElevator.getCurrentFloor() == r.getDestFloor()) {
									elevatorEvents.get(lSelectedElevator).add(r);
									lSelectedElevator.setDestFloor(r.getCarButton());
									events.remove(key);
									sendRequest(r);

								}
							}
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


	/**
	 * Creates the byte array that needs to be send via the DatagramPacket
	 * 
	 * @param SchedulerRequest
	 * @throws CommunicationException
	 */
	private synchronized byte[] createDataArray(SchedulerRequest request) throws CommunicationException {
		if(request.getType().equals(SubsystemConstants.FLOOR)) {
			Elevator elev = getElevator(elevatorEvents.keySet(), request);
			//			if (request.getRequestDirection().equals(Direction.DOWN) && elev != null
			//					&& (elevatorEvents.get(elev) != null || !elevatorEvents.get(elev).isEmpty())) {
			//
			//				elevatorEvents.get(elev).stream().forEach(x -> logger.debug("Events sorted D: ".concat(x.toString())));
			//			}
			if (elevatorEvents.get(elev).first() != null) {
				ElevatorPacket p = new ElevatorPacket(elev.getCurrentFloor(),
						elevatorEvents.get(elev).first().getDestFloor(),
						elevatorEvents.get(elev).first().getCarButton());
				return p.generatePacketData();
			}
			throw new CommunicationException(
					"Elevator or event not found: " + elev.toString() + " request: " + request.toString());
		} else {
			FloorPacket p = new FloorPacket(request.getRequestDirection(), request.getSourceFloor(),
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
				elevatorEvents.get(elev).descendingSet();
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

	public void sendUpdatePacket(ElevatorPacket sendPacket, Elevator elev)
			throws CommunicationException, HostActionsException {
		DatagramPacket sendElevPacket = new DatagramPacket(sendPacket.generatePacketData(), 0,
				sendPacket.generatePacketData().length, elevatorSubsystemAddress, elev.getElevatorId() + 50000);// TODO
		// GET
		// RID
		// OF
		// THE
		// CONSTANT
		sendElevPacket.setData(sendPacket.generatePacketData());
		HostActions.send(sendElevPacket, Optional.of(sendSocket));
	}

	public synchronized void updateStates(ElevatorPacket packet)
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
				ElevatorPacket sendPacket = new ElevatorPacket(elev.getCurrentFloor(), elev.getDestFloor(),
						packet.getRequestedFloor());
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