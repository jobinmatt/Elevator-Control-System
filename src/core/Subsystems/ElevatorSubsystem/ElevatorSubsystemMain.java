//****************************************************************************
//
// Filename: Elevator_Subsystem.java
//
// Description: Elevator Subsystem Class
//
//***************************************************************************

package core.Subsystems.ElevatorSubsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.LoggingManager;

public class ElevatorSubsystemMain {

	private static Logger logger = LogManager.getLogger(ElevatorSubsystemMain.class);
	
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
