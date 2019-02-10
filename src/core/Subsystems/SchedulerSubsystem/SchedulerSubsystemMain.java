//****************************************************************************
//
// Filename: SchedulerSubsystemMain.java
//
// Description: Scheduler Subsystem Main Class
//
//***************************************************************************

package core.Subsystems.SchedulerSubsystem;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.LoggingManager;

public class SchedulerSubsystemMain {

	private static Logger logger = LogManager.getLogger(SchedulerSubsystemMain.class);

	public static void main(String[] args) {

		logger.info(LoggingManager.BANNER + "Scheduler Subsystem\n");
		try {
			ConfigurationParser configurationParser = ConfigurationParser.getInstance();

			int numElevators = configurationParser.getInt(ConfigurationParser.NUMBER_OF_ELEVATORS);
			int numFloors = configurationParser.getInt(ConfigurationParser.NUMBER_OF_FLOORS);
			InetAddress elevatorSubsystemAddress = InetAddress.getByName(configurationParser.getString(ConfigurationParser.ELEVATOR_ADDRESS));
			InetAddress floorSubsystemAddress =InetAddress.getByName(configurationParser.getString(ConfigurationParser.FLOOR_ADDRESS));
			int elevatorInitPort = configurationParser.getInt(ConfigurationParser.ELEVATOR_INIT_PORT);
			int floorInitPort = configurationParser.getInt(ConfigurationParser.FLOOR_INIT_PORT);

			//input the right address
			SchedulerSubsystem scheduler = new SchedulerSubsystem(numElevators, numFloors, elevatorSubsystemAddress, floorSubsystemAddress, elevatorInitPort, floorInitPort);
			scheduler.startListeners();
		} catch (Exception e) {
			logger.error("", e);
			System.exit(-1);
		}
		System.exit(0);
	}

}