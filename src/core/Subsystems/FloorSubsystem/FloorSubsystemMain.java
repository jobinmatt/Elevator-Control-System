//****************************************************************************
//
// Filename: Floor_Subsystem.java
//
// Description: Floor Subsystem Class
//
//***************************************************************************

package core.Subsystems.FloorSubsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.LoggingManager;

public class FloorSubsystemMain {

	private static Logger logger = LogManager.getLogger(FloorSubsystemMain.class);
	
	public static void main(String[] args) {
		
		logger.info(LoggingManager.BANNER + "Floor Subsystem\n");
		
		try {
			
			ConfigurationParser configurationParser = ConfigurationParser.getInstance();
			//test
			System.out.println(configurationParser.getString(ConfigurationParser.ELEVATOR_DOOR_TIME_SECONDS));
			
		} catch (Exception e) {
			logger.error("", e);
            System.exit(-1);
		}
		
		System.exit(0);	
	}
}
