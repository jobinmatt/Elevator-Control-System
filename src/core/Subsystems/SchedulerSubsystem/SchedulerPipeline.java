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
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.Direction;
import core.ElevatorPacket;
import core.FloorPacket;
import core.Exceptions.CommunicationException;
import core.Exceptions.HostActionsException;
import core.Exceptions.SchedulerPipelineException;
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


	public SchedulerPipeline(SubsystemConstants objectType, int portOffset, int elevatorPort, int floorPort, SchedulerSubsystem subsystem) throws SchedulerPipelineException{
		String threadName;
		this.schedulerSubsystem = subsystem;
		int portNumber = -1;
		if(objectType == SubsystemConstants.ELEVATOR) {
			threadName = ELEVATOR_PIPELINE + portOffset;
			portNumber = elevatorPort + portOffset;
		}
		else {
			threadName = FLOOR_PIPELINE + portOffset;
			portNumber = floorPort + portOffset;
		}
		this.setName(threadName);

		try {
			//need to make sure data is received the same way, matching the ports
			this.receiveSocket = new DatagramSocket(portNumber);
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
			} catch (Exception e) {
				logger.error("Failed to receive packet", e);
			}
				try {
					schedulerSubsystem.addEvent(parsePacket(packet));
				} catch (CommunicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	/**
	 * Creates and returns a SchedulerEvent based on the DatagramPacket
	 * 
	 * @return SchedulerEvent
	 * @throws CommunicationException
	 */
	private SchedulerRequest parsePacket(DatagramPacket packet) throws CommunicationException {
		SchedulerRequest $packet = null;
		if (packet.getData()[0] == (byte) 0) { // Floor
			FloorPacket lFloorPacket = new FloorPacket(packet.getData(), packet.getLength());
			$packet = new SchedulerRequest(packet.getAddress(), packet.getPort(), SubsystemConstants.FLOOR,
					lFloorPacket.getSourceFloor(), SchedulerPriorityConstants.HIGH_PRIORITY,
					lFloorPacket.getDirection(), lFloorPacket.getDestinationFloor(), lFloorPacket.getDestinationFloor());
			logger.debug("Recieved packet from floor: " + lFloorPacket.toString() + System.lineSeparator());
			logger.debug("Recieved bytes from floor: " + Arrays.toString(lFloorPacket.generatePacketData())
					+ System.lineSeparator());
		}
		else if (packet.getData()[0] == (byte) 1) {// Elev
			ElevatorPacket lElevatorPacket = new ElevatorPacket(packet.getData(), packet.getLength());
			Direction lElevDir = null;
			if (lElevatorPacket.getCurrentFloor() - lElevatorPacket.getDestinationFloor() < 0) {
				lElevDir = Direction.DOWN;
			} else if (lElevatorPacket.getCurrentFloor() - lElevatorPacket.getDestinationFloor() >= 0) {
				lElevDir = Direction.UP;
			}
			if (lElevDir != null) {
				$packet = new SchedulerRequest(packet.getAddress(), packet.getPort(), SubsystemConstants.ELEVATOR,
						lElevatorPacket.getCurrentFloor(), SchedulerPriorityConstants.HIGH_PRIORITY, lElevDir,
						lElevatorPacket.getDestinationFloor(), lElevatorPacket.getElevatorNumber(), lElevatorPacket.getRequestedFloor());
				logger.debug("Recieved packet from elevator: " + lElevatorPacket.toString());
				logger.debug("Recieved bytes from floor: " + Arrays.toString(lElevatorPacket.generatePacketData())
				+ System.lineSeparator());
			} else {
				throw new CommunicationException(
						"Error with Elevator packet. Current Floor:" + lElevatorPacket.getCurrentFloor()
						+ ", Destination Floor: " + lElevatorPacket.getDestinationFloor());
			}
			if(lElevatorPacket.getArrivalSensor() == true) {
				schedulerSubsystem.updateStates(lElevatorPacket);
			}
		}
		return $packet;
	}

	public void terminate() {
		this.receiveSocket.close();
		//cleanup goes here
	}
	

}
