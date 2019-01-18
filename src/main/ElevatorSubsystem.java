//****************************************************************************
//
// Filename: Elevator_Subsystem.java
//
// Description: Elevator Subsystem Class
//
//***************************************************************************
package main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ElevatorSubsystem {

	private static Logger logger = LogManager.getLogger(ElevatorSubsystem.class);	
	
	public static void main(String[] args) {
		
		logger.info(LoggingManager.BANNER + "Elevator Subsystem\n");
		
		try {
			
		} catch (Exception e) {
			logger.error("", e);
            System.exit(-1);
		}
		
		System.exit(0);
		
	}

}
