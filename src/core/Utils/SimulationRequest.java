//****************************************************************************
//
// Filename: SimulationRequest.java
//
// Description: Holds the information about Simulation events the system needs
//              to cause.
//
//***************************************************************************

package core.Utils;

import core.Exceptions.GeneralException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * @author Brij Patel
 */

public class SimulationRequest implements Comparable<SimulationRequest> {

	private final Date startTime;
	private final int floor;
	private final boolean floorButton;
	private final int carButton;
	private long intervalTime;

	public SimulationRequest(Date startTime, int floor, boolean floorButton, int carButton) {

		super();
		this.startTime = startTime;
		this.floor = floor;
		this.floorButton = floorButton;
		this.carButton = carButton;
	}

	public SimulationRequest(Date startTime, int floor, boolean floorButton, int carButton, long intervalTime) {

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
	public int compareTo(SimulationRequest o) {

		if (getStartTime() == null || o.getStartTime() == null) {
			return 0;
		}
		return getStartTime().compareTo(o.getStartTime());
	}

	@Override
	public String toString() {
		
		if (floorButton) {
			return "Time: " + startTime + " Floor: " + floor + " Direction: UP." + " Destination floor: " + carButton;
		}
		return "Time: " + startTime + " Floor: " + floor + " Direction: DOWN." + " Destination floor: " + carButton;
	}
	
	public byte[] toBytes() throws GeneralException {
		
		////The order:Date startTime, int floor, boolean floorButton, int carButton
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		try {
			data.write(Utils.toByteArray(startTime.toString()));
			data.write(Utils.toByteArray(String.valueOf(floor)));
			data.write(Utils.toByteArray(floorButton));
			data.write(Utils.toByteArray(String.valueOf(carButton)));
		} catch (IOException e) {
		throw new GeneralException(e);
	}
		return data.toByteArray();
	}

}
