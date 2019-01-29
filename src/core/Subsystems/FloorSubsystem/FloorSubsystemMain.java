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

import java.net.InetAddress;

public class FloorSubsystemMain {

	private static Logger logger = LogManager.getLogger(FloorSubsystemMain.class);

	public static void main(String[] args) throws Exception {

		logger.info(LoggingManager.BANNER + "Floor Subsystem\n");

		try {
			ConfigurationParser configurationParser = ConfigurationParser.getInstance();
			
			int numFloors = configurationParser.getInt(ConfigurationParser.NUMBER_OF_FLOORS);
			

			InetAddress schedulerSubsystemAddress =InetAddress.getByName(configurationParser.getString(ConfigurationParser.SCHEDULER_ADDRESS));
			int floorInitPort = configurationParser.getInt(ConfigurationParser.FLOOR_INIT_PORT);

			FloorSubsystem floorSystem = new FloorSubsystem(numFloors, schedulerSubsystemAddress, floorInitPort);
			floorSystem.startFloorThreads();
		} catch (Exception e) {
			logger.error("", e);
			System.exit(-1);
		}

	}
}
