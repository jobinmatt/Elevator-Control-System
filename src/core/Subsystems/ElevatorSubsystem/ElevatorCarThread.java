//****************************************************************************
//
// Filename: ElevatorCar.java
//
// Description: This creates an elevator car, and handles the properties and states for the car
//
// @author Shounak Amladi
//***************************************************************************
package core.Subsystems.ElevatorSubsystem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.Direction;
import core.LoggingManager;
import core.Exceptions.CommunicationException;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.ElevatorSubsystemException;
import core.Messages.ElevatorMessage;
import core.Utils.Utils;

/**
 * This creates an elevator car, and handles the properties and states for the car
 * */
public class ElevatorCarThread extends Thread {

	private static Logger logger = LogManager.getLogger(ElevatorCarThread.class);

	private static final int DATA_SIZE = 1024;
	private static final int HARD_CODE = 1;
	private static final int TRANSIENT_CODE = 2;
	private static final int WAIT_TIME = 3000;
	private boolean[] selectedFloors; //if true then its is pressed
	private Map<ElevatorComponentConstants, ElevatorComponentStates> carProperties;
	private int numberOfFloors;
	private int elevatorNumber;
	private String name;

	private DatagramSocket elevatorSocket;
	private DatagramPacket elevatorPacket;

	private ElevatorMessage ePacket;
	private int port;
	private InetAddress schedulerAddress;
	private int doorSleepTime;
	private int floorSleepTime;
	private int currentFloor;
	private int destinationFloor;
	private boolean sentArrivalSensor;
	/**
	 * Constructor for elevator car
	 * 
	 * @param numFloors
	 * @throws ElevatorSubsystemException 
	 */
	public ElevatorCarThread(String name, int numFloors, InetAddress schedulerAddress) throws ElevatorSubsystemException {
		
		super (name);
		this.name = name;
		this.schedulerAddress = schedulerAddress;
		this.numberOfFloors = numFloors;
		this.setSentArrivalSensor(false);
		selectedFloors = new boolean[this.numberOfFloors];
//		elevatorNumber = name.charAt(name.length() - 1);
		elevatorNumber = Integer.parseInt(name.substring(name.length()-1));
		//initialize component states
		carProperties = new HashMap<ElevatorComponentConstants, ElevatorComponentStates>();
		carProperties.put(ElevatorComponentConstants.ELEV_DOORS, ElevatorComponentStates.ELEV_DOORS_CLOSE);
		carProperties.put(ElevatorComponentConstants.ELEV_MOTOR, ElevatorComponentStates.ELEV_MOTOR_IDLE);

		try {
			doorSleepTime = ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_DOOR_TIME_SECONDS)*1000;
			floorSleepTime = ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_FLOOR_TRAVEL_TIME_SECONDS)*1000;
		
