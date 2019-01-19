//****************************************************************************
//
// Filename: SimulationEvent.java
//
// Description: Holds the information about Simulation events the system needs
//              to cause.
//
//***************************************************************************
package Utils;

import java.util.Date;

public class SimulationEvent implements Comparable<SimulationEvent> {

	private final Date startTime;
	private final int floor;
	private final boolean floorButton;
	private final int carButton;
	private long intervalTime;

	public SimulationEvent(Date startTime, int floor, boolean floorButton, int carButton) {
		super();
		this.startTime = startTime;
		this.floor = floor;
		this.floorButton = floorButton;
		this.carButton = carButton;
	}

	public SimulationEvent(Date startTime, int floor, boolean floorButton, int carButton, long intervalTime) {
		super();
		this.startTime = startTime;
		this.floor = floor;
		this.floorButton = floorButton;
		this.carButton = carButton;
		this.intervalTime = intervalTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public int getFloor() {
		return floor;
	}

	public boolean getFloorButton() {
		return floorButton;
	}

	public int getCarButton() {
		return carButton;
	}

	public void setIntervalTime(long i) {
		this.intervalTime = i;
	}

	public long getIntervalTime() {
		return this.intervalTime;
	}

	@Override
	public int compareTo(SimulationEvent o) {
		if (getStartTime() == null || o.getStartTime() == null)
			return 0;
		return getStartTime().compareTo(o.getStartTime());
	}


}
