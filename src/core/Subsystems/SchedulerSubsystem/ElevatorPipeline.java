//****************************************************************************
//
// Filename: SchedulerPipeline.java
//
// Description: Thread that waits to receive incoming packets
//
//***************************************************************************
package core.Subsystems.SchedulerSubsystem;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.PerformanceTimer;
import core.ConfigurationParser;
import core.Direction;
import core.Exceptions.CommunicationException;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.GeneralException;
import core.Exceptions.HostActionsException;
import core.Exceptions.SchedulerPipelineException;
import core.Exceptions.SchedulerSubsystemException;
import core.Messages.ElevatorMessage;
import core.Subsystems.ElevatorSubsystem.ElevatorComponentStates;
import core.Utils.HostActions;
import core.Utils.SubsystemConstants;
import core.Utils.Utils;

/**
 * SchedulerPipeline is a receives incoming packets to the Scheduler and parses the data to a SchedulerEvent
 * */
public class ElevatorPipeline extends Thread implements SchedulerPipeline{

	private static Logger logger = LogManager.getLogger(ElevatorPipeline.class);
	private static final String ELEVATOR_PIPELINE = "Elevator pipeline ";
	private static final int DATA_SIZE = 50;
	
	private DatagramSocket receiveSocket;
	private DatagramSocket sendSocket; 
	private int sendPort;
	private int receivePort;
	private SchedulerSubsystem schedulerSubsystem;
	private Elevator elevator;
	private LinkedList<SchedulerRequest> elevatorEvents;
	private InetAddress elevatorSubsystemAddress;
	
	private SubsystemConstants objectType;
	private int pipeNumber;
	private boolean shutdown = false;
	private PerformanceTimer timer;


	public ElevatorPipeline(SubsystemConstants objectType, int portOffset, SchedulerSubsystem subsystem) throws SchedulerPipelineException {

		this.objectType = objectType;
		this.pipeNumber = portOffset;
		this.setName(ELEVATOR_PIPELINE + portOffset);
		this.schedulerSubsystem = subsystem;
		this.elevatorEvents = new LinkedList<SchedulerRequest>();
		this.elevator = new Elevator(portOffset, 1, -1, Direction.STATIONARY);
		this.elevatorSubsystemAddress = subsystem.getElevatorSubsystemAddress();
		this.sendPort = schedulerSubsystem.getElevatorPorts().get(portOffset);
		this.timer = new PerformanceTimer();
		
		try {
			//need to make sure data is received the same way, matching the ports
			this.receiveSocket = new DatagramSocket();
			this.receivePort = receiveSocket.getLocalPort();
			this.sendSocket = new DatagramSocket();
		}
		catch(SocketException e) {
			throw new SchedulerPipelineException("Unable to create a DatagramSocket on Scheduler", e);
		}
	}

