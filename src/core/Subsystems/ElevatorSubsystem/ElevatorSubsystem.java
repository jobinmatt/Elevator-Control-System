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
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import core.Exceptions.*;
import core.Utils.PortParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.ConfigurationParser;
import core.LoggingManager;
import core.Utils.HostActions;
import core.Utils.SubsystemConstants;
import core.Utils.Utils;

public class ElevatorSubsystem {
	
	private static final int DATA_SIZE = 1024;
	private final String ELEVATOR_NAME = "ElevatorCar";
	private static Logger logger = LogManager.getLogger(ElevatorSubsystem.class);

	private int numberOfFloors;
	private int numberOfElev;
	private Map<String, ElevatorCarThread> carPool;
	private static Map<Integer, Integer> schedulerPorts = new HashMap<>();
	private InetAddress schedulerAddress;

	public ElevatorSubsystem(int numElev, int numFloors, int initPort, InetAddress schedulerAddress) throws ElevatorSubsystemException, ConfigurationParserException, HostActionsException, IOException {

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

		PortParser.sendPortsToScheduler(initPort,carPool,SubsystemConstants.ELEVATOR);
		ConfigurationParser configurationParser = ConfigurationParser.getInstance();
		int initSchedulerPort = configurationParser.getInt(ConfigurationParser.SCHEDULER_INIT_PORT);
		try {
			schedulerPorts = PortParser.receivePortsFromScheduler(initSchedulerPort);
		} catch (PortParserException e) {
			new ElevatorSubsystemException(e);
		}
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

	public void setSchedulerPorts(Map<Integer, Integer> schedulerPorts) {
		ElevatorSubsystem.schedulerPorts = schedulerPorts;
	}
}
