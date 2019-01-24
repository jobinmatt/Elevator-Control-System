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

import core.Utils.SimulationRequest;
import core.Utils.TypeConstants;

/**
 *
 * This creates a SchedulerEvent based on a DatagramPacket
 * @author Jobin Mathew
 * */
public class SchedulerRequest implements Comparable<SchedulerRequest>{
	private SimulationRequest simulationEvent;
	private TypeConstants type;
	private int typeNumber = -1;
	private InetAddress receivedAddress;
	private int receivedPort;
	private SchedulerPriorityConstants priority;

	public SchedulerRequest(DatagramPacket packet) {
		receivedPort = packet.getPort();
		receivedAddress = packet.getAddress();
		// use packet.getData()
	}

	public SchedulerRequest() {
	}

	public SchedulerRequest(SimulationRequest simulationEvent, InetAddress receivedAddress, int receivedPort, TypeConstants type, int typeNumber, SchedulerPriorityConstants priority) {
		this.simulationEvent = simulationEvent;
		this.receivedAddress = receivedAddress;
		this.receivedPort = receivedPort;
		this.type = type;
		this.typeNumber = typeNumber;
		this.priority = priority;
	}

	/**
	 * Gets the simulationEvent object
	 * @param
	 * @return SimulationRequest
	 */
	public SimulationRequest getSimulationEvent() {
		return simulationEvent;
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
	public TypeConstants getType() {
		return type;
	}

	@Override
	public int compareTo(SchedulerRequest o) {
		return this.priority.compareTo(o.getPriority());
	}
}
