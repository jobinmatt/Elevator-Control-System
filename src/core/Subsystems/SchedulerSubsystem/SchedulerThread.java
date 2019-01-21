//****************************************************************************
//
// Filename: SchedulerThread.java
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

/**
 * 
 * SchedulerThread is a receives incoming packets to the Scheduler and parses the data to a QueuedEvent
 * @author Jobin Mathew
 * */
public class SchedulerThread extends Thread{

	private static Logger logger = LogManager.getLogger(SchedulerThread.class);

	private DatagramSocket receiveSocket;

	public SchedulerThread() {
		super("SchedulerThread");
	}

	/**
	 * Waits till a DatagramPacket is received
	 * @return DatagramPacket
	 */
	private DatagramPacket receive() {
		//need to decide the length later
		DatagramPacket receivePacket = new DatagramPacket(new byte[50], 50);
		try {
			logger.info("Waiting for data from...");
			receiveSocket.receive(receivePacket);
		}catch (IOException e) {
			logger.error("Receive Socket Timed Out on Host.\n" , e);
			e.printStackTrace();
		}
		return receivePacket;
	}

	public void run() {		
		try {			
			this.receiveSocket = new DatagramSocket();
			while(true) {
				DatagramPacket packet = receive();
				//parse packet
				SchedulerSubsystem.addEvent(parsePacket(packet));
			}
		}
		catch(SocketException e) {
			logger.error("Error creating the socket", e);
		}		
	}

	/**
	 * Creates and returns a QueuedEvent based on the DatagramPacket 
	 * @return QueuedEvent
	 */
	private QueuedEvent parsePacket(DatagramPacket packet) {

		return new QueuedEvent(packet);
	}

}
