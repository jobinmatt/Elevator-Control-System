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
import core.ElevatorPacket;
import core.FloorPacket;
import core.Exceptions.CommunicationException;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.ElevatorSubsystemException;
import core.Utils.Utils;

/**
 * This creates an elevator car, and handles the properties and states for the car
 * */
public class ElevatorCarThread extends Thread {

	private static Logger logger = LogManager.getLogger(ElevatorCarThread.class);

	private boolean[] selectedFloors; //if true then its is pressed
	private Map<ElevatorComponentConstants, ElevatorComponentStates> carProperties;
	private int numberOfFloors;
	private int elevatorNumber;

	private DatagramSocket elevatorSocket;
	private DatagramPacket elevatorPacket;

	private ElevatorPacket ePacket;
	private int port;
	private InetAddress schedulerDomain;
	private int doorSleepTime;
	private int floorSleepTime;
	private boolean sentArrivalSensor;
	/**
	 * Constructor for elevator car
	 * 
	 * @param numFloors
	 * @throws ElevatorSubsystemException 
	 */
	public ElevatorCarThread(String name, int numFloors, InetAddress schedulerDomain) throws ElevatorSubsystemException {
		
		super (name);
		this.schedulerDomain = schedulerDomain;
		this.numberOfFloors = numFloors;
		this.sentArrivalSensor = false;
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
			byte[] b = new byte[1024];
			elevatorPacket = new DatagramPacket(b, b.length);
		} catch (ConfigurationParserException | SocketException e) {
			throw new ElevatorSubsystemException(e);
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
				
				if (ePacket.getCurrentFloor() > ePacket.getDestinationFloor()) {
					updateMotorStatus(ElevatorComponentStates.ELEV_MOTOR_DOWN);
					moveFloor();
					sentArrivalSensor = false;
					continue;
				}
				else if (ePacket.getCurrentFloor() < ePacket.getDestinationFloor()) {
					updateMotorStatus(ElevatorComponentStates.ELEV_MOTOR_UP);
					moveFloor();
					sentArrivalSensor = false;
					continue;
				} 
				else if (ePacket.getCurrentFloor() == ePacket.getDestinationFloor()) {
					updateDoorStatus(ElevatorComponentStates.ELEV_DOORS_OPEN);
					Utils.Sleep(doorSleepTime);
					updateDoorStatus(ElevatorComponentStates.ELEV_DOORS_CLOSE);
					
					if (ePacket.getRequestedFloor() != -1) {
						selectedFloors[ePacket.getRequestedFloor()] = true;
						logger.info("User Selected Floor: " + ePacket.getRequestedFloor());
					}
					updateMotorStatus(ElevatorComponentStates.ELEV_MOTOR_IDLE);
					ElevatorPacket requestFloor = new ElevatorPacket(ePacket.getRequestedFloor(), elevatorNumber);
					DatagramPacket requestedFloorPacket = new DatagramPacket (requestFloor.generatePacketData(), requestFloor.generatePacketData().length, schedulerDomain, ElevatorSubsystem.getSchedulerPorts().get(elevatorNumber));
					logger.debug("Arrived dest.\n");
					if (!sentArrivalSensor) {						
						this.sendArrivalSensorPacket();
						sentArrivalSensor = true;
					}
//					this.elevatorSocket.send(requestedFloorPacket);
					continue;
				} 
				else {
					updateMotorStatus(ElevatorComponentStates.ELEV_MOTOR_IDLE);
					sentArrivalSensor = false;
					continue;
				}

			} catch (CommunicationException | IOException | ElevatorSubsystemException e) {
				logger.error(e);
			}
		}		
	}
	
	public void moveFloor() throws ElevatorSubsystemException {
		
		Utils.Sleep(floorSleepTime);
		sendArrivalSensorPacket();
	}
	
	public void sendArrivalSensorPacket() throws ElevatorSubsystemException {
		
		try {
			ElevatorPacket arrivalSensor = new ElevatorPacket(true, elevatorNumber);		
			DatagramPacket arrivalSensorPacket = new DatagramPacket(arrivalSensor.generatePacketData(), arrivalSensor.generatePacketData().length, schedulerDomain, ElevatorSubsystem.getSchedulerPorts().get(elevatorNumber));
//			logger.debug("Sending to: " + schedulerDomain+":"+port+" data: "+Arrays.toString(arrivalSensorPacket.getData()));
//			logger.debug("Elevator Packet "+ arrivalSensor.toString());
			logger.debug("sent Arrival Sensor.");
			this.elevatorSocket.send(arrivalSensorPacket);
		} catch (CommunicationException | IOException e) {
			throw new ElevatorSubsystemException(e);
		}
	}
	
	public void receivePacket(DatagramPacket packet)  throws IOException, CommunicationException {
		
		this.elevatorSocket.receive(packet);
		this.ePacket = new ElevatorPacket(packet.getData(), packet.getLength());
		
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
}
