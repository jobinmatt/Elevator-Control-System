//****************************************************************************
//
// Filename: SchedulerSubsystem.java
//
// Description: Scheduler Subsystem Class
//
// @author Jobin Mathew
//***************************************************************************

package core.Subsystems.SchedulerSubsystem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.LoggingManager;
import core.Exceptions.SchedulerPipelineException;
import core.Exceptions.SchedulerSubsystemException;
import core.Utils.SubsystemConstants;

/**
 * This creates SchedulerThreads based on the number of elevators and floors and starts it.
 * Schedules requests
 * */
public class SchedulerSubsystem {

	private static Logger logger = LogManager.getLogger(SchedulerSubsystem.class);
	private static final int DATA_SIZE = 50;

	private SchedulerPipeline[] listeners;
	private static Queue<SchedulerRequest> events = new PriorityQueue<SchedulerRequest>();
	private static int numberOfElevators;
	private static int numberOfFloors;
	private InetAddress elevatorSubsystemAddress;
	private InetAddress floorSubsystemAddress;

	private DatagramSocket sendSocket;

	public SchedulerSubsystem(int numElevators, int numFloors,
			InetAddress elevatorSubsystemAddress, InetAddress floorSubsystemAddress,
			int elevatorInitPort, int floorInitPort) throws SchedulerPipelineException, SchedulerSubsystemException {

		numberOfElevators = numElevators;
		numberOfFloors = numFloors;

		this.elevatorSubsystemAddress = elevatorSubsystemAddress;
		this.floorSubsystemAddress = floorSubsystemAddress;

		this.listeners = new SchedulerPipeline[numberOfElevators + numberOfFloors];

		for (int i = 0; i < numberOfElevators; i++) {
			this.listeners[i]= new SchedulerPipeline(SubsystemConstants.ELEVATOR, i+1, elevatorInitPort, floorInitPort);
		}
		for (int i = 0; i < numberOfFloors; i++) {
			this.listeners[numberOfElevators + i]= new SchedulerPipeline(SubsystemConstants.FLOOR, i+1, elevatorInitPort, floorInitPort);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (SchedulerPipeline listener: listeners) {
					if (listener != null) {
						listener.terminate();
					}
				}
				LoggingManager.terminate();
			}
		});

		try {
			this.sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			throw new SchedulerSubsystemException("Unable to create a DatagramSocket on in SchedulerSubsystem", e);
		}
	}

	/**
	 * Continuous loop that keeps checking to see if there is a new event and creates and sends SchedulerRequest
	 * @throws SchedulerSubsystemException
	 */
	public void startScheduling() throws SchedulerSubsystemException {

		while(true) {
			if(!events.isEmpty()) {
				//perform scheduling magic ***

				//create a SchedulerRequest based on magic ***
				sendRequest(new SchedulerRequest());
			}
		}
	}


	/**
	 * Creates the byte array that needs to be send via the DatagramPacket
	 * @param SchedulerRequest
	 */
	private byte[] createDataArray(SchedulerRequest request) {

		//create a byte array based on the data structure
		return new byte[DATA_SIZE];
	}


	/**
	 * Send the created DatagramPacket to the appropriate address and port through the DatagramSocket
	 * @param SchedulerRequest
	 * @throws SchedulerSubsystemException
	 */
	private void sendRequest(SchedulerRequest request) throws SchedulerSubsystemException {

		byte sendingData[] = createDataArray(request);

		InetAddress address;
		if(request.getType() == SubsystemConstants.ELEVATOR) {
			address = this.elevatorSubsystemAddress;
		}
		else {
			address = this.floorSubsystemAddress;
		}

		//modify port based on the request
		DatagramPacket packet = new DatagramPacket(sendingData, sendingData.length, address, request.getReceivedPort());
		try {
			this.sendSocket.send(packet);
		} catch (IOException e) {
			throw new SchedulerSubsystemException("Unable to send a DatagramPacket from SchedulerSubsystem", e);
		}
	}


	/**
	 * Starts the listener threads for the scheduler
	 * @throws InterruptedException
	 */
	public void startListeners() throws InterruptedException {

		logger.info("Starting listeners...");
		for (int i = 0; i < listeners.length; i++) {
			this.listeners[i].start();
			Thread.sleep(100);
		}
		logger.log(LoggingManager.getSuccessLevel(), LoggingManager.SUCCESS_MESSAGE);
	}

	public synchronized static void addEvent(SchedulerRequest e) {

		events.add(e);
	}
}