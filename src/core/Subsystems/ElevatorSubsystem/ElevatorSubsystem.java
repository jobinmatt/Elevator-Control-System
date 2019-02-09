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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.LoggingManager;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.ElevatorSubystemException;
import core.Exceptions.HostActionsException;
import core.Utils.HostActions;
import core.Utils.Utils;

public class ElevatorSubsystem {
	
	private final String ELEVATOR_NAME = "ElevatorCar";
	private static Logger logger = LogManager.getLogger(ElevatorSubsystem.class);

	private int numberOfFloors;
	private int numberOfElev;
	private Map<String, ElevatorCarThread> carPool;
	private InetAddress schedulerAddress;

	public ElevatorSubsystem(int numElev, int numFloors, int initPort, InetAddress schedulerAddress) throws ElevatorSubystemException, ConfigurationParserException, UnknownHostException, HostActionsException {

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
	}
	
	/**
	 * Sends a packet to the Scheduler with the port information of each elevator
	 * @param initPort
	 * @throws ElevatorSubystemException
	 * @throws UnknownHostException
	 * @throws HostActionsException
	 */
	public void sendPortsToScheduler(int initPort) throws ElevatorSubystemException, UnknownHostException, HostActionsException {
		byte[] packetData = createPortsArray((HashMap<String, ElevatorCarThread>) carPool);
		DatagramPacket packet = new DatagramPacket(packetData, packetData.length, InetAddress.getLocalHost(), initPort);
	    HostActions.send(packet, Optional.empty());
	}
	
	/**
	 * Creates a dataarray with the port information
	 * @param map
	 * @return
	 */
	private byte[] createPortsArray(HashMap<String, ElevatorCarThread> map) {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		byte SPACER = (byte) 0;
		
		for (Map.Entry<String, ElevatorCarThread> entry : map.entrySet()) {
	        System.out.println(entry.getKey() + ":" + entry.getValue());
	        int elevNumber = entry.getValue().getElevatorNumber();
	        int elevPort = entry.getValue().getPort();
	        data.write(elevNumber);
	        data.write(SPACER);
	        data.write(elevPort);
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
}
