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
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.LoggingManager;
import core.Exceptions.ElevatorSubystemException;

public class ElevatorSubsystem {
	private final String ELEVATOR_NAME = "ElevatorCar"; 
	private static Logger logger = LogManager.getLogger(ElevatorCarThread.class);
	
	private int numberOfFloors;
	private int numberOfElev;
	private Map<String, ElevatorCarThread> carPool;
	
	public ElevatorSubsystem(int numElev, int numFloors) throws ElevatorSubystemException {
		
		this.numberOfElev = numElev;
		this.numberOfFloors = numFloors;
		this.carPool = new HashMap<String, ElevatorCarThread>();
		
		try {
			for (int i=0; i< this.numberOfElev;i++) {
				
				this.carPool.put(ELEVATOR_NAME+i+1, new ElevatorCarThread(ELEVATOR_NAME+i+1, this.numberOfFloors));
			}
		} catch (SocketException e) {
			throw new ElevatorSubystemException(e);
		}
		
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	for (Map.Entry<String, ElevatorCarThread> car : carPool.entrySet()) { 
                    if (car != null) {
                        car.getValue().terminate();
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
		for (Map.Entry<String, ElevatorCarThread> car : carPool.entrySet()) { 
			car.getValue().start();
		}
		logger.log(LoggingManager.getSuccessLevel(), LoggingManager.SUCCESS_MESSAGE);
	}
}
