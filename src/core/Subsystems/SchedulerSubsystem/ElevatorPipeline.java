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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.PerformanceTimer;
import core.ConfigurationParser;
import core.Direction;
import core.Exceptions.CommunicationException;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.HostActionsException;
import core.Exceptions.SchedulerPipelineException;
import core.Exceptions.SchedulerSubsystemException;
import core.Messages.ElevatorMessage;
import core.Utils.HostActions;
import core.Utils.SubsystemConstants;

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
	private int portOffset;
	
	private SubsystemConstants objectType;
	private int pipeNumber;
	private boolean shutdown = false;
	private PerformanceTimer timer;
	private boolean transientError = false;
	private boolean hardErrorRecieved = false;


	public ElevatorPipeline(SubsystemConstants objectType, int portOffset, SchedulerSubsystem subsystem) throws SchedulerPipelineException {

		this.setName(ELEVATOR_PIPELINE + portOffset);
		this.objectType = objectType;
		this.pipeNumber = portOffset;
		this.portOffset = portOffset;
		this.schedulerSubsystem = subsystem;

		this.elevatorEvents = new LinkedList<SchedulerRequest>();
		this.elevator = new Elevator(portOffset, 1, -1, Direction.STATIONARY);
		this.timer = new PerformanceTimer();
		try {
			//need to make sure data is received the same way, matching the ports
			this.receiveSocket = new DatagramSocket();
			int elevatorTravelTime = ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_FLOOR_TRAVEL_TIME_SECONDS) * 1000;
			int elevatorDoorTime = ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_DOOR_TIME_SECONDS) * 1000;
			
			receiveSocket.setSoTimeout(elevatorTravelTime + elevatorDoorTime + 3500);
			this.receivePort = receiveSocket.getLocalPort();
			this.sendSocket = new DatagramSocket();
		}
		catch(SocketException | ConfigurationParserException e) {
			logger.info("Unable to create a DatagramSocket on Scheduler");
		}
	}

	@Override
	public void run() {

		this.sendPort = schedulerSubsystem.getElevatorPorts().get(portOffset);
		this.elevatorSubsystemAddress = schedulerSubsystem.getElevatorSubsystemAddress();
		

		while (!shutdown) {
			synchronized (elevatorEvents) {
				if(shutdown) {
					elevatorEvents.notifyAll();
				}
				if (elevatorEvents.isEmpty()) {
					try {
							elevatorEvents.wait();
					} catch (InterruptedException e) {
						logger.error(e.getLocalizedMessage());
					}
				}
			}
			if(!shutdown) {

					
				try {

					if (elevator.getRequestDirection() == Direction.UP) {
						Collections.sort(elevatorEvents, SchedulerRequest.BY_ASCENDING);
					} else {
						Collections.sort(elevatorEvents, SchedulerRequest.BY_DECENDING);
					}
					updateSubsystem(elevatorEvents.getFirst());
					
					Direction dir =null;
					if (elevator.getDestFloor() > elevator.getCurrentFloor()) {
						dir = Direction.UP;
					} else if (elevator.getDestFloor() < elevator.getCurrentFloor()) {
						dir = Direction.DOWN;
					}
					
					ElevatorMessage elevatorMessage = new ElevatorMessage(elevator.getCurrentFloor(), elevator.getDestFloor(), elevator.getElevatorId(), dir, elevatorEvents.getFirst().getErrorCode(), elevatorEvents.getFirst().getErrorFloor());	
					
					byte[] data = elevatorMessage.generatePacketData();
					DatagramPacket elevatorPacket = new DatagramPacket(data, data.length, elevatorSubsystemAddress, getSendPort());
					HostActions.send(elevatorPacket, Optional.of(sendSocket));
//					logger.debug("ELEVATOR MESSAGE SENT FOR ||"+ this.getName()+ "||:: " + elevatorMessage.toString());
					timer.start();
					ElevatorMessage elevatorRecieveMessage = recieve();
					timer.end();
					
					int elevatorTravelTime = ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_FLOOR_TRAVEL_TIME_SECONDS) * 1000;
					int elevatorDoorTime = ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_DOOR_TIME_SECONDS) * 1000;
					
					if ((!(timer.getDelta()/1000000 <= (elevatorTravelTime + elevatorDoorTime + 3500)) || hardErrorRecieved)) {
						schedulerSubsystem.removeElevator(elevator.getElevatorId());
						break;
					}
					
					if (elevatorRecieveMessage.getDoorFailureStatus()) {
						transientError = true;
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
					logger.error("Unable to send/recieve packet", e);
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
		synchronized (elevatorEvents) {
			elevatorEvents.notifyAll();
		}
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
			hardErrorRecieved = true;
		}
		ElevatorMessage recievedMessage = new ElevatorMessage(packet.getData(), packet.getLength());
//		logger.debug("ELEVATOR MESSAGE RECIEVED FOR ||"+ this.getName()+ "||:: " + recievedMessage.toString());
		return recievedMessage;
	}
	
	private void updateSubsystem(SchedulerRequest packet) throws SchedulerSubsystemException, CommunicationException, HostActionsException {
		
		elevator.setDestFloor(packet.getDestFloor());
		elevator.setRequestDirection(packet.getRequestDirection());
		elevator.setNumRequests(elevatorEvents.size());
		schedulerSubsystem.updateElevatorState(elevator);
		
		if (packet.getErrorCode() == 1) {
			schedulerSubsystem.updateFloorStates(new ElevatorMessage(elevator.getCurrentFloor(), elevator.getDestFloor(), elevator.getElevatorId(), elevator.getRequestDirection(), packet.getErrorCode(), packet.getErrorFloor()));
		}
		else {
			schedulerSubsystem.updateFloorStates(new ElevatorMessage(elevator.getCurrentFloor(), elevator.getDestFloor(), elevator.getElevatorId(), elevator.getRequestDirection()));
		}
	}
	
	private void updateStates(ElevatorMessage request) throws CommunicationException, SchedulerSubsystemException, HostActionsException {
			
		elevator.setCurrentFloor(request.getCurrentFloor());
		
		if (!shutdown) {
			List<SchedulerRequest> tempList = new ArrayList<>();
			for (SchedulerRequest event: elevatorEvents) {
				if (event.getDestFloor() == elevator.getCurrentFloor() && event.getRequestDirection().equals(elevator.getRequestDirection())) {
					tempList.add(event);
				}
			}
			if (!tempList.isEmpty()) {
//				logger.debug("\n" + "Removed events " + Arrays.toString(tempList.toArray()));
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
//				logger.debug("Elevator is stationary");
			}
			elevator.setNumRequests(elevatorEvents.size());
			if (transientError) {
				schedulerSubsystem.updateFloorStates(new ElevatorMessage(elevator.getCurrentFloor(), elevator.getDestFloor(), elevator.getElevatorId(), elevator.getRequestDirection(), 2, 0));
				transientError = false;

			} else {
				schedulerSubsystem.updateFloorStates(new ElevatorMessage(elevator.getCurrentFloor(), elevator.getDestFloor(), elevator.getElevatorId(), elevator.getRequestDirection()));
			}
		}
	}

	public void terminate() {
		this.receiveSocket.close();
		try {
			timer.printMinusTravelTime("The arrival sensor");
		} catch (ConfigurationParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
