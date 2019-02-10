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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.Exceptions.CommunicationException;
import core.Exceptions.HostActionsException;
import core.Exceptions.SchedulerPipelineException;
import core.Exceptions.SchedulerSubsystemException;
import core.Messages.ElevatorMessage;
import core.Messages.ElevatorSysMessageFactory;
import core.Messages.SubsystemMessage;
import core.Utils.HostActions;
import core.Utils.SubsystemConstants;

/**
 * SchedulerPipeline is a receives incoming packets to the Scheduler and parses the data to a SchedulerEvent
 * */
public class ElevatorPipeline extends Thread {

	private static Logger logger = LogManager.getLogger(ElevatorPipeline.class);
	private static final String ELEVATOR_PIPELINE = "Elevator pipeline ";
	private static final int DATA_SIZE = 50;
	
	private DatagramSocket receiveSocket;
	private SchedulerSubsystem schedulerSubsystem;


	public ElevatorPipeline(SubsystemConstants objectType, int portOffset, int elevatorPort, int floorPort, SchedulerSubsystem subsystem) throws SchedulerPipelineException {

		this.schedulerSubsystem = subsystem;
		String threadName = ELEVATOR_PIPELINE + portOffset;
		int portNumber = elevatorPort + portOffset;
		this.setName(threadName);

		try {
			//need to make sure data is received the same way, matching the ports
			this.receiveSocket = new DatagramSocket(portNumber);
		}
		catch(SocketException e) {
			throw new SchedulerPipelineException("Unable to create a DatagramSocket on Scheduler", e);
		}
	}

	@Override
	public void run() {

		logger.info("\n" + this.getName());
		while(true) {
			DatagramPacket packet = new DatagramPacket(new byte[DATA_SIZE], DATA_SIZE);
			try {
				logger.info("Waiting for data...");
				HostActions.receive(packet, receiveSocket);
				logger.info("Data received..");
				parsePacket(packet);
			} catch (HostActionsException | CommunicationException | SchedulerSubsystemException e) {
				logger.error("Failed to receive packet", e);
			}
		}
	}

	/**
	 * Creates and returns a SchedulerEvent based on the DatagramPacket
	 * 
	 * @return SchedulerEvent
	 * @throws CommunicationException
	 * @throws SchedulerSubsystemException
	 * @throws HostActionsException
	 */
	private void parsePacket(DatagramPacket packet) throws CommunicationException, SchedulerSubsystemException, HostActionsException {
		
		SchedulerRequest schedulerPacket = null;
		
		SubsystemMessage message = ElevatorSysMessageFactory.generateMessage(packet.getData(), packet.getLength());
		if (message instanceof ElevatorMessage) {
			ElevatorMessage elevatorPacket = (ElevatorMessage) message;
			
			if (elevatorPacket.getArrivalSensor()) {
				logger.debug("\nGot arrival sensor: " + elevatorPacket.toString());
				this.updateStates(elevatorPacket);
			} else {
				schedulerPacket = elevatorPacket.toSchedulerRequest(packet.getAddress(), packet.getPort());
				logger.debug("Recieved packet from elevator: " + elevatorPacket.toString());
				schedulerSubsystem.addEvent(schedulerPacket);
			}
		}
	}

	private void updateStates(ElevatorMessage packet) throws CommunicationException, HostActionsException {
		
		synchronized (schedulerSubsystem) {
			Elevator elevator = schedulerSubsystem.getElevator(packet.getElevatorNumber());
			if (elevator != null) {
				schedulerSubsystem.updateFloors(elevator);
				schedulerSubsystem.removeServicedEvents(elevator);
				ElevatorMessage sendPacket = new ElevatorMessage(elevator.getCurrentFloor(), elevator.getDestFloor(), packet.getTargetFloor());
				schedulerSubsystem.sendUpdatePacket(sendPacket, elevator);
				logger.debug("Elevator update packet created: " + sendPacket.toString());
			}
		}
	}

	public void terminate() {
		this.receiveSocket.close();
		//cleanup goes here
	}


}