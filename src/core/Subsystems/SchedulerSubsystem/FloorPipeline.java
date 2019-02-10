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
import core.Messages.ElevatorSysMessageFactory;
import core.Messages.FloorMessage;
import core.Messages.SubsystemMessage;
import core.Utils.HostActions;
import core.Utils.SubsystemConstants;

/**
 * SchedulerPipeline is a receives incoming packets to the Scheduler and parses the data to a SchedulerEvent 
 **/
public class FloorPipeline extends Thread implements Pipeline {

	private static Logger logger = LogManager.getLogger(FloorPipeline.class);
	private static final String FLOOR_PIPELINE = "Floor pipeline ";
	private static final int DATA_SIZE = 50;

	private DatagramSocket receiveSocket;
	private SchedulerSubsystem schedulerSubsystem;


	public FloorPipeline(SubsystemConstants objectType, int portOffset, int elevatorPort, int floorPort, SchedulerSubsystem subsystem) throws SchedulerPipelineException {

		this.schedulerSubsystem = subsystem;
		String threadName = FLOOR_PIPELINE + portOffset;
		int portNumber = floorPort + portOffset;
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
		while (true) {
			DatagramPacket packet = new DatagramPacket(new byte[DATA_SIZE], DATA_SIZE);
			try {
				logger.info("Waiting for data...");
				HostActions.receive(packet, receiveSocket);
				logger.info("Data received..");
				parsePacket(packet);
			} catch (CommunicationException | HostActionsException e) {
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
	public void parsePacket(DatagramPacket packet) throws CommunicationException {
		
		SubsystemMessage message = ElevatorSysMessageFactory.generateMessage(packet.getData(), packet.getLength());
		if (message instanceof FloorMessage) {
			FloorMessage floorPacket = (FloorMessage) message;
			SchedulerRequest schedulerPacket = floorPacket.toSchedulerRequest(packet.getAddress(), packet.getPort());
			schedulerSubsystem.addEvent(schedulerPacket);
		}
	}

	public void terminate() {
		
		this.receiveSocket.close();
	}


}
