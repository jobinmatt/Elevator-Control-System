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
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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
 * SchedulerPipeline is a receives incoming packets to the Scheduler and parses
 * the data to a SchedulerEvent
 **/
public class FloorPipeline extends Thread implements SchedulerPipeline {

	private static Logger logger = LogManager.getLogger(FloorPipeline.class);
	private static final String FLOOR_PIPELINE = "Floor pipeline ";
	private static final int DATA_SIZE = 50;

	private DatagramSocket receiveSocket;
	private DatagramSocket sendSocket;
	private int sendPort;
	private int receivePort;
	private SchedulerSubsystem schedulerSubsystem;
	private InetAddress floorSubSystemAddress;

	private SubsystemConstants objectType;
	private int pipeNumber;
	private boolean shutdown = false;

	public FloorPipeline(SubsystemConstants objectType, int portOffset, SchedulerSubsystem subsystem)
			throws SchedulerPipelineException {

		this.objectType = objectType;
		this.pipeNumber = portOffset;
		this.schedulerSubsystem = subsystem;
		String threadName = FLOOR_PIPELINE + portOffset;
		this.setName(threadName);
		this.floorSubSystemAddress = subsystem.getFloorSubsystemAddress();
		this.sendPort = schedulerSubsystem.getFloorPorts().get(portOffset);
		try {
			// need to make sure data is received the same way, matching the ports
			this.receiveSocket = new DatagramSocket();
			this.receivePort = receiveSocket.getLocalPort();
			this.sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			throw new SchedulerPipelineException("Unable to create a DatagramSocket on Scheduler", e);
		}
	}

	@Override
	public void run() {

		while (!shutdown) {
			DatagramPacket packet = new DatagramPacket(new byte[DATA_SIZE], DATA_SIZE);
			try {
				HostActions.receive(packet, receiveSocket);
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

		try {

			String str = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
			if (str.equalsIgnoreCase("End")) {
				schedulerSubsystem.end();
				return;
			}

			SubsystemMessage message = ElevatorSysMessageFactory.generateMessage(packet.getData(), packet.getLength());
			if (message instanceof FloorMessage) {
				FloorMessage floorPacket = (FloorMessage) message;
				SchedulerRequest schedulerPacket = floorPacket.toSchedulerRequest(packet.getAddress(),
						packet.getPort());
				schedulerSubsystem.scheduleEvent(schedulerPacket);
			}
		} catch (SchedulerSubsystemException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	public void sendElevatorStateToFloor(FloorMessage fMsg) throws HostActionsException, CommunicationException {

		byte[] data = fMsg.generatePacketData();
		DatagramPacket fPacket = new DatagramPacket(data, data.length, floorSubSystemAddress, getSendPort());
		HostActions.send(fPacket, Optional.of(sendSocket));
	}

	public void sendShutdownMessage() throws CommunicationException, HostActionsException {

		logger.info("Sending shutdown message...");
		shutdown = true;
		FloorMessage message = new FloorMessage();
		byte[] data = message.generateShutdownMessage();
		DatagramPacket floorPacket = new DatagramPacket(data, data.length, floorSubSystemAddress, getSendPort());
		HostActions.send(floorPacket, Optional.of(sendSocket));
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

	public void terminate() {
		this.receiveSocket.close();
	}
}
