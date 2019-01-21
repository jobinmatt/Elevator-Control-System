package core.Subsystems.FloorSubsystem;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.Exceptions.InputParserException;
import core.Utils.InputParser;
import core.Utils.SimulationEvent;

/**
 * The floor subsystem handles the initialization of each floor thread and the events to be simulated.
 * @author Dharina H.
 */
public class FloorSubsystem {


	private static Logger logger = LogManager.getLogger(FloorSubsystem.class);

	private Map<Integer, FloorThread> floors;
	private List<SimulationEvent> events;
	private int numOfFloors;

	/**
	 * Creates a floorSubsystem obj
	 * @param filename
	 * @param numOfFloors
	 */
	public FloorSubsystem(int numOfFloors) {
		floors = new HashMap<>();
		this.numOfFloors = numOfFloors;
	}

	/**
	 * Reads the input file to obtain the list of simulation events
	 * @throws InputParserException
	 */
	public void readFile() throws InputParserException {
		events = InputParser.parseCVSFile();
	}

	private void createFloorThreads() throws SocketException {

		for(int i = 1; i <= numOfFloors; i++ ) {
			floors.put(i,new FloorThread());
		}
	}

	private void addEvents() {
		for(SimulationEvent e: events) {
			floors.get(e.getFloor()).addEvent(e);
		}
	}

	/**
	 * Creates the floor threads.
	 * @throws SocketException
	 */
	public void startFloorThreads() throws SocketException {
		createFloorThreads();
		addEvents();
		logger.info("Initializing floor threads.");
		floors.forEach((k,v) -> v.start());
	}

}
