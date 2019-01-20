//****************************************************************************
//
// Filename: ElevatorControlSubsystem.java
//
// Description: Elevator Control Subsystem Class
//
//***************************************************************************

package core.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ElevatorControlSubsystemMain {
	
	private static Logger logger = LogManager.getLogger(ElevatorControlSubsystemMain.class);
	
	
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
