//****************************************************************************
//
// Filename: ElevatorCar.java
//
// Description: This creates an elevator car, and handles the properties and states for the car
//
//***************************************************************************
package core.Subsystems.ElevatorSubsystem;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.main.FloorSubsystemMain;
/**
 * 
 * This creates an elevator car, and handles the properties and states for the car
 * @author Shounak Amladi
 * */
public class ElevatorCar extends Thread {

	private static Logger logger = LogManager.getLogger(FloorSubsystemMain.class);
	private boolean[] floors; //if true then its is pressed
	private Map<ElevatorComponentConstants, ElevatorComponentStates> carProperties;
	private int floorNum;
	
	private DatagramSocket sckElevator;
	private int sckPort;
	/**
	 * Constructor for elevator car
	 * @param numFloors
	 * */
	public ElevatorCar(int numFloors){
		//init floors
		this.floorNum = numFloors;
		floors = new boolean[this.floorNum];
		
		//init componenet states
		carProperties = new HashMap<ElevatorComponentConstants, ElevatorComponentStates>();
		carProperties.put(ElevatorComponentConstants.ELEV_DOORS, ElevatorComponentStates.ELEV_DOORS_CLOSE);
		carProperties.put(ElevatorComponentConstants.ELEV_MOTOR, ElevatorComponentStates.ELEV_MOTOR_IDLE);
		
		//init comms stuff
		try {			
			this.sckElevator = new DatagramSocket();
		}
		catch(SocketException e) {
			logger.error("Error creating the socket", e);
		}
		
	}
	/**
	 * checks if the specified floor button is pressed
	 * @param index
	 * @return boolean
	 * */
	public boolean floorIsPressed(int index) {
		return this.floors[index];
	}
	/**
	 * Gets the number of floors 
	 * @param
	 * @return int
	 * */
	public int getFloorNumber() {
		return this.floorNum;
	}
	
	
	/**
	 * Get state of the motor
	 * @return ElevatorComponentStates
	 * */
	public ElevatorComponentStates getMotorStatus() {
		return carProperties.get(ElevatorComponentConstants.ELEV_MOTOR);
	}
	/**
	 * Get state of the door 
	 * @return ElevatorComponentStates
	 * */
	public ElevatorComponentStates getDoorStatus() {
		return carProperties.get(ElevatorComponentConstants.ELEV_MOTOR);
	}
	/**
	 * Updates the elevator car's motor status
	 * @param state
	 * 
	 * */
	public void updateMotorStatus(ElevatorComponentStates state) {
		carProperties.replace(ElevatorComponentConstants.ELEV_MOTOR, state);
	}
	/**
	 * Updates the elevator car's door status
	 * @param state
	 * */
	public void updateDoorStatus(ElevatorComponentStates state) {
		carProperties.replace(ElevatorComponentConstants.ELEV_DOORS, state);
	}
	

	
	
	@Override
	public void run() {
		// Thread stuff goes in here!!!!!!!
		logger.debug("Powered On");
	}

}
