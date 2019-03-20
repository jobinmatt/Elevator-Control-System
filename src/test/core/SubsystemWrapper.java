package test.core;

import core.Subsystems.ElevatorSubsystem.ElevatorSubsystem;
import core.Subsystems.FloorSubsystem.FloorSubsystem;
import core.Subsystems.SchedulerSubsystem.SchedulerSubsystem;

public class SubsystemWrapper {
	
	private SchedulerSubsystem schedulerSubsystem;
	private ElevatorSubsystem elevatorSubsystem;
	private FloorSubsystem floorSubsystem;
	
	public SubsystemWrapper () {
		schedulerSubsystem = null;
		elevatorSubsystem = null;
		floorSubsystem = null;
	}

	public SchedulerSubsystem getS() {
		return schedulerSubsystem;
	}

	public void setS(SchedulerSubsystem s) {
		this.schedulerSubsystem = s;
	}

	public ElevatorSubsystem getE() {
		return elevatorSubsystem;
	}

	public void setE(ElevatorSubsystem e) {
		this.elevatorSubsystem = e;
	}

	public FloorSubsystem getF() {
		return floorSubsystem;
	}

	public void setF(FloorSubsystem f) {
		this.floorSubsystem = f;
	}
	
	

}
