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
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.ElevatorPacket;
import core.Exceptions.CommunicationException;
import core.Exceptions.ConfigurationParserException;

/**
 * This creates an elevator car, and handles the properties and states for the car
 * */
public class ElevatorCarThread extends Thread {

	private static Logger logger = LogManager.getLogger(ElevatorCarThread.class);

	private boolean[] selectedFloors; //if true then its is pressed
	private Map<ElevatorComponentConstants, ElevatorComponentStates> carProperties;
	private int numberOfFloors;

	private DatagramSocket elevatorSocket;
	private DatagramPacket elevatorPacket;

	private ElevatorPacket ePacket;
	private int port;
	private InetAddress schedulerDomain;
	private int sleepTime;
	
	/**
	 * Constructor for elevator car
	 * 
	 * @param numFloors
	 * @throws SocketException 
	 */
	public ElevatorCarThread(String name, int numFloors, int port, InetAddress schedulerDomain) throws SocketException {
		
		super (name);
		this.schedulerDomain = schedulerDomain;
		this.numberOfFloors = numFloors;
		this.port = port;
		selectedFloors = new boolean[this.numberOfFloors];

		try {
			sleepTime = ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_DOOR_TIME_SECONDS);
		} catch (ConfigurationParserException e) {

		}

		//initialize component states
		carProperties = new HashMap<ElevatorComponentConstants, ElevatorComponentStates>();
		carProperties.put(ElevatorComponentConstants.ELEV_DOORS, ElevatorComponentStates.ELEV_DOORS_CLOSE);
		carProperties.put(ElevatorComponentConstants.ELEV_MOTOR, ElevatorComponentStates.ELEV_MOTOR_IDLE);

		//initialize communication stuff
		this.elevatorSocket = new DatagramSocket(this.port);
		byte[] b = new byte[1024];
		elevatorPacket = new DatagramPacket(b, b.length);
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

		return carProperties.get(ElevatorComponentConstants.ELEV_MOTOR);
	}
	/**
	 * Updates the elevator car's motor status
	 * @param state
	 * 
	 * */
	public synchronized void updateMotorStatus(ElevatorComponentStates state) {
		carProperties.replace(ElevatorComponentConstants.ELEV_MOTOR, state);
	}

	/**
	 * Updates the elevator car's door status
	 * @param state
	 * */
	public synchronized void updateDoorStatus(ElevatorComponentStates state) {

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
			// if source >dest then going down
			//if soure < dest doing up
			//if source = dest here
			try {
				this.receivePacket(elevatorPacket);
				int recPort = elevatorPacket.getPort();
				
				if (ePacket.getCurrentFloor() > ePacket.getDestinationFloor()) {

					if (this.getMotorStatus() == ElevatorComponentStates.ELEV_MOTOR_IDLE) {//means no one is in the ele dont need to openm doors
						updateMotorStatus(ElevatorComponentStates.ELEV_MOTOR_DOWN);
					} else {
						updateDoorStatus(ElevatorComponentStates.ELEV_DOORS_OPEN);
						Thread.sleep(sleepTime);// sleeps for seconds 
						updateDoorStatus(ElevatorComponentStates.ELEV_DOORS_CLOSE);
						updateMotorStatus(ElevatorComponentStates.ELEV_MOTOR_DOWN);
					}
				}
				else if (ePacket.getCurrentFloor() < ePacket.getDestinationFloor()) {
					
					if (this.getMotorStatus() == ElevatorComponentStates.ELEV_MOTOR_IDLE) {
						updateMotorStatus(ElevatorComponentStates.ELEV_MOTOR_UP);
					} else {
						updateDoorStatus(ElevatorComponentStates.ELEV_DOORS_OPEN);
						Thread.sleep(sleepTime);// sleeps for seconds 
						updateDoorStatus(ElevatorComponentStates.ELEV_DOORS_CLOSE);
						updateMotorStatus(ElevatorComponentStates.ELEV_MOTOR_UP);
					}
					
				} else {
					updateDoorStatus(ElevatorComponentStates.ELEV_DOORS_OPEN);
					Thread.sleep(sleepTime);// sleeps for seconds TODO addd to config file 
					updateDoorStatus(ElevatorComponentStates.ELEV_DOORS_CLOSE);
					if (ePacket.getRequestedFloor() != -1) {
						selectedFloors[ePacket.getRequestedFloor()] = true;
						logger.info("User Selected Floor: " + ePacket.getRequestedFloor());
					}
				}

				this.sendPacket(elevatorPacket, recPort,this.schedulerDomain);

			} catch (CommunicationException | IOException | InterruptedException e) {
				logger.error(e);
			}
		}		
	}
	
	public void receivePacket(DatagramPacket packet)  throws IOException, CommunicationException {
		
		this.elevatorSocket.receive(packet);
		this.ePacket = new ElevatorPacket(packet.getData(), packet.getLength());
		if (!ePacket.isValid())
			throw new CommunicationException("Invalid packet data, how you do?");
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
		
		this.elevatorSocket.close();
		//cleanup items go here
	}
}
