//****************************************************************************
//
// Filename: SchedulerSubsystemMain.java
//
// Description: Scheduler Subsystem Main Class
//
//***************************************************************************

package core.Subsystems.SchedulerSubsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.LoggingManager;
import core.Utils.SubsystemConstants;

public class SchedulerSubsystemMain {

	private static Logger logger = LogManager.getLogger(SchedulerSubsystemMain.class);
	
	public static void main(String[] args) {

		logger.info(LoggingManager.BANNER + "Scheduler Subsystem\n");
		try {
			ConfigurationParser configurationParser = ConfigurationParser.getInstance();

			int numElevators = configurationParser.getInt(ConfigurationParser.NUMBER_OF_ELEVATORS);
			int numFloors = configurationParser.getInt(ConfigurationParser.NUMBER_OF_FLOORS);
			int elevatorInitPort = configurationParser.getInt(ConfigurationParser.ELEVATOR_INIT_PORT);
			int floorInitPort = configurationParser.getInt(ConfigurationParser.FLOOR_INIT_PORT);

			SchedulerSubsystem scheduler = new SchedulerSubsystem(numElevators);
			
			ElevatorPipeline[] elevatorListeners = new ElevatorPipeline[numElevators];
			FloorPipeline[] floorListeners = new FloorPipeline[numFloors];

			for (int i = 0; i < numElevators; i++) {
				elevatorListeners[i] = new ElevatorPipeline(SubsystemConstants.ELEVATOR, i+1, scheduler);
			}
			for (int i = 0; i < numFloors; i++) {
				floorListeners[i] = new FloorPipeline(SubsystemConstants.FLOOR, i+1, scheduler);
			}
			scheduler.addListeners(elevatorListeners, floorListeners);
			scheduler.start(elevatorInitPort, floorInitPort);
			
			
			logger.info("Starting listeners...");
			for (int i = 0; i < elevatorListeners.length; i++) {
				elevatorListeners[i].start();
				Thread.sleep(100);
			}

			for (int i = 0; i < floorListeners.length; i++) {
				floorListeners[i].start();
				Thread.sleep(100);
			}
			logger.log(LoggingManager.getSuccessLevel(), LoggingManager.SUCCESS_MESSAGE);
			
			
			ShutdownThread shutdownThread = new ShutdownThread(scheduler, elevatorListeners, floorListeners);
			shutdownThread.run();
		
		} catch (Exception e) {
			logger.error("", e);
			System.exit(-1);
		}
	}
}