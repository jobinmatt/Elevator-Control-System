//****************************************************************************
//
// Filename: ElevatorSubsystem.java
//
// Description: This creates an elevator car thread pool, and powers them on. 
//              Will also handle elevator cars 
//
// @author Shounak Amladi
//***************************************************************************

package core.Subsystems.ElevatorSubsystem;

import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.LoggingManager;
import core.Exceptions.ElevatorSubystemException;

public class ElevatorSubsystem {
	
	private static Logger logger = LogManager.getLogger(ElevatorCarThread.class);
	
	private int numberOfFloors;
	private int numberOfElev;
	private ElevatorCarThread[] carPool;
	
	public ElevatorSubsystem(int numElev, int numFloors) throws ElevatorSubystemException {
		
		this.numberOfElev = numElev;
		this.numberOfFloors = numFloors;
		this.carPool = new ElevatorCarThread[numElev];
		
		try {
			for (int i=0; i< this.numberOfElev;i++) {
				this.carPool[i] = new ElevatorCarThread(numFloors);
			}
		} catch (SocketException e) {
			throw new ElevatorSubystemException(e);
		}
		
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	for (ElevatorCarThread car: carPool) {
                    if (car != null) {
                        car.terminate();
                    }
            	}
                LoggingManager.terminate();
            }
        });
	}
	
	/**
	 * Starts the thread (powering on elevator)
	 **/
	public void activateElevators() {
		
		logger.info("Activating Elevators...");
		for (ElevatorCarThread cars : this.carPool) {
			cars.start();
		}
		logger.log(LoggingManager.getSuccessLevel(), LoggingManager.SUCCESS_MESSAGE);
	}
}
