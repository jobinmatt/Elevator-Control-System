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
import core.Exceptions.GeneralException;
import core.Messages.ElevatorMessage;
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
	private int[] elevatorFloorStates;
	private DatagramPacket floorPacket;
	private boolean shutdown = false;
	
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
		this.elevatorFloorStates = new int[this.numOfElevators];
		this.setFloorType(floorType);
		if(this.floorType.equals(FloorType.TOP)) {
			floorButtons = new FloorButton[1];
			floorButtons[0] = new FloorButton(Direction.DOWN);
		}
		else if(this.floorType.equals(FloorType.BOTTOM)) {
			floorButtons = new FloorButton[1];
			floorButtons[0] = new FloorButton(Direction.UP);
		}
		else {
			floorButtons = new FloorButton[2];
			floorButtons[0] = new FloorButton(Direction.DOWN);
			floorButtons[1] = new FloorButton(Direction.UP);
		}
		
		byte[] b = new byte[DATA_SIZE];
		this.floorPacket = new DatagramPacket(b, b.length);
		
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
				FloorMessage floorMessage = receivePacket(this.floorPacket);
				
				if (floorMessage.getShutdown()) {
					shutdown = true;
					atFloorTimer.cancel();
					break;
				}
				updateElevatorFloorState(floorMessage.getElevatorNum()-1,floorMessage.getSourceFloor());
				logger.info("Updated elevator floor: "+Arrays.toString(this.elevatorFloorStates));
			} catch (CommunicationException | IOException e) {
			}
        }
		logger.info("Shutting down floor");
    } 

    private void serviceRequest(SimulationRequest event) throws GeneralException {
    	
        FloorMessage floorPacket = null;
        byte[] temp = new byte[DATA_SIZE]; //data to be sent to the Scheduler
        byte[] data = new byte[DATA_SIZE]; //data to be sent to the Scheduler
        
        if (event.getEnd()) {
        	data = "End".getBytes();
        } else {
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
		//cleanup goes here
	}
	
	/**
	 * updates the current position of the elevator as the floor sees it
	 * index = elevNum-1
	 * */
	private void updateElevatorFloorState(int index, int floorNum) { 
		synchronized(elevatorFloorStates) {
			this.elevatorFloorStates[index] = floorNum; 
		}
	}
	public FloorMessage receivePacket(DatagramPacket packet)  throws IOException, CommunicationException {
				
		this.receiveSocket.receive(packet);
		
		return new FloorMessage(packet.getData(), packet.getLength());
	}
	public int[] getElevatorFloorStates() {
		return elevatorFloorStates;
	}

	public FloorType getFloorType() {
		return floorType;
	}

	public void setFloorType(FloorType floorType) {
		this.floorType = floorType;
	}
}
