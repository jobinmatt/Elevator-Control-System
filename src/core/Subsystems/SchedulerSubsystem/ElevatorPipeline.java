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
import java.net.SocketException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.Direction;
import core.Exceptions.CommunicationException;
import core.Exceptions.HostActionsException;
import core.Exceptions.SchedulerPipelineException;
import core.Messages.ElevatorMessage;
import core.Utils.HostActions;
import core.Utils.SubsystemConstants;

/**
 * SchedulerPipeline is a receives incoming packets to the Scheduler and parses the data to a SchedulerEvent
 * */
public class ElevatorPipeline extends Thread implements Pipeline{

	private static Logger logger = LogManager.getLogger(ElevatorPipeline.class);
	private static final String ELEVATOR_PIPELINE = "Elevator pipeline ";
	private static final int DATA_SIZE = 50;
	
	private DatagramSocket receiveSocket;
	private DatagramSocket sendSocket; 
	private SchedulerSubsystem schedulerSubsystem;
	private Elevator elevator;
	private LinkedList<SchedulerRequest> elevatorEvents;


	public ElevatorPipeline(SubsystemConstants objectType, int portOffset, int elevatorPort,
			int floorPort, SchedulerSubsystem subsystem) throws SchedulerPipelineException {

		this.setName(ELEVATOR_PIPELINE + portOffset);
		this.schedulerSubsystem = subsystem;
		int portNumber = elevatorPort + portOffset;
		elevatorEvents = new LinkedList<SchedulerRequest>();
		elevator = new Elevator(portOffset, 1, -1, Direction.STATIONARY);
		

		try {
			//need to make sure data is received the same way, matching the ports
			this.receiveSocket = new DatagramSocket(portNumber);
			this.sendSocket = new DatagramSocket();
		}
		catch(SocketException e) {
			throw new SchedulerPipelineException("Unable to create a DatagramSocket on Scheduler", e);
		}
	}

	@Override
	public void run() {

		logger.info("\n" + this.getName());
		while (true) {		
			if (!elevatorEvents.isEmpty()) {
				break;
			}		
		}

		SchedulerRequest event = elevatorEvents.getFirst();
		updateSubsystem(event);
		
		while (true) {
			if (!elevatorEvents.isEmpty()) {								
				try {
					//updateStates(request);
					ElevatorMessage elevatorMessage = new ElevatorMessage(elevator.getCurrentFloor(), elevator.getDestFloor(), elevator.getElevatorId());	
					logger.debug("Sending Packet to Elevator " + elevator.getElevatorId() + ": " + elevatorMessage.toString());
					
					byte[] data = elevatorMessage.generatePacketData();
					DatagramPacket elevatorPacket = new DatagramPacket(data, data.length);
					HostActions.send(elevatorPacket, Optional.of(sendSocket));
					
					ElevatorMessage elevatorRecieveMessage = recieve();
					logger.debug("Recieved packet from elevator: " + elevatorRecieveMessage.toString());
					
					if (elevatorRecieveMessage.getArrivalSensor()) {
						updateStates(elevatorRecieveMessage);
					}
				} catch (HostActionsException | CommunicationException e) {
					logger.error("Unable to send/recieve packet", e);
				}
			}
		}		
	}

	public void addEvent(SchedulerRequest request) {
		
		elevatorEvents.add(request);
	}
	
	public ElevatorMessage recieve() throws CommunicationException {
		
		DatagramPacket packet = new DatagramPacket(new byte[DATA_SIZE], DATA_SIZE);
		try {
			logger.info("Waiting for data...");
			HostActions.receive(packet, receiveSocket);
			logger.info("Data received..");
		} catch (HostActionsException e) {
			logger.error("Failed to receive packet", e);
			throw new CommunicationException(e);
		}
		return new ElevatorMessage(packet.getData(), packet.getLength());
	}
	
	private void updateSubsystem(SchedulerRequest packet) {
		
		elevator.setDestFloor(packet.getDestFloor());
		elevator.setCurrentDirection(packet.getRequestDirection());
		elevator.setNumRequests(elevatorEvents.size());
		schedulerSubsystem.updateElevatorState(elevator);
	}
	
	private void updateStates(ElevatorMessage request) throws CommunicationException {
			
		elevator.setCurrentFloor(request.getCurrentFloor());
		
		for (SchedulerRequest event: elevatorEvents) {
			if (event.getDestFloor() == elevator.getCurrentFloor()) {
				elevatorEvents.removeFirstOccurrence(event);
			}
		}
		
		if (elevator.getCurrentDirection() == Direction.UP) {
			Collections.sort(elevatorEvents, SchedulerRequest.BY_ASCENDING);
		} else {
			Collections.sort(elevatorEvents, SchedulerRequest.BY_DECENDING);
		}
		
		if (!elevatorEvents.isEmpty()) {
			elevator.setDestFloor(elevatorEvents.getFirst().getDestFloor());
			elevator.setCurrentDirection(elevatorEvents.getFirst().getRequestDirection());
		} else {
			elevator.setDestFloor(-1);
			elevator.setCurrentDirection(Direction.STATIONARY);
		}
		elevator.setNumRequests(elevatorEvents.size());
		schedulerSubsystem.updateElevatorState(elevator);
	}

	public void terminate() {
		this.receiveSocket.close();
		//cleanup goes here
	}


}
