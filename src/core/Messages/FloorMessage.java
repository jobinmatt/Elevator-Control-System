//****************************************************************************
//
// Filename: FloorPacket.java
//
// Description: Stores packet information, transcodes into byte data and reverse
//
//***************************************************************************
package core.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import core.Direction;
import core.Exceptions.CommunicationException;
import core.Subsystems.SchedulerSubsystem.SchedulerRequest;
import core.Utils.SubsystemConstants;
/**
 * Used to convert between FloorPacket, and Datagram Buffer (byte[]) 
 * @author Rajat Bansal
 * Refactored: Shounak Amladi
 * */
public class FloorMessage implements SubsystemMessage {

	private byte FLOOR_FLAG = (byte) 0;
	private byte SPACER = (byte) 0;

	private final static byte[] UP = {1, 1};
	private final static byte[] DOWN = {1, 2};
	private final static byte[] STATIONARY = {1, 3};


	private int sourceFloor = -1; //THIS IS THE SOURCE FLOOR
	private Direction direction;
	private int targetFloor; //DESTINATION FLOOR (end goal)
	private boolean isValid = true;


	/**
	 *
	 * @param direction Direction
	 * @param sourceFloor current floor, i.e. source floor when direction is pressed
	 * @param date  The Date
	 * @param targetFloor Button pressed in the elevator
	 */
	public FloorMessage(Direction direction, int sourceFloor, int targetFloor) {

		this.sourceFloor = sourceFloor;
		this.direction = direction;
		this.targetFloor = targetFloor;
	}

	/**
	 *
	 * @param data
	 * @param dataLength
	 * @throws CommunicationException
	 */
	public FloorMessage(byte[] data, int dataLength) throws CommunicationException {

		isValid = true;

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
		if (data[i++] != SPACER) {
			isValid = false;
		}

		sourceFloor = data[i++]; // i = 4
		// must be zero
		if (data[i++] != SPACER) { //i = 5
			isValid = false;
		}

		targetFloor = data[i++];

		// must be zero at end
		while (i < dataLength) {
			if (data[i++] != SPACER) {
				isValid = false;
				break;
			}
		}
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

			stream.write(targetFloor); //add the button pressed in the elevator

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

	public int getTargetFloor() {
		return targetFloor;
	}

	public Direction getDirection() {

		return direction;
	}

	/**
	 * Method used by the Scheduler to send the elevatorNumber of the elevator to the floor
	 * @param elevatorNumber
	 * @return
	 */
	public byte[] sendArrival(int elevatorNumber) {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		data.write(elevatorNumber);
		return data.toByteArray();
	}

	public String toString() {

		return "Direction: " + direction.name() + " Source Location: " + sourceFloor + " Car Button Pressed Number: "
				+ targetFloor;
	}
	
	public SchedulerRequest toSchedulerRequest(InetAddress receivedAddress, int receivedPort) {
		return new SchedulerRequest(receivedAddress,receivedPort , SubsystemConstants.FLOOR, this.sourceFloor, this.direction,this.targetFloor, this.targetFloor );
		
	}
}
