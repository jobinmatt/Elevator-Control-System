//****************************************************************************
//
// Filename: ElevatorControlSubsystemMain.java
//
// Description: Elevator Control Subsystem Main Class
//
//***************************************************************************

package core.Subsystems.ElevatorControlSubsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.LoggingManager;

public class ElevatorControlSubsystemMain {

	private static Logger logger = LogManager.getLogger(ElevatorControlSubsystemMain.class);
	
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