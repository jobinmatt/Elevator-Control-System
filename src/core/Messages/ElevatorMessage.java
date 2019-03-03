//****************************************************************************
//
// Filename: ElevatorPacket.java
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
 *  Used to convert between ElevatorPacket, and Datagram Buffer (byte[]), and to ScehdulerRequest
 * @author Rajat Bansal
 * Refactored: Shounak Amladi
 * */
public class  ElevatorMessage implements SubsystemMessage {

	private byte ELEVATOR_FLAG = (byte) 1;
	private byte SPACER = (byte) 0;

	private int currentFloor = -1; //Current floor the elevator is on
	private int destinationFloor = -1; //where it is going to stop next
//	private int targetFloor = -1; //Floor requested by user (end destination)
	private int elevatorNumber = -1;//dont leave this empty we neeeeeeeeeeeeeed ITTTTT
	private boolean arrived = false;
	private boolean isValid = true;
	private boolean doorsOpen;


	public ElevatorMessage(boolean arrived, int elevatorNumber) {

		this.arrived = arrived; 
		this.elevatorNumber = elevatorNumber;
	}

	public ElevatorMessage(int requestedFloor, int elevatorNumber) {

		this.destinationFloor = requestedFloor;
		this.elevatorNumber = elevatorNumber;
	}

	public ElevatorMessage(int currentFloor, int destinationFloor, int elevNumber) {

		this.currentFloor = currentFloor;
		this.destinationFloor = destinationFloor;
		this.elevatorNumber = elevNumber;
	}

	public ElevatorMessage(){}

	public ElevatorMessage(byte[] data, int dataLength) {

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

		i++;
		if((byte)0 == data[i]) {
			doorsOpen = false;
		}else {
			doorsOpen = true;
		}

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

			if(doorsOpen) {
				stream.write(1);
			}else {
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

	public boolean getDoorStatus() {
		return doorsOpen;
	}

	public int getElevatorNumber() {
		
		return this.elevatorNumber;
	}
	
	public boolean getArrivalSensor() {
		
		return arrived;
	}
	
	public void setArrivalSensor(boolean isArrive) {
		this.arrived = isArrive;
	}

	public void setDoorsOpen(boolean b) {
		this.doorsOpen = b;
	}

	public String toString() {

		return "Current Floor: " + currentFloor + " Destination Floor: " + destinationFloor + " Elevator Number: " + elevatorNumber + " Doors can open/close: " + doorsOpen;
	}

	@Override
	public SchedulerRequest toSchedulerRequest(InetAddress receivedAddress, int receivedPort) {
		Direction dir = null;
		if (this.currentFloor>this.destinationFloor) {
			dir = Direction.DOWN;
		}else {
			dir = Direction.UP;
		}
		return new SchedulerRequest(receivedAddress,receivedPort , SubsystemConstants.FLOOR, this.currentFloor, dir,this.elevatorNumber, this.destinationFloor );
	}
}
