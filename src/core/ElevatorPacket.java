//****************************************************************************
//
// Filename: ElevatorPacket.java
//
// Description: Stores packet information, transcodes into byte data and reverse
//
//***************************************************************************

package core;

import java.io.ByteArrayOutputStream;

import core.Exceptions.CommunicationException;

public class ElevatorPacket implements DatagramBuffer {

	private byte ELEVATOR_FLAG = (byte) 1;
	private byte SPACER = (byte) 0;

	private int currentFloor = -1; //Current floor of the elevator
	private int destinationFloor = -1; //Destination it is heading to (final)
	private int requestedFloor = -1; //Floor requested by user
	private int elevatorNumber = -1;
	private boolean arrived = false;
	private boolean isValid = true;

	public ElevatorPacket(boolean arrived, int elevatorNumber) {

		this.arrived = arrived; 
		this.elevatorNumber = elevatorNumber;
	}

	public ElevatorPacket(int requestedFloor, int elevatorNumber) {

		this.requestedFloor = requestedFloor;
		this.elevatorNumber = elevatorNumber;
	}

	public ElevatorPacket(int currentFloor, int destinationFloor, int selectedFloors) {

		this.currentFloor = currentFloor;
		this.destinationFloor = destinationFloor;
		this.requestedFloor = selectedFloors;
	}

	public ElevatorPacket(byte[] data, int dataLength) {

		isValid = true;
		int i = 1;

		currentFloor = data[i++];
		// must be zero
		if (data[i++] != SPACER) {
			isValid = false;
		}

		destinationFloor = data[i++];
		// must be zero
		if (data[i++] != SPACER) {
			isValid = false;
		}

		requestedFloor = data[i++];
		// must be zero
		if (data[i++] != SPACER) {
			isValid = false;
		}
		if ((byte)1 == data[i]) {
			arrived = true;
		} else if ((byte)0 == data[i]) {
			arrived = false;
		}
		i++;
		// must be zero
		if (data[i++] != SPACER) {
			isValid = false;
		}

		elevatorNumber = data[i++];
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
			stream.write(ELEVATOR_FLAG); // elevator packet flag

			if (currentFloor != -1 ) {
				stream.write(currentFloor);
			} else {
				stream.write(SPACER);
			}
			
			// add space
			stream.write(SPACER);

			if (destinationFloor != -1 ) {
				stream.write(destinationFloor);
			} else {
				stream.write(SPACER);
			}
			
			// add space
			stream.write(SPACER);

			if (requestedFloor != -1 ) {
				stream.write(requestedFloor);
			} else {
				stream.write(SPACER);
			}
			
			// add space
			stream.write(SPACER);
			
			if (arrived) {
				stream.write(ELEVATOR_FLAG);
			} else {
				stream.write(SPACER);
			}
			// add space
			stream.write(SPACER);
			
			if (elevatorNumber != -1 ) {
				stream.write(elevatorNumber);
			} else {
				stream.write(SPACER);
			}
			
			// add space
			stream.write(SPACER);

			return stream.toByteArray();
		} catch (NullPointerException e) {
			throw new CommunicationException("Unable to generate packet", e);
		}
	}
	public boolean isValid() {

		if (!isValid) {
			return false;
		}

		return true;
	}
	
	public int getCurrentFloor() {
		
		return this.currentFloor;
	}
	
	public int getDestinationFloor() {
		
		return this.destinationFloor;
	}
	
	public int getRequestedFloor() {
		
		return this.requestedFloor;
	}
	
	public int getElevatorNumber() {
		
		return this.elevatorNumber;
	}
	
	public boolean getArrivalSensor() {
		
		return arrived;
	}
	
	public String toString() {

		return "Current Floor: " + currentFloor + " Destination Floor: " + destinationFloor + " Requested Floor: " + requestedFloor + " Elevator Number: " + elevatorNumber;
	}
}
