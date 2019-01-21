//****************************************************************************
//
// Filename: ElevatorSubsystem.java
//
// Description: update elevator states 
//
//***************************************************************************
package core.Subsystems.ElevatorSubsystem;

import java.net.SocketException;

/**
 * 
 * This creates an elevator car thread pool, and powers them on. Will also handle elevator cars 
 * @author Shounak Amladi
 * */
public class ElevatorSubsystem {
	
	
	private int numFloors, numElev;
	private ElevatorCar[] carPool; //lol
	public ElevatorSubsystem(int numElev, int numFloors) throws SocketException {
		this.numElev = numElev;
		this.numFloors = numFloors;
		this.carPool = new ElevatorCar[numElev];
		for (int i=0; i<this.numElev;i++) {
			this.carPool[i]= new ElevatorCar(numFloors);
		}
	}
	/**
	 * Starts the thread (powering on elevator)
	 * */
	public void activateElevators() {
		for (ElevatorCar cars : this.carPool) {
			cars.start();
		}
	}
	
	//other elevator stuff goes here

}
