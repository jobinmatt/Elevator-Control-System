//****************************************************************************
//
// Filename: FloorSubsystem.java
//
// Description: Floor Subsystem Class
//
// @author Dharina H.
//***************************************************************************

package core.Subsystems.FloorSubsystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;

import core.Utils.PortParser;
import core.Utils.SubsystemConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.InputParser;
import core.LoggingManager;
import core.Exceptions.ElevatorSubsystemException;
import core.Exceptions.FloorSubsystemException;
import core.Exceptions.GeneralException;
import core.Exceptions.HostActionsException;
import core.Exceptions.InputParserException;
import core.Subsystems.ElevatorSubsystem.ElevatorCarThread;
import core.Utils.HostActions;
import core.Utils.SimulationRequest;

import javax.sound.sampled.Port;


/**
 * The floor subsystem handles the initialization of each floor thread and the events to be simulated.
 */
public class FloorSubsystem {

	private static final int DATA_SIZE = 1024;
	private static Logger logger = LogManager.getLogger(FloorSubsystem.class);
	private final String FLOOR_NAME = "Floor";
	private Map<String, FloorThread> floors;
	private static Map<Integer, Integer> schedulerPorts = new HashMap<>();
	private List<SimulationRequest> events;
	private int numberOfFloors;
	private InetAddress schedulerAddress;
	private int floorInitPort;
	private Timer sharedTimer;
	
	/**
	 * Creates a floorSubsystem object
	 * 
	 * @param numOfFloors
	 * @throws IOException 
	 * @throws FloorSubsystemException
	 */
	public FloorSubsystem(int numOfFloors, InetAddress schedulerAddress, int floorInitPort) throws GeneralException, IOException {
		
		this.floors = new HashMap<String, FloorThread>();
		this.numberOfFloors = numOfFloors;
		this.schedulerAddress = schedulerAddress;
		this.floorInitPort = floorInitPort;
		this.sharedTimer = new Timer();
		try {
			readFile();


			for (int i = 1; i <= numOfFloors; i++ ) { //since a floor will start at 1, i has to be 1
				floors.put(FLOOR_NAME + i,
						new FloorThread(FLOOR_NAME + i, i, schedulerAddress, this.sharedTimer));
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

			PortParser.sendPortsToScheduler(floorInitPort, floors, SubsystemConstants.FLOOR);
			ConfigurationParser configurationParser = ConfigurationParser.getInstance();
			int initSchedulerPort = configurationParser.getInt(ConfigurationParser.SCHEDULER_INIT_PORT);
			FloorSubsystem.schedulerPorts = PortParser.receivePortsFromScheduler(initSchedulerPort + 1);//to avoid conflict with Elevator

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

	public static Map<Integer, Integer> getSchedulerPorts() {
		return schedulerPorts;
	}

}
