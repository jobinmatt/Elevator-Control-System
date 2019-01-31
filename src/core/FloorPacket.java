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

	private byte FLOOR_FLAG = (byte) 0;
	private byte ARRIVAL_SENSOR = (byte) 3;
	private byte SPACER = (byte) 0;

	public final static byte[] UP = {1, 1};
	public final static byte[] DOWN = {1, 2};
	public final static byte[] STATIONARY = {1, 3};


	private int sourceFloor = -1; //THIS IS THE SOURCE FLOOR
	private Direction direction;
	private int carButtonPressed; //DESTINATION FLOOR
	private boolean isValid = true;

	private static boolean elevatorArrived = false; //arrival of an elevator to the floor is by default false

	/**
	 *
	 * @param direction Direction
	 * @param sourceFloor current floor, i.e. source floor when direction is pressed
	 * @param date  The Date
	 * @param carButtonPressed Button pressed in the elevator
	 */
	public FloorPacket(Direction direction, int sourceFloor, int carButtonPressed) {

		this.sourceFloor = sourceFloor;
		this.direction = direction;
		this.carButtonPressed = carButtonPressed;
	}

	/**
	 *
	 * @param data
	 * @param dataLength
	 * @throws CommunicationException
	 */
	public FloorPacket(byte[] data, int dataLength, boolean isArrival) throws CommunicationException {

		isValid = true;

		if(isArrival) { //this is an arrivalSensor packet to be parsed
			if(data[0] != ARRIVAL_SENSOR) {
				isValid = false;
			}
			if(data[1] == 1) {
				this.elevatorArrived = true;
			}
			if(data[2] != SPACER) {
				isValid = false;
			}

			if (data[3] == UP[0] && data[4] == UP[1]) {
				direction = Direction.UP;
			} else if (data[3] == DOWN[0] && data[4] == DOWN[1]) {
				direction = Direction.DOWN;
			} else if (data[3] == STATIONARY[0] && data[4] == STATIONARY[1]) {
				direction = Direction.STATIONARY;
			} else {
				isValid = false;
			}
		}else {
			//format:
			// FLOOR_FLAG Direction Direction SPACER sourceFloor SPACER  carButton SPACER
			//	0			1			2		3			4		5		6        7
			// extract read or write request

			if (data[1] == UP[0] && data[2] == UP[1]) {
				direction = Direction.UP;
			} else if (data[1] == DOWN[0] && data[2] == DOWN[1]) {
				direction = Direction.DOWN;
			} else if (data[1] == STATIONARY[0] && data[2] == STATIONARY[1]) {
				direction = Direction.STATIONARY;
			} else {
				isValid = false;
			}

			int i = 3;
			// must be zero
			if (data[i] != SPACER) {
				isValid = false;
			}

			sourceFloor = data[i++]; // i = 4
			// must be zero
			if (data[i++] != SPACER) { //i = 5
				isValid = false;
			}

			carButtonPressed = data[i++];

			// must be zero at end
			while (i < dataLength) {
				if (data[i++] != SPACER) {
					isValid = false;
					break;
				}
			}
		}

	}

	/**
	 * To be used to send the arrival of an elevator to the floor
	 * @param arrived arrival sensor triggered for arrival
	 * @param direction direction that the elevator is coming from
	 */
	public FloorPacket(boolean arrived, Direction direction) {
		this.elevatorArrived = arrived;
		this.direction = direction;
	}


	public byte[] generateArrivalSensorData() throws CommunicationException, IOException {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		if(this.elevatorArrived) {
			stream.write(ARRIVAL_SENSOR);
			stream.write(1);
			stream.write(SPACER);
		}

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
				throw new CommunicationException("Unable to generate floor packet");
		}

		// add spacer
		stream.write(ARRIVAL_SENSOR);

		return stream.toByteArray();

	}

	public byte[] generatePacketData() throws CommunicationException {

		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			stream.write(FLOOR_FLAG); // floor packet flag

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

			// add spacer
			stream.write(SPACER);

			if (sourceFloor != -1) {
				stream.write(sourceFloor);
			}
			// add spacer
			stream.write(SPACER);

			stream.write(carButtonPressed); //add the button pressed in the elevator

			stream.write(SPACER);

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
		if (sourceFloor == -1) {
			return false;
		}

		return true;
	}

	public int getSourceFloor() {
		return sourceFloor;
	}

	public int getDestinationFloor() {
		return carButtonPressed;
	}

	public boolean getArrivalSensor() {
		return elevatorArrived;
	}

	public Direction getDirection() {

		return direction;
	}

	public String toString() {

		return "Direction: " + direction.name() + " Elevator Location: " + sourceFloor + " Car Button Pressed Number: " + carButtonPressed;
	}
}
