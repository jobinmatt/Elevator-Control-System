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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import core.Exceptions.CommunicationException;

public class FloorPacket {

	private byte FLOOR_FLAG = (byte) 0;
	private byte SPACER = (byte) 0;

	public final static byte[] UP = {1, 1};
	public final static byte[] DOWN = {1, 2};
	public final static byte[] STATIONARY = {1, 3};


	private int sourceFloor = -1; //THIS IS THE SOURCE FLOOR
	private int elevatorNumber = -1; //NOT USED
	private Direction direction;
	private Date date;
	private int carButtonPressed; //DESTINATION FLOOR
	private boolean isValid = true;


	/**
	 *
	 * @param direction Direction
	 * @param sourceFloor current floor, i.e. source floor when direction is pressed
	 * @param number The elevator number (NOT USED RIGHT NOW)
	 * @param date  The Date
	 * @param carButtonPressed Button pressed in the elevator
	 */
	public FloorPacket(Direction direction, int sourceFloor, Date date, int carButtonPressed) {

		this.sourceFloor = sourceFloor;
		this.direction = direction;
		this.date = date;
		this.carButtonPressed = carButtonPressed;
	}

	/**
	 *
	 * @param data
	 * @param dataLength
	 * @throws CommunicationException
	 */
	public FloorPacket(byte[] data, int dataLength) throws CommunicationException {

		isValid = true;
		//format:
		// FLOOR_FLAG Direction Direction SPACER sourceFloor SPACER  Date SPACER carButton SPACER
		//	0			1			2		3			4		5		6
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

		/**elevatorNumber = data[i++];

		if (data[i++] != SPACER) {
			isValid = false;
		}*/

		i++; //i = 6
		ByteArrayOutputStream dateBytes = new ByteArrayOutputStream();
		while (data[i] != SPACER) {
			dateBytes.write(data[i]);
			i++;
		}

		DateFormat format = new SimpleDateFormat("hh:mm:ss.SSS", Locale.ENGLISH);
		try {
			date = format.parse(dateBytes.toString());
		} catch (ParseException e) {
			isValid = false;
			throw new CommunicationException("Could not parse the date.", e);
		}

		if (data[i] != SPACER) { //i = after the date bytes
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

			/**if (elevatorNumber != -1) {
				stream.write(elevatorNumber);
			}
			// add spacer
			stream.write(SPACER);*/

			stream.write(date.toString().getBytes()); //add the Date object

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
		/**if (elevatorNumber == -1) {
			return false;
		}*/

		return true;
	}

	public int getSourceFloor() {
		return sourceFloor;
	}

	public int getDestinationFloor() {
		return carButtonPressed;
	}

	public Date getDate() {
		return date;
	}

	public Direction getDirection() {
		return direction;
	}

	/**
	 * Method usesd by the Scheduler to send the elevatorNumber of the elevator to the floor
	 * @param elevatorNumber
	 * @return
	 */
	public byte[] sendArrival(int elevatorNumber) {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		data.write(elevatorNumber);
		return data.toByteArray();
	}

	public String toString() {


		return "Direction: " + direction.name() + " Elevator Location: " + sourceFloor + " Elevator Number: " + elevatorNumber;
	}
}
