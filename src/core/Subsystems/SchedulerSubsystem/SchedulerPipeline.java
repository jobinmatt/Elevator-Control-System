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

import core.Exceptions.SchedulerPipelineException;
import core.Utils.TypeConstants;

/**
 * SchedulerPipeline is a receives incoming packets to the Scheduler and parses the data to a SchedulerEvent
 * */
public class SchedulerPipeline extends Thread{

	private static Logger logger = LogManager.getLogger(SchedulerPipeline.class);
	private static final String ELEVATOR_PIPELINE = "Elevator pipeline ";
	private static final String FLOOR_PIPELINE = "Floor pipeline ";
	private static final int DEFAULT_ELEVATOR_PORT_VALUE = 5000;
	private static final int DEFAULT_FLOOR_PORT_VALUE = 4000;
	private static final int DATA_SIZE = 50;

	private DatagramSocket receiveSocket;


	public SchedulerPipeline(TypeConstants objectType, int portOffset) throws SchedulerPipelineException{
		String threadName;
		int portNumber = -1;
		if(objectType == TypeConstants.ELEVATOR) {
			threadName = ELEVATOR_PIPELINE + portOffset;
			portNumber = DEFAULT_ELEVATOR_PORT_VALUE + portOffset;
		}
		else {
			threadName = FLOOR_PIPELINE + portOffset;
			portNumber = DEFAULT_FLOOR_PORT_VALUE + portOffset;
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
			SchedulerSubsystem.addEvent(parsePacket(packet));
		}
	}

	/**
	 * Creates and returns a SchedulerEvent based on the DatagramPacket
	 * @return SchedulerEvent
	 */
	private SchedulerRequest parsePacket(DatagramPacket packet) {

		return new SchedulerRequest(packet);
	}

	public void terminate() {
		this.receiveSocket.close();
		//cleanup goes here
	}

}