			//initialize communication stuff
			this.elevatorSocket = new DatagramSocket();
			this.port = elevatorSocket.getLocalPort();
			byte[] b = new byte[DATA_SIZE];
			elevatorPacket = new DatagramPacket(b, b.length);
		} catch (ConfigurationParserException | SocketException e) {
			throw new ElevatorSubsystemException(e);
		}
	}

	@Override
	public void run() {
		//init
		logger.debug(getName() + ": Powered On");

		while (true) {
			// if source > dest then going down
			//if soure < dest doing up
			//if source = dest here
			try {
				this.receivePacket(elevatorPacket);
				currentFloor = ePacket.getCurrentFloor();
				destinationFloor = ePacket.getDestinationFloor();

				if(ePacket.getErrorCode() == HARD_CODE && ePacket.getErrorFloor() == currentFloor) {
					Utils.Sleep(floorSleepTime + WAIT_TIME);
					sendArrivalSensorPacket();
					logger.info("Hard error message received, elevator thread being interrupted");
					break;
				}

				updateButtonStatus(ElevatorComponentStates.ELEV_FLOOR_BUTTON_ON, destinationFloor);

				if (currentFloor > destinationFloor) {
					updateMotorStatus(ElevatorComponentStates.ELEV_MOTOR_DOWN);
					moveFloor(ePacket, Direction.DOWN);
					
				} else if (currentFloor < destinationFloor) {
					updateMotorStatus(ElevatorComponentStates.ELEV_MOTOR_UP);
					moveFloor(ePacket, Direction.UP);

				}
				
				if (currentFloor == destinationFloor && getMotorStatus() != ElevatorComponentStates.ELEV_MOTOR_IDLE) {

					updateMotorStatus(ElevatorComponentStates.ELEV_MOTOR_IDLE);					
					updateDoorStatus(ElevatorComponentStates.ELEV_DOORS_OPEN);
					updateButtonStatus(ElevatorComponentStates.ELEV_FLOOR_BUTTON_OFF, destinationFloor);

					Utils.Sleep(doorSleepTime);
					
					if (destinationFloor != -1) {
						selectedFloors[ePacket.getDestinationFloor()] = true;
						logger.info("User Selected Floor: " + ePacket.getDestinationFloor());
					}
					
					if (ePacket.getErrorCode() == TRANSIENT_CODE) {
						logger.info("Unable to Close Doors");
						sendFailureDoorRequest();
						this.receivePacket(elevatorPacket);
						if (ePacket.getForceCloseStatus()) {
							logger.info("Force Closing door in " + WAIT_TIME/1000 + " seconds");
							Utils.Sleep(WAIT_TIME);
						}
					}
					
					updateDoorStatus(ElevatorComponentStates.ELEV_DOORS_CLOSE);
										
					logger.debug("Arrived destination\n");
				}
				sendArrivalSensorPacket();
			} catch (CommunicationException | IOException | ElevatorSubsystemException e) {
				logger.error(e);
			}
		}		
	}
	
	/**
	 * checks if the specified floor button is pressed
	 * @param index
	 * @return boolean
	 * */
	public boolean floorIsPressed(int index) {

		return this.selectedFloors[index];
	}

	/**
	 * Gets the number of floors
	 * @param
	 * @return int
	 * */
	public int getFloorNumber() {

		return this.numberOfFloors;
	}
	
	/**
	 * Returns the elevator number
	 * @param
	 * @return int
	 * */
	public int getElevatorNumber(){
		return this.elevatorNumber;
	}

	/**
	 * Get state of the motor
	 * @return ElevatorComponentStates
	 * */
	public synchronized ElevatorComponentStates getMotorStatus() {

		return carProperties.get(ElevatorComponentConstants.ELEV_MOTOR);
	}

	/**
	 * Get state of the door
	 * @return ElevatorComponentStates
	 * */
	public synchronized ElevatorComponentStates getDoorStatus() {

		return carProperties.get(ElevatorComponentConstants.ELEV_DOORS);
	}
	/**
	 * Updates the elevator car's motor status
	 * @param state
	 * 
	 * */
	public synchronized void updateMotorStatus(ElevatorComponentStates state) {
		
		logger.info("\nElevator Motor: " + state.name());
		carProperties.replace(ElevatorComponentConstants.ELEV_MOTOR, state);
	}

	public synchronized  void updateButtonStatus(ElevatorComponentStates state, int destinationFloor) {
		logger.info("\nElevator Floor Button " + destinationFloor + ": " + state.name());
	}

	/**
	 * Updates the elevator car's door status
	 * @param state
	 * */
	public synchronized void updateDoorStatus(ElevatorComponentStates state) {

		logger.info("Elevator Door: " + state.name());
		carProperties.replace(ElevatorComponentConstants.ELEV_DOORS, state);
	}
	/**
	 * Get the port that the socket is running on
	 * @return port
	 * */
	public int getPort() {
		
		return this.port; 
	}
	
	public void moveFloor(ElevatorMessage em, Direction dir) throws ElevatorSubsystemException {
		
		moveFloor(em, dir, floorSleepTime);
	}
	
	public void moveFloor(ElevatorMessage em, Direction dir, int time) throws ElevatorSubsystemException {
		
		Utils.Sleep(time);
		
		if (dir == Direction.UP) {
			currentFloor++;
		}
		else if (dir == Direction.DOWN) {
			currentFloor--;
		}
	}
	
	public void sendArrivalSensorPacket() throws ElevatorSubsystemException {
		
		try {
			ElevatorMessage arrivalSensor ;
			arrivalSensor = new ElevatorMessage(currentFloor, -1, elevatorNumber);
			arrivalSensor.setArrivalSensor(true);
			int port = ElevatorSubsystem.getSchedulerPorts().get(elevatorNumber);
			DatagramPacket arrivalSensorPacket = new DatagramPacket(arrivalSensor.generatePacketData(), arrivalSensor.generatePacketData().length, schedulerAddress, port);
			logger.debug("Sending to: " + schedulerAddress+":"+port+" data: "+Arrays.toString(arrivalSensorPacket.getData()));
			logger.debug("Elevator Packet "+ arrivalSensor.toString());
			this.elevatorSocket.send(arrivalSensorPacket);
		} catch (CommunicationException | IOException e) {
			throw new ElevatorSubsystemException(e);
		}
	}
	
	public void sendFailureDoorRequest() throws ElevatorSubsystemException{

		try {
			ElevatorMessage msg = new ElevatorMessage();
			byte[] data = msg.generateDoorFailureMessage();
			int port = ElevatorSubsystem.getSchedulerPorts().get(elevatorNumber);
			DatagramPacket packet = new DatagramPacket(data, data.length, schedulerAddress, port);
			this.elevatorSocket.send(packet);
		} catch (CommunicationException | IOException e) {
			throw new ElevatorSubsystemException(e);
		}
	}
	
	public void receivePacket(DatagramPacket packet)  throws IOException, CommunicationException {
		
		this.elevatorSocket.receive(packet);
		this.ePacket = new ElevatorMessage(packet.getData(), packet.getLength());
		
		if (!ePacket.isValid()) {
			throw new CommunicationException("Invalid packet data, how you do?");
		}
		logger.debug("Received: "+ ePacket.toString());
	}

	public void sendPacket(DatagramPacket packet, int port, InetAddress domain) throws CommunicationException, IOException {
		
		packet.setData(ePacket.generatePacketData());
		packet.setAddress(domain);
		packet.setPort(port);
		logger.debug("Sending: " + ePacket.toString());
		this.elevatorSocket.send(packet);
	}
	
	public void terminate() {
		System.out.println("\nTearDown Elevator...");
		this.elevatorSocket.close();
		System.out.println("TearDown Complete");
		//cleanup items go here
	}

	public boolean isSentArrivalSensor() {
		return sentArrivalSensor;
	}

	public void setSentArrivalSensor(boolean sentArrivalSensor) {
		this.sentArrivalSensor = sentArrivalSensor;
	}
}
