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
			//InetAddress elevatorSubsystemAddress = configurationParser.getAddress(TypeConstants.ELEVATOR);
			//InetAddress floorSubsystemAddress = configurationParser.getAddress(TypeConstants.FLOOR);

			//input the right address
			SchedulerSubsystem scheduler = new SchedulerSubsystem(numElevators, numFloors, InetAddress.getLocalHost(), InetAddress.getLocalHost());
			scheduler.startListeners();
			scheduler.startScheduling();
		} catch (Exception e) {
			logger.error("", e);
			System.exit(-1);
		}
		System.exit(0);
	}

}