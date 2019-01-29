//****************************************************************************
//
// Filename: FloorThread.java
//
// Description: Floor thread Class
//
// @author Dharina H.
//***************************************************************************

package core.Subsystems.FloorSubsystem;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.Direction;
import core.FloorPacket;
import core.Exceptions.GeneralException;
import core.Exceptions.HostActionsException;
import core.Utils.SimulationRequest;
import core.Utils.Utils;

/**
 * The FloorThread represents a floor on which a person can request an elevator. Maintains a queue of events.
 */
public class FloorThread extends Thread {

	private static Logger logger = LogManager.getLogger(FloorThread.class);
	private int port; //port to communicate with the scheduler
	private Queue<SimulationRequest> events;
	private int floorNumber;

	DatagramSocket receiveSocket;
	private InetAddress floorSubsystemAddress;



	/**
	 * Creates a floor thread
	 */
	public FloorThread(int floorNumber, InetAddress floorSubsystemAddress, int port) throws GeneralException {

		super("FloorThread " + Integer.toString(floorNumber));
		events = new LinkedList<>();
		this.floorNumber = floorNumber;
		this.floorSubsystemAddress = floorSubsystemAddress;
		this.port = port;

		try {
			receiveSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			throw new GeneralException("Socket could not be created", e);
		}
	}

	/**
	 * Add a SimulationEvent to the queue
	 * @param e
	 */
	public void addEvent(SimulationRequest e) {

		events.add(e);
	}

	/**
	 *Services each floor request
	 */
	@Override
	public void run() {

		while(!events.isEmpty()) {
			SimulationRequest event = events.peek(); //first event in the queue
			logger.info("Event request: " + event.toString());

			try {
				serviceRequest(event);
			} catch (HostActionsException e) {
				logger.error("", e);
			} catch (GeneralException e) {
				logger.error(e);
			}
			events.remove(); //remove already serviced event from the queue

			try {
				Utils.Sleep(event.getIntervalTime());
			} catch (Exception e) {
				logger.error("", e);
			}

		}

	}

	private void serviceRequest(SimulationRequest event) throws GeneralException {

		FloorPacket floorPacket = null;
		byte[] data = null; //data to be sent to the Scheduler

		if (event.getFloorButton().equals(Direction.UP)) {
			floorPacket = new FloorPacket(Direction.UP, event.getFloor(), event.getStartTime(), event.getCarButton());
			data = floorPacket.generatePacketData();
		}else{
			floorPacket = new FloorPacket(Direction.DOWN, event.getFloor(), event.getStartTime(), event.getCarButton());
			data = floorPacket.generatePacketData();
		}

	}

	public void terminate() {
		receiveSocket.close();
		//cleanup goes here
	}
}
