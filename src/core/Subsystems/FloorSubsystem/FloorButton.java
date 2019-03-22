package core.Subsystems.FloorSubsystem;

import core.Direction;

public class FloorButton {
	private boolean isPressed = false;
	private Direction direction;
	
	public FloorButton(Direction direction) {
		this.direction = direction;
	}
	
	public void setButtonPressed() {
		this.isPressed = true;
	}
	
	public void setButtonNotPressed() {
		this.isPressed = false;
	}
	
	public boolean getStatus() {
		return this.isPressed;
	}

	public Direction getDirection() {
		return direction;
	}
}
