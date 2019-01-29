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

import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.LoggingManager;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.ElevatorSubystemException;

public class ElevatorSubsystem {
	private final String ELEVATOR_NAME = "ElevatorCar";
	private static Logger logger = LogManager.getLogger(ElevatorSubsystem.class);

	private int numberOfFloors;
	private int numberOfElev;
	private Map<String, ElevatorCarThread> carPool;
	private InetAddress schedulerAddress;

	public ElevatorSubsystem(int numElev, int numFloors, int initPort, InetAddress schedulerAddress)
			throws ElevatorSubystemException, ConfigurationParserException {

		this.schedulerAddress = schedulerAddress;
		this.numberOfElev = numElev;
		this.numberOfFloors = numFloors;
		this.carPool = new HashMap<String, ElevatorCarThread>();

		try {
			String curr_name;
			for (int i=0; i< this.numberOfElev;i++) {
				curr_name = ELEVATOR_NAME+(i+1);
				this.carPool.put(curr_name, new ElevatorCarThread(curr_name, this.numberOfFloors, initPort+(i+1), this.schedulerAddress));
			}
		} catch (SocketException e) {
			throw new ElevatorSubystemException(e);
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
