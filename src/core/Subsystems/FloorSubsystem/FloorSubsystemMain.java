//****************************************************************************
//
// Filename: FloorSubsystemMain.java
//
// Description: Floor Subsystem Main Class
//
//***************************************************************************

package core.Subsystems.FloorSubsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.LoggingManager;

public class FloorSubsystemMain {

	private static Logger logger = LogManager.getLogger(FloorSubsystemMain.class);
	
	public static void main(String[] args) throws Exception {
		
		logger.info(LoggingManager.BANNER + "Floor Subsystem\n");
		
		try {
			
			ConfigurationParser configurationParser = ConfigurationParser.getInstance();
            FloorSubsystem floorSystem = new FloorSubsystem("filename", Integer.getInteger(configurationParser.NUMBER_OF_FLOORS));
            floorSystem.readFile();
            //****** send List<SimulationEvent> events to the scheduler ? ****
            floorSystem.startFloorThreads();
			
		} catch (Exception e) {
			logger.error("", e);
            System.exit(-1);
		}

	}
}
