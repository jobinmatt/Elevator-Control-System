//****************************************************************************
//
// Filename: ElevatorSubsystem.java
//
// Description: This creates an elevator car thread pool, and powers them on.
//              Will also handle elevator cars
//
// @author Shounak Amladi
//***************************************************************************

package core.Subsystems.ElevatorSubsystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.LoggingManager;
import core.Exceptions.CommunicationException;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.ElevatorSubsystemException;
import core.Exceptions.HostActionsException;
import core.Messages.InitMessage;
import core.Utils.HostActions;

public class ElevatorSubsystem {
	
	private final String ELEVATOR_NAME = "ElevatorCar";
	private static Logger logger = LogManager.getLogger(ElevatorSubsystem.class);
	private static final int DATA_SIZE = 1024;
	private final byte SPACER = (byte) 0;
	private int numberOfFloors;
	private int numberOfElev;
	private Map<String, ElevatorCarThread> carPool;
	private static Map<Integer, Integer> schedulerPorts = new HashMap<>();
	private InetAddress schedulerAddress;

	public ElevatorSubsystem(int numElev, int numFloors, int initPort, InetAddress schedulerAddress) throws ElevatorSubsystemException, ConfigurationParserException, HostActionsException, CommunicationException, IOException {

		this.schedulerAddress = schedulerAddress;
		this.numberOfElev = numElev;
		this.numberOfFloors = numFloors;
		this.carPool = new HashMap<String, ElevatorCarThread>();

		String curr_name;
		for (int i = 0; i < this.numberOfElev; i++) {
			curr_name = ELEVATOR_NAME + (i+1);
			this.carPool.put(curr_name, new ElevatorCarThread(curr_name, this.numberOfFloors, this.schedulerAddress));
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for (Map.Entry<String, ElevatorCarThread> car : carPool.entrySet()) {
					if (car != null) {
						car.getValue().terminate();
					}
				}
				LoggingManager.terminate();
			}
		});
		
		sendPortsToScheduler(initPort);
		ConfigurationParser configurationParser = ConfigurationParser.getInstance();
		int initSchedulerPort = configurationParser.getInt(ConfigurationParser.SCHEDULER_INIT_PORT);
		receivePortsFromScheduler(initSchedulerPort);
	}
	
	private void receivePortsFromScheduler(int listenPort) throws ElevatorSubsystemException {
		try {
			DatagramPacket packet = new DatagramPacket(new byte[DATA_SIZE], DATA_SIZE);
			DatagramSocket receiveSocket = new DatagramSocket(listenPort);
			try {
				logger.info("Waiting to receive port information from SCHEDULER...");
				HostActions.receive(packet, receiveSocket);
				receiveSocket.close();
				convertPacketToMap(packet.getData(), packet.getLength());
			} catch (HostActionsException e) {
				throw new ElevatorSubsystemException("Unable to receive scheduler ports packet", e);
			}
		} catch (SocketException e) {
			throw new ElevatorSubsystemException("Unable to create a DatagramSocket", e);
		}
	}

	private void convertPacketToMap(byte[] data, int length) throws ElevatorSubsystemException {
		if(data != null && data[0] != SPACER) {
			
			HashMap<Integer, Integer> tempPorts = new HashMap<>();
			for(int i = 0; i < length; i = i + 8) {
				int pipelineNumber = data[i];
				
				byte[] portNumInByte = {data[i+2], data[i+3], data[i+4], data[i+5]};
				int schedulerPort = ByteBuffer.wrap(portNumInByte).getInt();
				tempPorts.put(pipelineNumber, schedulerPort);
				if(data.length<(i+8) || data[i+8] == SPACER) {
					break;
				}
			}
			ElevatorSubsystem.setSchedulerPorts(tempPorts);
		}
		else throw new ElevatorSubsystemException("Cannot convert null to elevator ports map or invalid data found");
	}

	/**
	 * Sends a packet to the Scheduler with the port information of each elevator
	 * @param initPort
	 * @throws ElevatorSubsystemException
	 * @throws HostActionsException
	 * @throws IOException 
	 * @throws CommunicationException 
	 */
	public void sendPortsToScheduler(int initPort) throws HostActionsException, IOException, CommunicationException {
		byte[] packetData = createPortsArray((HashMap<String, ElevatorCarThread>) carPool);
		DatagramPacket packet = new DatagramPacket(packetData, packetData.length, InetAddress.getLocalHost(), initPort);
	    HostActions.send(packet, Optional.empty());
	}
	
	/**
	 * Creates a data array with the port information
	 * @param map
	 * @return
	 * @throws IOException 
	 * @throws CommunicationException 
	 */
	private byte[] createPortsArray(HashMap<String, ElevatorCarThread> map) throws IOException, CommunicationException {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		data.write(new InitMessage().generatePacketData());
		for (Map.Entry<String, ElevatorCarThread> entry : map.entrySet()) {
	        System.out.println(entry.getKey() + ":" + entry.getValue());
	        int elevNumber = entry.getValue().getElevatorNumber();
	        int elevPort = entry.getValue().getPort();
	        data.write(elevNumber);
	        data.write(SPACER);
	        try {
				data.write(ByteBuffer.allocate(4).putInt(elevPort).array());
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
	 * Starts the thread (powering on elevator)
	 **/
	public void activateElevators() {

		logger.info("Activating Elevators...");
		for (Map.Entry<String, ElevatorCarThread> car : carPool.entrySet()) {
			car.getValue().start();
		}
		logger.log(LoggingManager.getSuccessLevel(), LoggingManager.SUCCESS_MESSAGE);
	}

	public void listen() {
		logger.debug("Listening for requests...");
		while(true) {

		}
	}

	public static Map<Integer, Integer> getSchedulerPorts() {
		return schedulerPorts;
	}

	public static void setSchedulerPorts(Map<Integer, Integer> schedulerPorts) {
		ElevatorSubsystem.schedulerPorts = schedulerPorts;
	}
}
