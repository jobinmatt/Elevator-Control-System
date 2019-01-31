//****************************************************************************
//
// Filename: FloorThread.java
//
// Description: Floor thread Class
//
// @author Dharina H.
//***************************************************************************

package core.Subsystems.FloorSubsystem;

import core.Direction;
import core.Exceptions.CommunicationException;
import core.Exceptions.HostActionsException;
import core.FloorPacket;
import core.Exceptions.GeneralException;
import core.Exceptions.FloorSubsystemException;
import core.Utils.HostActions;
import core.Utils.SimulationRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The FloorThread represents a floor on which a person can request an elevator. Maintains a queue of events.
 */
public class FloorThread extends Thread {

	private static Logger logger = LogManager.getLogger(FloorThread.class);
	private int port; //port to communicate with the scheduler
	private Queue<SimulationRequest> events;
	private int floorNumber;
	DatagramSocket receiveSocket;
	private InetAddress schedulerSubsystemAddress;
	private Timer atFloorTimer;
	private final int DATA_SIZE = 1024;
	private String lampSensor = "";
	private String buttonSensor = "";


	/**
	 * Creates a floor thread
	 */
	public FloorThread(String name, int floorNumber, InetAddress schedulerSubsystemAddress, int port, Timer sharedTimer) throws GeneralException {

		super(name);

		events = new LinkedList<>();
		this.floorNumber = floorNumber;
		this.schedulerSubsystemAddress = schedulerSubsystemAddress;
		this.port = port;
		this.atFloorTimer = sharedTimer;
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

		this.atFloorTimer.schedule(new TimerTask () {
			public void run() {
				try {
					logger.info("Scheduling request: "+e.toString());
					serviceRequest(e);
					receiveArrival();
				} catch (GeneralException e) {
					logger.error(e);
				}
			}
		}, e.getStartTime());

	}

	/**
	 *Services each floor request
	 */
	@Override
	public void run() {

		while(true) {

        }
    }

    private void serviceRequest(SimulationRequest event) throws GeneralException {
    	
        FloorPacket floorPacket = null;
        byte[] temp = new byte[DATA_SIZE]; //data to be sent to the Scheduler
        byte[] data = new byte[DATA_SIZE]; //data to be sent to the Scheduler
        
        floorPacket = new FloorPacket(event.getFloorButton(), event.getFloor(), event.getCarButton());
        data = floorPacket.generatePacketData();
            
        DatagramPacket tempPacket = new DatagramPacket(temp, temp.length);
        tempPacket.setData(data);
        tempPacket.setAddress(this.schedulerSubsystemAddress);
        tempPacket.setPort(port);
        logger.info("Buffer Data: "+ Arrays.toString(data));
        HostActions.send( tempPacket, Optional.of(receiveSocket));

    }

    private void receiveArrival() throws HostActionsException, CommunicationException {
		byte[] data = new byte[DATA_SIZE];
		DatagramPacket packetFromScheduler = new DatagramPacket(data, data.length);
		HostActions.receive(packetFromScheduler, receiveSocket);

		FloorPacket arrivalSensorPacket = new FloorPacket(data, data.length, true);
		if(arrivalSensorPacket.isValid()) {
			if(arrivalSensorPacket.getArrivalSensor()) {
				Direction direction = arrivalSensorPacket.getDirection();
				switch (direction) {
					case UP:
						lampSensor = "UP";
						buttonSensor = "UP";
						break;
					case DOWN:
						lampSensor = "DOWN";
						buttonSensor = "DOWN";
						break;
				}

			}
		}
		printArrival();
	}

	private void printArrival() {
		logger.debug("The elevator is here. Lamp Sensor flashing [" + lampSensor + "]. Button sensor flashing [" + buttonSensor + "].");
	}
	/**
	 * To be used by the Scheduler to send an arrival sensor
	 * @param port
	 * @param direction direction that the elevator is coming from
	 * @throws FloorSubsystemException
	 */
	public void sendArrivalSensorPacket(int port, Direction direction) throws FloorSubsystemException {

		try {
			FloorPacket arrivalSensor = new FloorPacket(true, direction);
			DatagramPacket arrivalSensorPacket = new DatagramPacket(arrivalSensor.generateArrivalSensorData(), arrivalSensor.generatePacketData().length, schedulerSubsystemAddress, port);
			this.receiveSocket.send(arrivalSensorPacket);
		} catch (CommunicationException | IOException e) {
			throw new FloorSubsystemException(e);
		}
	}

	public void terminate() {
		receiveSocket.close();
		//cleanup goes here
	}
}
