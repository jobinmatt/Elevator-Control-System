//****************************************************************************
//
// Filename: ElevatorCar.java
//
// Description: This creates an elevator car, and handles the properties and states for the car
//
// @author Shounak Amladi
//***************************************************************************
package core.Subsystems.ElevatorSubsystem;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This creates an elevator car, and handles the properties and states for the car
 * */
public class ElevatorCarThread extends Thread {

	private static Logger logger = LogManager.getLogger(ElevatorCarThread.class);
	
	private boolean[] selectedFloors; //if true then its is pressed
	private Map<ElevatorComponentConstants, ElevatorComponentStates> carProperties;
	private int numberOfFloors;
	
	private DatagramSocket elevatorSocket;
	private int port;
	
	/**
	 * Constructor for elevator car
	 * @param numFloors
	 * */
	public ElevatorCarThread(int numFloors) throws SocketException {
		
		this.numberOfFloors = numFloors;
		selectedFloors = new boolean[this.numberOfFloors];
		
		//initialize component states
		carProperties = new HashMap<ElevatorComponentConstants, ElevatorComponentStates>();
		carProperties.put(ElevatorComponentConstants.ELEV_DOORS, ElevatorComponentStates.ELEV_DOORS_CLOSE);
		carProperties.put(ElevatorComponentConstants.ELEV_MOTOR, ElevatorComponentStates.ELEV_MOTOR_IDLE);
		
		//initialize communication stuff		
		this.elevatorSocket = new DatagramSocket();
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
	
	@Override
	public void run() {

		logger.debug("Powered On");
	}

	public void terminate() {
		
		//cleanup items go here
	}
}
