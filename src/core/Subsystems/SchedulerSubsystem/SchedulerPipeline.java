//****************************************************************************
//
// Filename: SchedulerPipeline.java
//
// Description: Thread that waits to receive incoming packets
//
//***************************************************************************
package core.Subsystems.SchedulerSubsystem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.Direction;
import core.ElevatorPacket;
import core.FloorPacket;
import core.Exceptions.CommunicationException;
import core.Exceptions.SchedulerPipelineException;
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


	public SchedulerPipeline(SubsystemConstants objectType, int portOffset, int elevatorPort, int floorPort) throws SchedulerPipelineException{
		String threadName;
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
			receiveSocket.receive(receivePacket);
			logger.info("Data received.");
		}catch (IOException e) {
			throw new SchedulerPipelineException("Receive Socket Timed Out on Scheduler", e);
		}
		return receivePacket;
	}

	@Override
	public void run() {

		logger.info("\n" + this.getName());
		while(true) {
			DatagramPacket packet = null;
			try {
				packet = receive();
			} catch (Exception e) {
				logger.error("Failed to receive packet", e);
			}
			//parse packet
			try {
				SchedulerSubsystem.addEvent(parsePacket(packet));
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
					lFloorPacket.getCurrentElevatorFloor(), SchedulerPriorityConstants.HIGH_PRIORITY,
					lFloorPacket.getRequestDirection());
		}
		else if (packet.getData()[0] == (byte) 1) {// Elev
			ElevatorPacket lElevatorPacket = new ElevatorPacket(packet.getData(), packet.getLength());
			Direction lElevDir = null;
			if (lElevatorPacket.getCurrent_Floor() - lElevatorPacket.getDestination_Floor() < 0) {
				lElevDir = Direction.DOWN;
			} else if (lElevatorPacket.getCurrent_Floor() - lElevatorPacket.getDestination_Floor() >= 0) {
				lElevDir = Direction.UP;
			}
			if (lElevDir != null) {
				$packet = new SchedulerRequest(packet.getAddress(), packet.getPort(), SubsystemConstants.ELEVATOR,
						lElevatorPacket.getCurrent_Floor(), SchedulerPriorityConstants.HIGH_PRIORITY, lElevDir,
						lElevatorPacket.getDestination_Floor(), lElevatorPacket.getElevator_Number());
			} else {
				throw new CommunicationException(
						"Error with Elevator packet. Current Floor:" + lElevatorPacket.getCurrent_Floor()
						+ ", Destination Floor: " + lElevatorPacket.getDestination_Floor());
			}
		}

		return $packet;
	}

	public void terminate() {
		this.receiveSocket.close();
		//cleanup goes here
	}

}
