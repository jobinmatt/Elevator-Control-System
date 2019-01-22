//****************************************************************************
//
// Filename: FloorSubsystem.java
//
// Description: Floor Subsystem Class
//
// @author Dharina H.
//***************************************************************************

package core.Subsystems.FloorSubsystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.InputParser;
import core.LoggingManager;
import core.Exceptions.FloorSubsystemException;
import core.Exceptions.InputParserException;
import core.Utils.SimulationEvent;

/**
 * The floor subsystem handles the initialization of each floor thread and the events to be simulated.
 */
public class FloorSubsystem {
	
	private static Logger logger = LogManager.getLogger(FloorSubsystem.class);

	private Map<Integer, FloorThread> floors;
	private List<SimulationEvent> events;
	private int numberOfFloors;

	/**
	 * Creates a floorSubsystem object
	 * @param filename
	 * @param numOfFloors
	 * @throws FloorSubsystemException 
	 */
	public FloorSubsystem(int numOfFloors) throws FloorSubsystemException {
		
		floors = new HashMap<>();
		this.numberOfFloors = numOfFloors;
		
		try {
			readFile();
			
			//****** send List<SimulationEvent> events to the scheduler ? ****	
			for (int i = 0; i < numOfFloors; i++ ) {
				floors.put(i,new FloorThread());
			}
			
	        Runtime.getRuntime().addShutdownHook(new Thread() {
	            public void run() {
	            	for(int i = 0; i < numOfFloors; i++ ) {
	                    if (floors.get(i) != null) {
	                        floors.get(i).terminate();
	                    }
	            	}
	                LoggingManager.terminate();
	            }
	        });
			
		} catch (InputParserException e) {
			throw new FloorSubsystemException(e);
		}
	}

	/**
	 * Reads the input file to obtain the list of simulation events
	 * @throws InputParserException
	 */
	public void readFile() throws InputParserException {
		
		events = InputParser.parseCVSFile();
	}

	private void addEvents() {
		for(SimulationEvent e: events) {
			floors.get(e.getFloor()).addEvent(e);
		}
	}

	/**
	 * Creates the floor threads.
	 */
	public void startFloorThreads() {
		
		addEvents();
		logger.info("Initializing floor threads.");
		floors.forEach((k,v) -> v.start());
	}

}
