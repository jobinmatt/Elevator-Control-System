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
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.Direction;
import core.LoggingManager;
import core.Exceptions.CommunicationException;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.HostActionsException;
import core.Exceptions.SchedulerPipelineException;
import core.Exceptions.SchedulerSubsystemException;
import core.Messages.ElevatorMessage;
import core.Messages.FloorMessage;
import core.Utils.HostActions;
import core.Utils.SubsystemConstants;
import core.Utils.Utils;

/**
 * This creates SchedulerThreads based on the number of elevators and floors and starts it.
 * Schedules requests
 * */
public class SchedulerSubsystem {

	private static Logger logger = LogManager.getLogger(SchedulerSubsystem.class);

	private ElevatorPipeline[] elevatorListeners;
	private FloorPipeline[] floorListeners;
	private static Set<SchedulerRequest> unscheduledEvents = new HashSet<SchedulerRequest>();
	public HashMap<Integer, Elevator> elevatorStatus = new HashMap<Integer, Elevator>();
	private Map<Integer, Integer> elevatorPorts = new HashMap<>();
	private Map<Integer, Integer> floorPorts = new HashMap<>();
	private final byte SPACER = (byte) 0;
	private static final int DATA_SIZE = 1024;
	private static int numberOfElevators;
	private InetAddress elevatorSubsystemAddress;
	private InetAddress floorSubsystemAddress;
	private boolean end = false;

	public SchedulerSubsystem(int numElevators) throws SchedulerPipelineException, SchedulerSubsystemException, ConfigurationParserException, HostActionsException, IOException {

		numberOfElevators = numElevators;

		for (int i = 0; i < numberOfElevators; i++) {
			elevatorStatus.put(i+1, new Elevator(i + 1, 1, -1, Direction.STATIONARY));
		}
	}

