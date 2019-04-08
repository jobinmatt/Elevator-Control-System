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
import core.PerformanceTimer;
import core.Exceptions.CommunicationException;
import core.Exceptions.GeneralException;
import core.Messages.FloorMessage;
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
import java.util.HashMap;
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
	private FloorType floorType;
	private FloorButton[] floorButtons;
	DatagramSocket receiveSocket;
	private InetAddress schedulerAddress;
	private Timer atFloorTimer;
	private final int DATA_SIZE = 1024;
	private int numOfElevators=0;
	private FloorStatus[] elevatorFloorStates;
	private DatagramPacket floorPacket;
	private boolean shutdown = false;
	private PerformanceTimer timer;
	private boolean firstStart = true;
	
	/**
	 * Creates a floor thread
	 */
	public FloorThread(String name, int floorNumber, InetAddress schedulerAddress, Timer sharedTimer, int numElev, FloorType floorType) throws GeneralException {

		super(name);

		events = new LinkedList<>();
		this.floorNumber = floorNumber;
		this.schedulerAddress = schedulerAddress;
		this.atFloorTimer = sharedTimer;
		this.numOfElevators = numElev;
		this.elevatorFloorStates = new FloorStatus[numElev];
		for (int i=0;i<numElev;i++) {
			this.elevatorFloorStates[i] = new FloorStatus(i+1, 1, core.Direction.STATIONARY,0,0);

		}
		this.setFloorType(floorType);
		if (this.floorType.equals(FloorType.TOP)) {
			floorButtons = new FloorButton[1];
			floorButtons[0] = new FloorButton(Direction.DOWN);
		} else if (this.floorType.equals(FloorType.BOTTOM)) {
			floorButtons = new FloorButton[1];
			floorButtons[0] = new FloorButton(Direction.UP);
		} else {
			floorButtons = new FloorButton[2];
			floorButtons[0] = new FloorButton(Direction.DOWN);
			floorButtons[1] = new FloorButton(Direction.UP);
		}
		byte[] b = new byte[DATA_SIZE];
		this.floorPacket = new DatagramPacket(b, b.length);
		this.timer = new PerformanceTimer();
		
		try {
			receiveSocket = new DatagramSocket();
			this.port = receiveSocket.getLocalPort();
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

		while (!shutdown) {
		
			try {
				FloorMessage floorMessage;
				if (!firstStart) {
					timer.start();
					floorMessage = receivePacket(this.floorPacket);
					timer.end();
				} else {
					floorMessage = receivePacket(this.floorPacket);
					firstStart = false;
				}
				
				if (floorMessage.getShutdown()) {
					shutdown = true;
					atFloorTimer.cancel();
					break;
				}
				updateElevatorFloorState(floorMessage);
				
//				if(floorMessage.getDirection().equals(Direction.STATIONARY) && this.floorNumber == floorMessage.getSourceFloor()) {
//					logger.debug("Elevator "+ (floorMessage.getElevatorNum()) +" is stationary! , Source Floor: "+floorMessage.getSourceFloor()+", Dest Floor: "+floorMessage.getTargetFloor());
//				}
//				logger.info("Updated elevator floor: "+Arrays.toString(this.elevatorFloorStates));
			} catch (CommunicationException | IOException e) {
			}
        }
		logger.info("Shutting down floor");
    } 
	
	private void updateErrorStatus(FloorMessage floorMessage) {
		synchronized(elevatorFloorStates) {
			int index = floorMessage.getElevatorNum()-1;
			this.elevatorFloorStates[index].setErrorCode(floorMessage.getErrorCode());
			this.elevatorFloorStates[index].setErrorFloor(floorMessage.getErrorFloor());
		}
	}
	private void updateFloorButtonState(FloorMessage floorMessage) {
		if ((this.floorNumber == floorMessage.getSourceFloor()) && (floorMessage.getDirection().equals(Direction.STATIONARY))) {
			if (this.floorType.equals(FloorType.TOP) || this.floorType.equals(FloorType.BOTTOM)) {
				if (this.floorButtons[0].getStatus()) {
					this.floorButtons[0].setButtonNotPressed();
//					logger.info("Floor Number: " + this.floorNumber + " - Button turned OFF.");
				}
			} else {
				if (floorButtons[0].getStatus()&&floorMessage.getTargetFloor()<this.floorNumber) {
					this.floorButtons[0].setButtonNotPressed();
//					logger.info("Floor Number: " + this.floorNumber + " - DOWN turned OFF.");
				} else if(floorButtons[1].getStatus()&&floorMessage.getTargetFloor()>this.floorNumber){
					this.floorButtons[1].setButtonNotPressed();
//					logger.info("Floor Number: " + this.floorNumber + " - UP turned OFF.");
				}
			}
		}
	}
	
	private void turnOnFloorButton(SimulationRequest event) {
		if (this.floorType.equals(FloorType.BOTTOM) || this.floorType.equals(FloorType.TOP)) {
			this.floorButtons[0].setButtonPressed();
			logger.info("Floor Number: " + this.floorNumber + " - Button turned ON.");
		} else {
			if (event.getFloorButton().equals(Direction.UP)) {// This is probably redundant we could force NORMAL
																// floors to follow a specific pattern for floor
																// buttons.
				if (this.floorButtons[0].getDirection().equals(Direction.UP)) {
					this.floorButtons[0].setButtonPressed();
				} else {
					this.floorButtons[1].setButtonPressed();
				}
				logger.info("Floor Number: " + this.floorNumber + " - UP button turned ON.");
			} else {
				if (this.floorButtons[0].getDirection().equals(Direction.DOWN)) {
					this.floorButtons[0].setButtonPressed();
				} else {
					this.floorButtons[1].setButtonPressed();
				}
				logger.info("Floor Number: " + this.floorNumber + " - DOWN button turned ON.");
			}
		}
	}

    private void serviceRequest(SimulationRequest event) throws GeneralException {
    	
        FloorMessage floorPacket = null;
        byte[] temp = new byte[DATA_SIZE]; //data to be sent to the Scheduler
        byte[] data = new byte[DATA_SIZE]; //data to be sent to the Scheduler
        
        if (event.getEnd()) {
        	data = "End".getBytes();
        } else {
        	turnOnFloorButton(event);
	        floorPacket = new FloorMessage(event.getFloorButton(), event.getFloor(), event.getCarButton(), event.getErrorCode(), event.getErrorElevator());
	        data = floorPacket.generatePacketData();
        }
            
        DatagramPacket tempPacket = new DatagramPacket(temp, temp.length);
        tempPacket.setData(data);
        tempPacket.setAddress(this.schedulerAddress);
        tempPacket.setPort(FloorSubsystem.getSchedulerPorts().get(floorNumber));
        logger.info("Buffer Data: "+ Arrays.toString(data));
        HostActions.send(tempPacket, Optional.of(receiveSocket));
    }
    
    /** 
	 * Get the port that the socket is running on 
	 * @return port: int
	 * */
	public int getPort() {		
		return this.port;
	}
	
	/**
	 * Get the port that the socket is running on
	 * @return floorNumber: int
	 * */
	public int getFloorNumber() {		
		return this.floorNumber;
	}

	public void terminate() {
		receiveSocket.close();
		try {
			timer.print("Floor Interface: ");
		} catch (Exception e) {}
	}
	
	/**
	 * updates the current position of the elevator as the floor sees it
	 * index = elevNum-1
	 * */
	private void updateElevatorFloorState(FloorMessage floorMessage) { 
		synchronized(elevatorFloorStates) {
			int index = floorMessage.getElevatorNum()-1;
			this.elevatorFloorStates[index].setFloorStatus(floorMessage.getSourceFloor());
			this.elevatorFloorStates[index].setDir(floorMessage.getDirection());
			updateErrorStatus(floorMessage);
			updateFloorButtonState(floorMessage);
		}
	}
	public FloorMessage receivePacket(DatagramPacket packet)  throws IOException, CommunicationException {
				
		this.receiveSocket.receive(packet);
		
		return new FloorMessage(packet.getData(), packet.getLength());
	}
	
	public FloorStatus[] getFloorStatus() {
		return elevatorFloorStates;
	}
	
	public FloorType getFloorType() {
		return floorType;
	}

	public void setFloorType(FloorType floorType) {
		this.floorType = floorType;
	}
}
