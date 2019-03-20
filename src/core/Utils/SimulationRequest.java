//****************************************************************************
//
// Filename: SimulationRequest.java
//
// Description: Holds the information about Simulation events the system needs
//              to cause.
//
//***************************************************************************

package core.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import core.Direction;
import core.Exceptions.GeneralException;

/**
 * @author Brij Patel
 */

public class SimulationRequest implements Comparable<SimulationRequest> {

	private static final String END = "End";
	
	private Date startTime;
	private int floor;
	private Direction floorButton;
	private int carButton;
	private long intervalTime;
	private int errorCode;
	private int errorElevator;
	private boolean end;

	public SimulationRequest(boolean end) {

		super();
		this.end = true;
	}
	
	public SimulationRequest(Date startTime, int floor, Direction floorButton, int carButton, int errorCode, int errorFloor) {

		super();
		this.startTime = startTime;
		this.floor = floor;
		this.floorButton = floorButton;
		this.carButton = carButton;
		this.errorCode = errorCode;
		this.errorElevator = errorFloor;
	}

	public SimulationRequest(Date startTime, int floor, Direction floorButton, int carButton, long intervalTime, int errorCode, int errorFloor) {

		super();
		this.startTime = startTime;
		this.floor = floor;
		this.floorButton = floorButton;
		this.carButton = carButton;
		this.intervalTime = intervalTime;
		this.errorCode = errorCode;
		this.errorElevator = errorFloor;
	}

	public void setStartTime(Date time) {
		
		this.startTime = time; 
	}
	
	public Date getStartTime() {

		return startTime;
	}

	public int getFloor() {

		return floor;
	}

	public Direction getFloorButton() {

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

	public int getErrorCode() {
		return this.errorCode;
	}

	public int getErrorElevator() {
		return this.errorElevator;
	}
	
	public boolean getEnd() {
		return this.end;
	}

	@Override
	public int compareTo(SimulationRequest o) {

		if (end) {
			return -1;
		}
		
		if (getStartTime() == null || o.getStartTime() == null) {
			return 0;
		}
		return getStartTime().compareTo(o.getStartTime());
	}

	@Override
	public String toString() {
		
		if (end) {
			return "end";
		}
		return "Time: " + startTime + " Floor: " + floor + " Direction: " + floorButton.toString()
		+ " Destination floor: " + carButton;
	}

	public byte[] toBytes() throws GeneralException {

		////The order:Date startTime, int floor, boolean floorButton, int carButton
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		try {
			
			if (end) {
				data.write(Utils.toByteArray(END));
				return data.toByteArray();
			}
			data.write(Utils.toByteArray(startTime.toString()));
			data.write(Utils.toByteArray(String.valueOf(floor)));
			data.write(Utils.toByteArray(floorButton.toString()));
			data.write(Utils.toByteArray(String.valueOf(carButton)));
		} catch (IOException e) {
			throw new GeneralException(e);
		}
		return data.toByteArray();
	}

}