	public void addListeners(ElevatorPipeline[] elev, FloorPipeline[] floor) {
		
		elevatorListeners = elev;
		floorListeners = floor;
		
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
	
	public void start(int elevatorInitPort, int floorInitPort) {
		
		try {			
			ConfigurationParser configurationParser = ConfigurationParser.getInstance();
			int initSchedulerPort = configurationParser.getInt(ConfigurationParser.SCHEDULER_INIT_PORT);
			
			receiveInitPorts(elevatorInitPort, SubsystemConstants.ELEVATOR);
			sendSchedulerPorts(initSchedulerPort, SubsystemConstants.ELEVATOR);
			receiveInitPorts(floorInitPort, SubsystemConstants.FLOOR);
			sendSchedulerPorts(initSchedulerPort + 1, SubsystemConstants.FLOOR);
		} catch (Exception e) {
			logger.info("Unable to connect to subsystems");
		}
	}
	
	public void shutDown() {

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

	private void receiveInitPorts(int listenPort, SubsystemConstants systemType) throws SchedulerSubsystemException, UnknownHostException {
		try {
			DatagramPacket packet = new DatagramPacket(new byte[DATA_SIZE], DATA_SIZE);
			DatagramSocket receiveSocket = new DatagramSocket(listenPort);
			try {
				logger.info("Waiting to receive port information from " + systemType + "...");
				HostActions.receive(packet, receiveSocket);
				receiveSocket.close();
				if(systemType.equals(SubsystemConstants.ELEVATOR)) {
					if(packet.getAddress() != null) {
						this.setElevatorSubsystemAddress(packet.getAddress());
					}else {
						this.setElevatorSubsystemAddress(InetAddress.getLocalHost());
					}
				}
				else {
					if(packet.getAddress() != null) {
						this.setFloorSubsystemAddress(packet.getAddress());
					}else {
						this.setFloorSubsystemAddress(InetAddress.getLocalHost());
					}
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
			//13 because of InitMessage
			for(int i = 13; i < length; i = i + 8) {
				int elevNumber = data[i];

				byte[] portNumInByte = {data[i+2], data[i+3], data[i+4], data[i+5]};
				int elevPort = ByteBuffer.wrap(portNumInByte).getInt();
				tempPorts.put(elevNumber, elevPort);
				if(data.length<(i+8) || data[i+8] == SPACER) {
					break;
				}
			}
			if(systemType.equals(SubsystemConstants.ELEVATOR)) {
				this.setElevatorPorts(tempPorts);
			}
			else {
				this.setFloorPorts(tempPorts);
			}
		}
		else throw new SchedulerSubsystemException("Cannot convert null to elevator ports map or invalid data found");
	}

	private void sendSchedulerPorts(int sendPort, SubsystemConstants systemType) throws IOException, HostActionsException {
		byte[] packetData;
		InetAddress sendAddress;
		if(systemType.equals(SubsystemConstants.ELEVATOR)) {
			packetData = createPortsArray(elevatorListeners, systemType);
			sendAddress = getElevatorSubsystemAddress();
		}else {
			packetData = createPortsArray(floorListeners, systemType);
			sendAddress = getFloorSubsystemAddress();
		}
		DatagramPacket packet = new DatagramPacket(packetData, packetData.length, sendAddress, sendPort);
		System.out.println("The data sent is: " + Utils.bytesToHex(packetData));
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
					data.write(ByteBuffer.allocate(4).putInt(pipe.getReceivePort()).array());
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
			if (selectedElevator != null) {
				request.setElevatorNumber(selectedElevator.getElevatorId());
				if (selectedElevator.getCurrentFloor() != request.getSourceFloor()) {
					Direction dir = null;
					if (selectedElevator.getCurrentFloor() > request.getDestFloor()) {
						dir = Direction.DOWN;
					} else {
						dir = Direction.UP;
					}
					SchedulerRequest tempRequest = new SchedulerRequest(request.getReceivedAddress(),
							request.getReceivedPort(), SubsystemConstants.FLOOR, selectedElevator.getCurrentFloor(),
							dir, request.getSourceFloor(), selectedElevator.getElevatorId(),
							request.getTargetFloor(), request.getErrorCode(), request.getErrorFloor());
					elevatorListeners[selectedElevator.getElevatorId() - 1].addEvent(tempRequest);
					logger.debug("Intermediate event added " + tempRequest.toString() + " FOR Elevator "
							+ selectedElevator.getElevatorId());
					selectedElevator.incRequests();
				}
				logger.debug("Event added " + request.toString() + " FOR Elevator "
						+ selectedElevator.getElevatorId());
				elevatorListeners[selectedElevator.getElevatorId() - 1].addEvent(request);
				selectedElevator.incRequests();
			} else {
				unscheduledEvents.add(request);
			}
		}
	}


	public void removeElevator(int id) throws SchedulerSubsystemException, CommunicationException {

		LinkedList<SchedulerRequest> elevatorEvents = elevatorListeners[id - 1].getElevatorEvents();
		Elevator selectedElevator = elevatorStatus.get(id);
		List<SchedulerRequest> tempList = new ArrayList<>();
		for (SchedulerRequest event: elevatorEvents) {
			if (event.getDestFloor() == selectedElevator.getCurrentFloor() && event.getRequestDirection().equals(selectedElevator.getRequestDirection())) {
				tempList.add(event);
			}
			if(event.getErrorFloor() == selectedElevator.getCurrentFloor()) {
				tempList.add(event);
			}
		}
		if (!tempList.isEmpty()) {
			logger.debug("\n" + "Removed events " + Arrays.toString(tempList.toArray()));
		}
		elevatorEvents.removeAll(tempList);
		unscheduledEvents.addAll(elevatorEvents);
		elevatorStatus.remove(id);
		reEvaluateEvents();
	}

	public void reEvaluateEvents() throws SchedulerSubsystemException, CommunicationException {
		List<SchedulerRequest> tempList = new ArrayList<>();
		if(!unscheduledEvents.isEmpty()) {
			for(SchedulerRequest request : unscheduledEvents) {
				if(request != null) {
					Elevator selectedElevator = getBestElevator(request);
					if(selectedElevator != null) {
						request.setElevatorNumber(selectedElevator.getElevatorId());
						if(selectedElevator.getCurrentFloor() != request.getSourceFloor()) {
							Direction dir = null;
							if(selectedElevator.getCurrentFloor() > request.getDestFloor()) {
								dir = Direction.DOWN;
							} else {
								dir = Direction.UP;
							}
							SchedulerRequest tempRequest = new SchedulerRequest(request.getReceivedAddress(), request.getReceivedPort(), SubsystemConstants.FLOOR,
									selectedElevator.getCurrentFloor(), dir, request.getSourceFloor(), selectedElevator.getElevatorId(), request.getTargetFloor(), 0, 0);
							elevatorListeners[selectedElevator.getElevatorId() - 1].addEvent(tempRequest);
							logger.debug("\n" +"Intermediate event added " + tempRequest.toString() + " FOR Elevator " + selectedElevator.getElevatorId());
							selectedElevator.incRequests();
						}
						logger.debug("\n" +"Event added " + request.toString() + " FOR Elevator " + selectedElevator.getElevatorId());
						elevatorListeners[selectedElevator.getElevatorId() - 1].addEvent(request);
						tempList.add(request);
						selectedElevator.incRequests();
					}else {
						unscheduledEvents.add(request);
					}
				}
			}
			unscheduledEvents.removeAll(tempList);
		}
	}

	private Elevator getBestElevator(SchedulerRequest request) {
		Elevator tempElevator = null;
		for (int i = 1; i <= numberOfElevators; i++) {
			Elevator elevator = elevatorStatus.get(i);
			if (elevator != null) {
				if(elevator.getNumRequests() == 0) {
					return elevator;
				}
				if (elevator.getRequestDirection().equals(request.getRequestDirection())) {
					if (tempElevator == null) {
						tempElevator = elevator;
					}
					if (elevator.getRequestDirection().equals(Direction.DOWN)
							&& elevator.getCurrentFloor() > request.getSourceFloor()) {
						if (elevator.getNumRequests() < tempElevator.getNumRequests()) {
							tempElevator = elevator;
						}
					} else if (elevator.getRequestDirection().equals(Direction.UP)
							&& elevator.getCurrentFloor() < request.getDestFloor()) {
						if (elevator.getNumRequests() < tempElevator.getNumRequests()) {
							tempElevator = elevator;
						}
					}
				}
			}
		}
		return tempElevator;
	}

	public void updateElevatorState(Elevator elevator) throws SchedulerSubsystemException, CommunicationException, HostActionsException {
		synchronized (elevatorStatus) {
			elevatorStatus.put(elevator.getElevatorId(), elevator);
			this.reEvaluateEvents();
		}
	}

	public void updateFloorStates (ElevatorMessage elevator) throws HostActionsException, CommunicationException {

		FloorMessage floorState = new FloorMessage(elevator.getDirection(), elevator.getCurrentFloor(), elevator.getDestinationFloor(), 0, 0); // source floor will be current floor, and dont need dest becuase this goes to floors to update buttons

		floorState.setElevatorNum(elevator.getElevatorNumber());
		for (FloorPipeline listeners : this.floorListeners) {
			listeners.sendElevatorStateToFloor(floorState);
		}
	}

	public Map<Integer, Integer> getElevatorPorts() {
		return elevatorPorts;
	}

	public void setElevatorPorts(Map<Integer, Integer> elevatorPorts) {
		this.elevatorPorts = elevatorPorts;
	}

	public Map<Integer, Integer> getFloorPorts() {
		return floorPorts;
	}

	public void setFloorPorts(Map<Integer, Integer> floorPorts) {
		this.floorPorts = floorPorts;
	}

	public InetAddress getElevatorSubsystemAddress() {
		return elevatorSubsystemAddress;
	}

	public void end() {
		this.end = true;
	}

	public boolean getEnd() {
		return end;
	}
	public void setElevatorSubsystemAddress(InetAddress elevatorSubsystemAddress) {
		this.elevatorSubsystemAddress = elevatorSubsystemAddress;
	}

	public InetAddress getFloorSubsystemAddress() {
		return floorSubsystemAddress;
	}

	public void setFloorSubsystemAddress(InetAddress floorSubsystemAddress) {
		this.floorSubsystemAddress = floorSubsystemAddress;
	}

	public Set<SchedulerRequest> getUnscheduledEventsSet() {
		return SchedulerSubsystem.unscheduledEvents;
	}

	public HashMap<Integer, Elevator> getElevatorStatusMap() {
		return this.elevatorStatus;
	}
}