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
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.InputParser;
import core.LoggingManager;
import core.Exceptions.CommunicationException;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.FloorSubsystemException;
import core.Exceptions.GeneralException;
import core.Exceptions.HostActionsException;
import core.Exceptions.InputParserException;
import core.Messages.InitMessage;
import core.Utils.HostActions;
import core.Utils.SimulationRequest;

/**
 * The floor subsystem handles the initialization of each floor thread and the
 * events to be simulated.
 */
public class FloorSubsystem {

	private static Logger logger = LogManager.getLogger(FloorSubsystem.class);
	private final String FLOOR_NAME = "Floor";
	private final byte SPACER = (byte) 0;
	private static final int DATA_SIZE = 1024;
	private final int PORT_TIMEOUT = 10000;
	private static Map<Integer, Integer> schedulerPorts = new HashMap<>();
	private Map<String, FloorThread> floors;
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
	public FloorSubsystem(int numOfFloors, InetAddress schedulerAddress, int floorInitPort, int numOfElevators) throws GeneralException, IOException {

		this.floors = new HashMap<String, FloorThread>();
		this.numberOfFloors = numOfFloors;
		this.setSchedulerAddress(schedulerAddress);
		this.setFloorInitPort(floorInitPort);
		this.sharedTimer = new Timer();
		try {
			readFile();

			// ****** ?? send List<SimulationEvent> events to the scheduler ?? ****

			for (int i = 1; i <= numOfFloors; i++) { // since a floor will start at 1, i has to be 1
				FloorType floorType;
				if (i == 1) {
					floorType = FloorType.BOTTOM;
				} else if (i == numOfFloors) {
					floorType = FloorType.TOP;
				} else {
					floorType = FloorType.NORMAL;
				}
				floors.put(FLOOR_NAME + i, new FloorThread(FLOOR_NAME + i, i, schedulerAddress, this.sharedTimer, numOfElevators, floorType));
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

	public void shutdown() {

		for (int i = 1; i <= numberOfFloors; i++) {
			if (floors.get(FLOOR_NAME + i) != null) {
				floors.get(FLOOR_NAME + i).terminate();
			}
		}
		LoggingManager.terminate();

	}

	private void convertPacketToMap(byte[] data, int length) throws FloorSubsystemException {
		if (data != null && data[0] != SPACER) {
			HashMap<Integer, Integer> tempPorts = new HashMap<>();
			for (int i = 0; i < length; i = i + 8) {
				int pipelineNumber = data[i];
				byte[] portNumInByte = { data[i + 2], data[i + 3], data[i + 4], data[i + 5] };
				int schedulerPort = ByteBuffer.wrap(portNumInByte).getInt();
				tempPorts.put(pipelineNumber, schedulerPort);
				if (data.length < (i + 8) || data[i + 8] == SPACER) {
					break;
				}
			}
			this.setSchedulerPorts(tempPorts);
		} else
			throw new FloorSubsystemException("Cannot convert null to elevator ports map or invalid data found");
	}

	/**
	 * Sends a packet to the Scheduler with the port information of each elevator
	 * 
	 * @param initPort
	 * @throws HostActionsException
	 * @throws IOException
	 * @throws CommunicationException
	 */
	public void sendPortsToScheduler(int initPort) throws HostActionsException, IOException, CommunicationException {

		boolean received = false;

		try {
			ConfigurationParser configurationParser = ConfigurationParser.getInstance();
			int initSchedulerPort = configurationParser.getInt(ConfigurationParser.SCHEDULER_INIT_PORT);

			while (!received) {

				byte[] packetData = createPortsArray((HashMap<String, FloorThread>) floors);
				DatagramPacket packet = new DatagramPacket(packetData, packetData.length, schedulerAddress, initPort);
				HostActions.send(packet, Optional.empty());

				DatagramPacket recievePacket = new DatagramPacket(new byte[DATA_SIZE], DATA_SIZE);
				DatagramSocket receiveSocket = new DatagramSocket(initSchedulerPort + 1);
				logger.info("Waiting to receive port information from SCHEDULER...");
				receiveSocket.setSoTimeout(PORT_TIMEOUT);
				receiveSocket.receive(recievePacket);
				received = true;
				receiveSocket.close();
				convertPacketToMap(recievePacket.getData(), recievePacket.getLength());
			}
		} catch (ConfigurationParserException e) {
			logger.info("Unable to get Configuration Parser");
		} catch (FloorSubsystemException fse) {

		} catch (SocketTimeoutException ste) {
			sendPortsToScheduler(floorInitPort);
		}
	}

	/**
	 * Creates a data array with the port information
	 * 
	 * @param map
	 * @return
	 * @throws IOException
	 * @throws CommunicationException
	 */
	private byte[] createPortsArray(HashMap<String, FloorThread> map) throws IOException, CommunicationException {

		ByteArrayOutputStream data = new ByteArrayOutputStream();
		data.write(new InitMessage().generatePacketData());
		for (Map.Entry<String, FloorThread> entry : map.entrySet()) {
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
		return data.toByteArray();
	}

	/**
	 * Reads the input file to obtain the list of simulation events
	 * 
	 * @throws InputParserException
	 */
	public void readFile() throws InputParserException {

		events = InputParser.parseCVSFile();
	}

	private void addEvents() {

		for (SimulationRequest e : events) {

			if (e.getEnd()) {
				e.setStartTime(events.get(events.size() - 2).getStartTime());
				floors.get(FLOOR_NAME + 1).addEvent(e);
			} else {
				floors.get(FLOOR_NAME + e.getFloor()).addEvent(e);
			}
		}
	}

	/**
	 * Creates the floor threads.
	 */
	public void startFloorThreads() {

		addEvents();
		logger.info("Initializing floor threads.");
		floors.forEach((k, v) -> v.start());
	}

	public static Map<Integer, Integer> getSchedulerPorts() {
		return schedulerPorts;
	}

	public void setSchedulerPorts(Map<Integer, Integer> schedulerPorts) {
		FloorSubsystem.schedulerPorts = schedulerPorts;
	}

	public InetAddress getSchedulerAddress() {
		return schedulerAddress;
	}

	public void setSchedulerAddress(InetAddress schedulerAddress) {
		this.schedulerAddress = schedulerAddress;
	}

	public int getFloorInitPort() {
		return floorInitPort;
	}

	public Map<String, FloorThread> getFloorsMap() {
		return this.floors;
	}

	public void setFloorInitPort(int floorInitPort) {
		this.floorInitPort = floorInitPort;
	}

	public FloorThread getFirstFloor() {
		return floors.get(FLOOR_NAME + "1");
	}

	public FloorThread getFloor(int i) {
		return floors.get(FLOOR_NAME + i);
	}
}
