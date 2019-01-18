//****************************************************************************
//
// Filename: ElevatorControlSystems.java
//
// Description: Elevator Control Systems Class
//
//***************************************************************************

package main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ElevatorControlSubSystems {
	
	private static Logger logger = LogManager.getLogger(ElevatorControlSubSystems.class);
	
	
	public static void main(String[] args) {
		
		logger.info(LoggingManager.BANNER + "Control Subsystem\n");
		
		try {
			
		} catch (Exception e) {
			logger.error("", e);
            System.exit(-1);
		}
		
		System.exit(0);
	}

}
