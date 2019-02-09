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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.InputParser;
import core.LoggingManager;
import core.Exceptions.ElevatorSubystemException;
import core.Exceptions.FloorSubsystemException;
import core.Exceptions.GeneralException;
import core.Exceptions.HostActionsException;
import core.Exceptions.InputParserException;
import core.Subsystems.ElevatorSubsystem.ElevatorCarThread;
import core.Utils.HostActions;
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
	 * @throws IOException 
	 * @throws FloorSubsystemException
	 */
	public FloorSubsystem(int numOfFloors, InetAddress floorSubsystemAddress, int floorInitPort) throws GeneralException, IOException {
		
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
						new FloorThread(FLOOR_NAME + i, i, floorSubsystemAddress, this.sharedTimer));
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
			
			sendPortsToScheduler(floorInitPort);

		} catch (InputParserException e) {
			throw new FloorSubsystemException(e);
		}
	}
	
	/**
	 * Sends a packet to the Scheduler with the port information of each elevator
	 * @param initPort
	 * @throws HostActionsException
	 * @throws IOException 
	 */
	public void sendPortsToScheduler(int initPort) throws HostActionsException, IOException {
		byte[] packetData = createPortsArray((HashMap<String, FloorThread>) floors);
		DatagramPacket packet = new DatagramPacket(packetData, packetData.length, InetAddress.getLocalHost(), initPort);
	    HostActions.send(packet, Optional.empty());
	}
	
	/**
	 * Creates a dataarray with the port information
	 * @param map
	 * @return
	 * @throws IOException 
	 */
	private byte[] createPortsArray(HashMap<String, FloorThread> map) throws IOException {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		byte SPACER = (byte) 0;
        
		for (Map.Entry<String, FloorThread> entry : map.entrySet()) {
	        System.out.println(entry.getKey() + ":" + entry.getValue());
	        int floorNumber = entry.getValue().getFloorNumber();
	        int floorPort = entry.getValue().getPort();
	        data.write(floorNumber);
	        data.write(SPACER);
	        try {
				data.write(ByteBuffer.allocate(4).putInt(floorPort).array());
			} catch (IOException e) {
				throw new IOException("" + e);
			}
	        data.write(SPACER);
	        data.write(SPACER);
	    }
	    data.write(SPACER);
	    data.write(SPACER);
		return data.toByteArray();
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
