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
import java.util.Comparator;

import javax.sound.sampled.Port;

import core.Direction;
import core.Utils.SubsystemConstants;

/**
 *
 * This creates a SchedulerEvent based on a DatagramPacket
 * @author Jobin Mathew
 * */
public class SchedulerRequest implements Comparable<SchedulerRequest>{
	private SubsystemConstants type;
	private int currentFloor = -1;
	private InetAddress receivedAddress;
	private int receivedPort;
	private SchedulerPriorityConstants priority;
	private int destFloor = -1;
	private Direction requestDirection;
	private int elevatorNumber = -1;
	private int carButton = -1;

	public SchedulerRequest(DatagramPacket packet) {
		receivedPort = packet.getPort();
		receivedAddress = packet.getAddress();
		// use packet.getData()
	}

	public SchedulerRequest() {
	}
	
	@Override
	public String toString() {
		return "Type: " + type.toString() + " current floor: " + currentFloor + "receieved address: " + receivedAddress.getHostAddress() + ":" + receivedPort + 
				"destination floor: " + destFloor + " request direction: " + requestDirection.toString() + " elevator number: " + elevatorNumber + " car button: " + carButton;
	}

	public SchedulerRequest(InetAddress receivedAddress, int receivedPort, SubsystemConstants type, int typeNumber,
			SchedulerPriorityConstants priority, Direction requestDirection, int destFloor, int carButton) {// Floor
		this.receivedAddress = receivedAddress;
		this.receivedPort = receivedPort;
		this.type = type;
		this.currentFloor = typeNumber;
		this.priority = priority;
		this.destFloor = Integer.MIN_VALUE;
		this.requestDirection = requestDirection;
		this.elevatorNumber = -1;
		this.destFloor = destFloor;
		this.carButton = carButton;
	}

	public SchedulerRequest(InetAddress receivedAddress, int receivedPort, SubsystemConstants type, int typeNumber,
			SchedulerPriorityConstants priority, Direction requestDirection, int destFloor, int elevNumber, int carButton) {// Elev
		this.receivedAddress = receivedAddress;
		this.receivedPort = receivedPort;
		this.type = type;
		this.currentFloor = typeNumber;
		this.priority = priority;
		this.destFloor = destFloor;
		this.requestDirection = requestDirection;
		this.elevatorNumber = elevNumber;
		this.carButton = carButton;
	}

	/**
	 * Gets the type number
	 * @param
	 * @return int
	 */
	public int getCurrentFloor() {
		return currentFloor;
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

	public int getElevatorNumber() {
		return elevatorNumber;
	}


	public void setType(SubsystemConstants type) {
		this.type = type;
	}

	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}

	public void setReceivedAddress(InetAddress receivedAddress) {
		this.receivedAddress = receivedAddress;
	}

	public void setReceivedPort(int receivedPort) {
		this.receivedPort = receivedPort;
	}

	public void setPriority(SchedulerPriorityConstants priority) {
		this.priority = priority;
	}

	public void setDestFloor(int destFloor) {
		this.destFloor = destFloor;
	}

	public void setRequestDirection(Direction requestDirection) {
		this.requestDirection = requestDirection;
	}

	public void setElevatorNumber(int elevatorNumber) {
		this.elevatorNumber = elevatorNumber;
	}

	public int getCarButton() {
		return carButton;
	}

	public void setCarButton(int carButton) {
		this.carButton = carButton;
	}

	@Override
	public int compareTo(SchedulerRequest arg1) {
		if(getDestFloor() > arg1.getDestFloor()) {
			return 1;
		}
		else if(getDestFloor() == arg1.getDestFloor()) {
			return 0;
		}
		return -1;
	}
	
	static final Comparator<SchedulerRequest> BY_ASCENDING = new Comparator<SchedulerRequest>() {

		@Override
		public int compare(SchedulerRequest arg0, SchedulerRequest arg1) {
			if(arg0.getDestFloor() > arg1.getDestFloor() && arg0.getRequestDirection().compareTo(arg1.getRequestDirection()) == 0) {
				return 1;
			}
			else if(arg0.getDestFloor() == arg1.getDestFloor()) {
				return 0;
			}
			return -1;
		}
	};
}
