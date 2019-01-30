//****************************************************************************
//
// Filename: FloorSubsystem.java
//
// Description: Floor Subsystem Class
//
// @author Dharina H.
//***************************************************************************

package core.Subsystems.FloorSubsystem;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.InputParser;
import core.LoggingManager;
import core.Exceptions.FloorSubsystemException;
import core.Exceptions.GeneralException;
import core.Exceptions.InputParserException;
import core.Utils.SimulationRequest;


/**
 * The floor subsystem handles the initialization of each floor thread and the events to be simulated.
 */
public class FloorSubsystem {

	private static Logger logger = LogManager.getLogger(FloorSubsystem.class);
	private final String FLOOR_NAME = "Floor";
	private Map<String, FloorThread> floors;
	private List<SimulationRequest> events;
	private int numberOfFloors;
	private InetAddress floorSubsystemAddress;
	private int floorInitPort;
	private Timer sharedTimer;
	
	/**
	 * Creates a floorSubsystem object
	 * 
	 * @param numOfFloors
	 * @throws FloorSubsystemException
	 */
	public FloorSubsystem(int numOfFloors, InetAddress floorSubsystemAddress, int floorInitPort) throws GeneralException {
		
		floors = new HashMap<String, FloorThread>();
		this.numberOfFloors = numOfFloors;
		this.floorSubsystemAddress = floorSubsystemAddress;
		this.floorInitPort = floorInitPort;
		this.sharedTimer = new Timer();
		try {
			readFile();

			//****** ?? send List<SimulationEvent> events to the scheduler ?? ****

			for (int i = 1; i <= numOfFloors; i++ ) { //since a floor will start at 1, i has to be 1
				floors.put(FLOOR_NAME + i,
						new FloorThread(FLOOR_NAME + i, i, floorSubsystemAddress, floorInitPort + i, this.sharedTimer));
			}

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					for (int i = 1; i <= numberOfFloors; i++) {
						if (floors.get(FLOOR_NAME + i) != null) {
							floors.get(FLOOR_NAME + i).terminate();
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
		for(SimulationRequest e: events) {
			floors.get(FLOOR_NAME + e.getFloor()).addEvent(e);
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
