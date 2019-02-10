package core.Subsystems.SchedulerSubsystem;

import core.Direction;

/**
 * @author brijpatel
 *
 */
public class Elevator {

	private int elevatorId;
	private int currentFloor;
	private int destFloor;
	private Direction currentDirection;
	private int numRequests = 0;

	public Elevator(int i, int currentFloor, int destFloor, Direction currentDirection) {
		super();
		this.elevatorId = i;
		this.currentFloor = currentFloor;
		this.destFloor = destFloor;
		this.currentDirection = currentDirection;
	}

	public int getElevatorId() {
		return elevatorId;
	}

	public void setElevatorId(int elevatorId) {
		this.elevatorId = elevatorId;
	}

	public int getCurrentFloor() {
		return currentFloor;
	}

	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}

	public Direction getCurrentDirection() {
		return currentDirection;
	}

	public void setCurrentDirection(Direction currentDirection) {
		this.currentDirection = currentDirection;
	}

	public int getDestFloor() {
		return destFloor;
	}
	public void setDestFloor(int destFloor) {
		this.destFloor = destFloor;
	}

	public int getNumRequests() {
		return this.numRequests;
	}
	
	public void setNumRequests(int requests) {
		numRequests = requests;
	}
	
	@Override
	public String toString() {
		return "Elevator " + elevatorId + " current floor: " + currentFloor + " destination floor: " + destFloor
				+ " current direction: " + currentDirection.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Elevator && o != null) {
			if (((Elevator) o).getElevatorId() == this.getElevatorId()) {
				return true;
			}
		}
		return false;
	}

}
