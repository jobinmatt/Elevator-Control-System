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

import core.Direction;
import core.Exceptions.CommunicationException;
import core.Exceptions.HostActionsException;
import core.Exceptions.SchedulerPipelineException;
import core.Exceptions.SchedulerSubsystemException;
import core.Messages.ElevatorMessage;
import core.Messages.FloorMessage;
import core.Utils.HostActions;
import core.Utils.SubsystemConstants;

/**
 * SchedulerPipeline is a receives incoming packets to the Scheduler and parses the data to a SchedulerEvent
 * */
public class SchedulerPipeline extends Thread{

	private static Logger logger = LogManager.getLogger(SchedulerPipeline.class);
	private static final String ELEVATOR_PIPELINE = "Elevator pipeline ";
	private static final String FLOOR_PIPELINE = "Floor pipeline ";
	private static final int DATA_SIZE = 50;

	private DatagramSocket receiveSocket;
	private SchedulerSubsystem schedulerSubsystem;
	private SubsystemConstants objectType;
	private int pipeNumber;
	private int port;


	public SchedulerPipeline(SubsystemConstants objectType, int portOffset,
			SchedulerSubsystem subsystem) throws SchedulerPipelineException {
		String threadName;
		this.schedulerSubsystem = subsystem;
		this.objectType = objectType;
		this.pipeNumber = portOffset;
		if(objectType == SubsystemConstants.ELEVATOR) {
			threadName = ELEVATOR_PIPELINE + portOffset;
		}
		else {
			threadName = FLOOR_PIPELINE + portOffset;
		}
		this.setName(threadName);

		try {
			//need to make sure data is received the same way, matching the ports
			this.receiveSocket = new DatagramSocket();
			this.port = receiveSocket.getLocalPort();
		}
		catch(SocketException e) {
			throw new SchedulerPipelineException("Unable to create a DatagramSocket on Scheduler", e);
		}
	}

	/**
	 * Waits till a DatagramPacket is received
	 * @return DatagramPacket
	 * @throws Exception
	 */
	private DatagramPacket receive() throws SchedulerPipelineException {

		//need to decide the length later
		DatagramPacket receivePacket = new DatagramPacket(new byte[DATA_SIZE], DATA_SIZE);
		try {
			logger.info("Waiting for data...");
			HostActions.receive(receivePacket, receiveSocket);
			logger.info("Data received..");
		} catch (HostActionsException e) {
			throw new SchedulerPipelineException("Receive Socket Timed Out on Scheduler", e);
		}
		return receivePacket;
	}


	@Override
	public void run() {

		logger.info("\n" + this.getName());
		while(true) {
			DatagramPacket packet = new DatagramPacket(new byte[DATA_SIZE], DATA_SIZE);
			try {
				packet = receive();
				parsePacket(packet);
			} catch (SchedulerPipelineException | CommunicationException | SchedulerSubsystemException
					| HostActionsException e) {
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
	private void parsePacket(DatagramPacket packet)
			throws CommunicationException, SchedulerSubsystemException, HostActionsException {
		SchedulerRequest schedulerPacket = null;
		if (packet.getData()[0] == (byte) 0) { // Floor
			FloorMessage lFloorPacket = new FloorMessage(packet.getData(), packet.getLength());
			schedulerPacket = new SchedulerRequest(packet.getAddress(), packet.getPort(), SubsystemConstants.FLOOR,
					lFloorPacket.getSourceFloor(),
					lFloorPacket.getDirection(), lFloorPacket.getTargetFloor(), lFloorPacket.getTargetFloor());
			schedulerSubsystem.addEvent(schedulerPacket);
		}
		else if (packet.getData()[0] == (byte) 1) {
			ElevatorMessage lElevatorPacket = new ElevatorMessage(packet.getData(), packet.getLength());
			if (lElevatorPacket.getArrivalSensor()) {
				logger.debug("\n Got arrival sensor: " + lElevatorPacket.toString());
				this.updateStates(lElevatorPacket);
			} else {
				Direction lElevDir = null;
				if (lElevatorPacket.getCurrentFloor() - lElevatorPacket.getDestinationFloor() < 0) {
					lElevDir = Direction.DOWN;
				} else if (lElevatorPacket.getCurrentFloor() - lElevatorPacket.getDestinationFloor() >= 0) {
					lElevDir = Direction.UP;
				}
				schedulerPacket = new SchedulerRequest(packet.getAddress(), packet.getPort(),
						SubsystemConstants.ELEVATOR,
						lElevatorPacket.getCurrentFloor(), lElevDir,
						lElevatorPacket.getDestinationFloor(), lElevatorPacket.getElevatorNumber(),
						lElevatorPacket.getTargetFloor());
				logger.debug("Recieved packet from elevator: " + lElevatorPacket.toString());
				schedulerSubsystem.addEvent(schedulerPacket);
			}
		}
	}

	private void updateStates(ElevatorMessage packet) throws CommunicationException, HostActionsException {
		synchronized (schedulerSubsystem) {
			Elevator elev = schedulerSubsystem.getElevator(packet.getElevatorNumber());
			if (elev != null) {
				schedulerSubsystem.updateFloors(elev);
				schedulerSubsystem.removeServicedEvents(elev);
				ElevatorMessage sendPacket = new ElevatorMessage(elev.getCurrentFloor(), elev.getDestFloor(),
						packet.getTargetFloor());
				schedulerSubsystem.sendUpdatePacket(sendPacket, elev);
				logger.debug("Elevator update packet created: " + sendPacket.toString());
			}
		}
	}

	public void terminate() {
		this.receiveSocket.close();
		//cleanup goes here
	}

	public SubsystemConstants getObjectType() {
		return objectType;
	}

	public int getPipeNumber() {
		return pipeNumber;
	}

	public int getPort() {
		return port;
	}

}
