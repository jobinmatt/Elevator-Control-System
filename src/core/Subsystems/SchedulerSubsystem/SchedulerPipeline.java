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

import core.LoggingManager;
import core.Exceptions.SchedulerPipelineException;

/**
 *
 * SchedulerPipeline is a receives incoming packets to the Scheduler and parses the data to a SchedulerEvent
 * @author Jobin Mathew
 * */
public class SchedulerPipeline extends Thread{

	private static Logger logger = LogManager.getLogger(SchedulerPipeline.class);

	private DatagramSocket receiveSocket;
	private static final int DATA_SIZE = 50;

	public SchedulerPipeline() throws SchedulerPipelineException{
		super("SchedulerThread");

		logger.info(LoggingManager.BANNER + "Scheduler Pipeline Thread\n");

		try {
			this.receiveSocket = new DatagramSocket();
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
		}catch (IOException e) {
			throw new SchedulerPipelineException("Receive Socket Timed Out on Scheduler", e);
		}
		return receivePacket;
	}

	@Override
	public void run() {
		while(true) {
			DatagramPacket packet = null;
			try {
				packet = receive();
			} catch (Exception e) {
				//need to fix this
				//throw new SchedulerPipelineException("Failed to receive packet", e);
				logger.error("Failed to receive packet", e);
			}
			//parse packet
			SchedulerSubsystem.addEvent(parsePacket(packet));
		}
	}

	/**
	 * Creates and returns a SchedulerEvent based on the DatagramPacket
	 * @return SchedulerEvent
	 */
	private SchedulerEvent parsePacket(DatagramPacket packet) {

		return new SchedulerEvent(packet);
	}

}
