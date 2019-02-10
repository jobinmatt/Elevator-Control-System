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

import core.Direction;
import core.Messages.ElevatorMessage;
import core.Messages.FloorMessage;
import core.Messages.SubsystemMessage;
import core.Utils.SubsystemConstants;

/**
 *
 * This creates a SchedulerEvent based on a DatagramPacket
 * @author Jobin Mathew
 * Refactored by: Shounak Amladi
 * */
public class SchedulerRequest implements Comparable<SchedulerRequest>{
	private SubsystemConstants type;
	private int sourceFloor = -1;
	private InetAddress receivedAddress;
	private int receivedPort;
	private long requestId;
	private int destFloor = -1;
	private Direction requestDirection;
	private int elevatorNumber = -1;
	private int targetFloor = -1;

	public SchedulerRequest(DatagramPacket packet) {
		receivedPort = packet.getPort();
		receivedAddress = packet.getAddress();
		// use packet.getData()
	}

	public SchedulerRequest() {
	}

	@Override
	public String toString() {
		return "Request id: " + requestId + " Type: " + type.toString() + " source floor: " + sourceFloor
				+ " receieved address: " + receivedAddress.getHostAddress() + ":" + receivedPort
				+ 
				" destination floor: " + destFloor + " request direction: " + requestDirection.toString()
				+ " elevator number: " + elevatorNumber + " car button: " + targetFloor;
	}
	//for floor the source floor is where the initial request came from
	public SchedulerRequest(InetAddress receivedAddress, int receivedPort, SubsystemConstants type,
			int sourceFloor, Direction requestDirection, int destFloor, int carButton) {// Floor
		this.receivedAddress = receivedAddress;
		this.receivedPort = receivedPort;
		this.type = type;
		this.sourceFloor = sourceFloor;
		this.requestId = System.currentTimeMillis();
		this.destFloor = Integer.MIN_VALUE;
		this.requestDirection = requestDirection;
		this.elevatorNumber = -1;
		this.destFloor = destFloor;
		this.targetFloor = carButton;
	}
	//for elevator its the current floor is the source floor 
	public SchedulerRequest(InetAddress receivedAddress, int receivedPort, SubsystemConstants type,
			int currentFloor, Direction requestDirection, int destFloor, int elevNumber, int targetFloor) {// Elev
		this.receivedAddress = receivedAddress;
		this.receivedPort = receivedPort;
		this.type = type;
		this.sourceFloor = currentFloor;
		this.requestId = System.currentTimeMillis() / 1000L;
		this.destFloor = destFloor; //next floor to visit
		this.requestDirection = requestDirection;
		this.elevatorNumber = elevNumber;
		this.targetFloor = targetFloor; //final destination 
	}

	/**
	 * Gets the type number
	 * @param
	 * @return int
	 */
	public int getSourceFloor() {
		return sourceFloor;
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
	public long getrRequestId() {
		return requestId;
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

	public void setSourceFloor(int sourceFloor) {
		this.sourceFloor = sourceFloor;
	}

	public void setReceivedAddress(InetAddress receivedAddress) {
		this.receivedAddress = receivedAddress;
	}

	public void setReceivedPort(int receivedPort) {
		this.receivedPort = receivedPort;
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

	public int getTargetFloor() {
		return targetFloor;
	}

	public void setTargetFloor(int carButton) {
		this.targetFloor = carButton;
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
			if (arg0.getSourceFloor() < arg1.getSourceFloor()) {
				return 1;
			}
			else if(arg0.getSourceFloor() == arg1.getSourceFloor()) {
				return 0;
			}
			return -1;
		}
	};
	
	static final Comparator<SchedulerRequest> BY_DECENDING = new Comparator<SchedulerRequest>() {

		@Override
		public int compare(SchedulerRequest arg0, SchedulerRequest arg1) {
			if (arg0.getSourceFloor() > arg1.getSourceFloor()) {
				return 1;
			}
			else if(arg0.getSourceFloor() == arg1.getSourceFloor()) {
				return 0;
			}
			return -1;
		}
	};

	@Override
	public boolean equals(Object o) {
		if(o instanceof SchedulerRequest && o != null) {
			if (((SchedulerRequest) o).getElevatorNumber() == this.elevatorNumber) {
				if (((SchedulerRequest) o).getDestFloor() == this.destFloor) {
					if (((SchedulerRequest) o).getSourceFloor() == this.sourceFloor) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public SubsystemMessage toFloorPacket() {
		return new FloorMessage(this.requestDirection,this.sourceFloor, this.targetFloor );
	}
	
	public SubsystemMessage toElevatorPacket() {
		return new ElevatorMessage(this.sourceFloor, this.destFloor, this.targetFloor);
	}
}
