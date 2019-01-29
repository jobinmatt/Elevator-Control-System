package core.Subsystems.SchedulerSubsystem;

import core.Direction;

/**
 * @author brijpatel
 *
 */
public class Elevator {

	private int elevatorId;
	private int currentFloor;
	private Direction currentDirection;

	public Elevator(int i, int currentFloor, Direction currentDirection) {
		super();
		this.elevatorId = i;
		this.currentFloor = currentFloor;
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

}