	@Override
	public void run() {

		synchronized (elevatorEvents) {
			if(elevatorEvents.isEmpty()) {
				try {
					elevatorEvents.wait();
				} catch (InterruptedException e) {
					logger.error(e.getLocalizedMessage());
				}
			}
		}

		while (!shutdown) {
			if (!elevatorEvents.isEmpty()) {								
				try {

					if (elevator.getRequestDirection() == Direction.UP) {
						Collections.sort(elevatorEvents, SchedulerRequest.BY_ASCENDING);
					} else {
						Collections.sort(elevatorEvents, SchedulerRequest.BY_DECENDING);
					}
					updateSubsystem(elevatorEvents.getFirst());
					
					ElevatorMessage elevatorMessage = new ElevatorMessage(elevator.getCurrentFloor(), elevator.getDestFloor(), elevator.getElevatorId(), elevatorEvents.getFirst().getErrorCode(), elevatorEvents.getFirst().getErrorFloor());	
					
					byte[] data = elevatorMessage.generatePacketData();
					DatagramPacket elevatorPacket = new DatagramPacket(data, data.length, elevatorSubsystemAddress, getSendPort());
					HostActions.send(elevatorPacket, Optional.of(sendSocket));
					
					timer.start();
					ElevatorMessage elevatorRecieveMessage = recieve();
					timer.end();
					
					int elevatorTravelTime = ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_FLOOR_TRAVEL_TIME_SECONDS) * 1000;
					int elevatorDoorTime = ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_DOOR_TIME_SECONDS) * 1000;
					
					if (!(timer.getDelta()/1000000 <= (elevatorTravelTime + elevatorDoorTime + 3500))) {
						schedulerSubsystem.removeElevator(elevator.getElevatorId());
						break;
					}
					
					if (elevatorRecieveMessage.getDoorFailureStatus()) {
						ElevatorMessage msg = new ElevatorMessage();	
						data = msg.generateForceCloseMessage();
						DatagramPacket packet = new DatagramPacket(data, data.length, elevatorSubsystemAddress, getSendPort());
						HostActions.send(packet, Optional.of(sendSocket));
						elevatorRecieveMessage = recieve();
					}
					
					if (elevatorRecieveMessage.getArrivalSensor()) {
						updateStates(elevatorRecieveMessage);
					}
								
				} catch (HostActionsException | CommunicationException | SchedulerSubsystemException | ConfigurationParserException e) {
					logger.error("Unab`le to send/recieve packet", e);
				}
			}
		}		
	}

	
	public void sendShutdownMessage() throws CommunicationException, HostActionsException {
		
		ElevatorMessage okayMessage = new ElevatorMessage();
		byte[] data = okayMessage.generateShutdownMessage();
		DatagramPacket elevatorPacket = new DatagramPacket(data, data.length, elevatorSubsystemAddress, getSendPort());
		HostActions.send(elevatorPacket, Optional.of(sendSocket));
		shutdown = true;
	}
	
	public void addEvent(SchedulerRequest request) {
		synchronized (elevatorEvents) {
			elevatorEvents.add(request);
			elevatorEvents.notifyAll();
		}
	}
	
	public ElevatorMessage recieve() throws CommunicationException {
		
		DatagramPacket packet = new DatagramPacket(new byte[DATA_SIZE], DATA_SIZE);
		try {
			HostActions.receive(packet, receiveSocket);
		} catch (HostActionsException e) {
			logger.error("Failed to receive packet", e);
			throw new CommunicationException(e);
		}
		return new ElevatorMessage(packet.getData(), packet.getLength());
	}
	
	private void updateSubsystem(SchedulerRequest packet) throws SchedulerSubsystemException, CommunicationException, HostActionsException {
		
		elevator.setDestFloor(packet.getDestFloor());
		elevator.setRequestDirection(packet.getRequestDirection());
		elevator.setNumRequests(elevatorEvents.size());
		schedulerSubsystem.updateElevatorState(elevator);
		schedulerSubsystem.updateFloorStates(new ElevatorMessage(elevator.getCurrentFloor(), elevator.getDestFloor(), elevator.getElevatorId()));
	}
	
	private void updateStates(ElevatorMessage request) throws CommunicationException, SchedulerSubsystemException, HostActionsException {
			
		elevator.setCurrentFloor(request.getCurrentFloor());
		
		List<SchedulerRequest> tempList = new ArrayList<>();
		for (SchedulerRequest event: elevatorEvents) {
			if (event.getDestFloor() == elevator.getCurrentFloor() && event.getRequestDirection().equals(elevator.getRequestDirection())) {
				tempList.add(event);
			}
		}
		if (!tempList.isEmpty()) {
			logger.debug("\n" + "Removed events " + Arrays.toString(tempList.toArray()));
		}
		elevatorEvents.removeAll(tempList);
		
		if (elevator.getRequestDirection() == Direction.UP) {
			Collections.sort(elevatorEvents, SchedulerRequest.BY_ASCENDING);
		} else {
			Collections.sort(elevatorEvents, SchedulerRequest.BY_DECENDING);
		}
		
		if (!elevatorEvents.isEmpty()) {
			elevator.setDestFloor(elevatorEvents.getFirst().getDestFloor());
			elevator.setRequestDirection(elevatorEvents.getFirst().getRequestDirection());
		} else {
			elevator.setRequestDirection(Direction.STATIONARY);
		}
		elevator.setNumRequests(elevatorEvents.size());
		schedulerSubsystem.updateElevatorState(elevator);
		schedulerSubsystem.updateFloorStates(new ElevatorMessage(elevator.getCurrentFloor(), elevator.getDestFloor(), elevator.getElevatorId()));
		logger.debug("Elevator status updated: " + elevator.toString() + "\n ");
	}

	public void terminate() {
		this.receiveSocket.close();
		timer.print("The arrival sensor");
	}

	public SubsystemConstants getObjectType() {
		return this.objectType;
	}

	public DatagramSocket getSendSocket() {
		return this.sendSocket;
	}
	
	public DatagramSocket getReceiveSocket() {
		return this.receiveSocket;
	}

	public int getSendPort() {
		return this.sendPort;
	}

	public int getReceivePort() {
		return this.receivePort;
	}

	public int getPipeNumber() {
		return this.pipeNumber;
	}

	public LinkedList<SchedulerRequest> getElevatorEvents() {
		return elevatorEvents;
	}

}
