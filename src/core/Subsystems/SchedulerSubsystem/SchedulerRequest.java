//****************************************************************************
//
// Filename: SchedulerRequest.java
//
// Description: SchedulerRequest that can be used by the scheduler
//
//***************************************************************************
package core.Subsystems.SchedulerSubsystem;

import java.net.DatagramPacket;
import java.net.InetAddress;

import core.Direction;
import core.Utils.SubsystemConstants;

/**
 *
 * This creates a SchedulerEvent based on a DatagramPacket
 * @author Jobin Mathew
 * */
public class SchedulerRequest implements Comparable<SchedulerRequest>{
	private SubsystemConstants type;
	private int typeNumber = -1; // floor num/ elev num
	private InetAddress receivedAddress;
	private int receivedPort;
	private SchedulerPriorityConstants priority;
	private int destFloor;
	private Direction requestDirection;

	public SchedulerRequest(DatagramPacket packet) {
		receivedPort = packet.getPort();
		receivedAddress = packet.getAddress();
		// use packet.getData()
	}

	public SchedulerRequest() {
	}

	public SchedulerRequest(InetAddress receivedAddress, int receivedPort, SubsystemConstants type, int typeNumber,
			SchedulerPriorityConstants priority, Direction requestDirection) {// Floor
		this.receivedAddress = receivedAddress;
		this.receivedPort = receivedPort;
		this.type = type;
		this.typeNumber = typeNumber;
		this.priority = priority;
		this.destFloor = Integer.MIN_VALUE;
		this.requestDirection = requestDirection;
	}

	public SchedulerRequest(InetAddress receivedAddress, int receivedPort, SubsystemConstants type, int typeNumber,
			SchedulerPriorityConstants priority, Direction requestDirection, int destFloor) {// Elev
		this.receivedAddress = receivedAddress;
		this.receivedPort = receivedPort;
		this.type = type;
		this.typeNumber = typeNumber;
		this.priority = priority;
		this.destFloor = destFloor;
		this.requestDirection = requestDirection;
	}

	/**
	 * Gets the type number
	 * @param
	 * @return int
	 */
	public int getTypeNumber() {
		return typeNumber;
	}

	/**
	 * Gets the received port number
	 * @param
	 * @return int
	 */
	public int getReceivedPort() {
		return receivedPort;
	}

	/**
	 * Gets the received address
	 * @param
	 * @return InetAddress
	 */
	public InetAddress getReceivedAddress() {
		return receivedAddress;
	}

	/**
	 * Gets the simulationEvent object
	 * @param
	 * @return SimulationRequest
	 */
	public SchedulerPriorityConstants getPriority() {
		return priority;
	}

	/**
	 * Gets the type of the request
	 * @param
	 * @return TypeConstants
	 */
	public SubsystemConstants getType() {
		return type;
	}

	public int getDestFloor() {
		return destFloor;
	}

	public Direction getRequestDirection() {
		return requestDirection;
	}

	@Override
	public int compareTo(SchedulerRequest o) {
		return this.priority.compareTo(o.getPriority());
	}
}
