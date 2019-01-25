//****************************************************************************
//
// Filename: FloorPacket.java
//
// Description: Stores packet information, transcodes into byte data and reverse
//
//***************************************************************************
package core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import core.Exceptions.CommunicationException;

public class FloorPacket {

	public final static byte[] UP = {1, 1};
	public final static byte[] DOWN = {1, 2};
	public final static byte[] STATIONARY = {1, 3};
	
	private int currentElevatorFloor = -1;
	private int elevatorNumber = -1;
	private Elevator_Direction direction; 
	private boolean isValid = true;

	public FloorPacket(Elevator_Direction direction, int elevatorFloor, int number) {

		this.currentElevatorFloor = elevatorFloor;
		this.elevatorNumber = number;
		this.direction = direction;
	}

	public FloorPacket(byte[] data, int dataLength) throws CommunicationException {

		isValid = true;

		// extract read or write request
		if (data[1] == UP[0] && data[2] == UP[1]) {
			direction = Elevator_Direction.UP;
		} else if (data[1] == DOWN[0] && data[2] == DOWN[1]) {
			direction = Elevator_Direction.DOWN;
		} else if (data[1] == STATIONARY[0] && data[2] == STATIONARY[1]) {
			direction = Elevator_Direction.STATIONARY;
		} else {
			isValid = false;
		}
		
		int i = 3;
		// must be zero
		if (data[i++] != 0) {
			isValid = false;
		}

		currentElevatorFloor = data[i++];
		// must be zero
		if (data[i++] != 0) {
			isValid = false;
		}
		
		elevatorNumber = data[i++];
		// must be zero at end
		while (i < dataLength) {
			if (data[i++] != 0) {
				isValid = false;
				break;
			}
		}
	}

	public byte[] generatePacketData() throws CommunicationException {

		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			stream.write((byte) 0); // elevator packet flag
			
			// write request bytes
			switch (direction) {
			case UP:
				stream.write(UP);
				break;
			case DOWN:
				stream.write(DOWN);
				break;
			case STATIONARY:
				stream.write(STATIONARY);
				break;
			default:
				throw new CommunicationException("Unable to generate packet");
			}
			
			// add 0
			stream.write(0);

			if (currentElevatorFloor != -1) {
				stream.write(currentElevatorFloor);
			}
			// add 0
			stream.write(0);
			
			if (elevatorNumber != -1) {
				stream.write(elevatorNumber);
			}
			// add 0
			stream.write(0);

			return stream.toByteArray();
		} catch (IOException | NullPointerException e) {
			throw new CommunicationException("Unable to generate packet", e);
		}
	}

	public boolean isValid() {

		if (!isValid) {
			return false;
		}
		if (direction == null) {
			return false;
		}
		if (currentElevatorFloor == -1) {
			return false;
		}
		if (elevatorNumber == -1) {
			return false;
		}

		return true;
	}

	public String toString() {

		return "Direction: " + direction.name() + " Elevator Location: " + currentElevatorFloor + " Elevator Number: " + elevatorNumber; 
	}
}
