//****************************************************************************
//
// Filename: ElevatorSubsystemMain.java
//
// Description: Elevator Subsystem Main Class
//
//***************************************************************************

package core.Subsystems.ElevatorSubsystem;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.LoggingManager;
import core.Subsystems.ElevatorSubsystem.ElevatorSubsystem;


public class ElevatorSubsystemMain {

	private static Logger logger = LogManager.getLogger(ElevatorSubsystemMain.class);
	
	public static void main(String[] args) {
		
		logger.info(LoggingManager.BANNER + "Elevator Subsystem\n");
		
		try {
			ConfigurationParser configurationParser = ConfigurationParser.getInstance();
			
			int numElev = configurationParser.getInt(ConfigurationParser.NUMBER_OF_ELEVATORS);
			int numFloors = configurationParser.getInt(ConfigurationParser.NUMBER_OF_FLOORS);
			int initElevatorPort = configurationParser.getInt(ConfigurationParser.ELEVATOR_INIT_PORT);
			InetAddress schedulerAddress = InetAddress.getByName(configurationParser.getString(ConfigurationParser.SCHEDULER_ADDRESS));
			ElevatorSubsystem elevatorSystem = new ElevatorSubsystem(numElev, numFloors, initElevatorPort, schedulerAddress);
			elevatorSystem.activateElevators();		
			
			elevatorSystem.listen(); 
		} catch (Exception e) {
			logger.error("", e);
            System.exit(-1);
		}
		
//		System.exit(0);
		
	}

}
