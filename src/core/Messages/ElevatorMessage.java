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
import java.nio.charset.StandardCharsets;

import core.Direction;
import core.Exceptions.CommunicationException;
import core.Subsystems.ElevatorSubsystem.ElevatorComponentStates;
import core.Subsystems.SchedulerSubsystem.SchedulerRequest;
import core.Utils.SubsystemConstants;
/**
 *  Used to convert between ElevatorPacket, and Datagram Buffer (byte[]), and to ScehdulerRequest
 * @author Rajat Bansal
 * Refactored: Shounak Amladi
 * */
public class ElevatorMessage implements SubsystemMessage {

	private String FORCE_CLOSE = "Force Close";
	private String DOOR_FAILURE = "Door Failure";
	private String SHUTDOWN = "Shutdown";
	
	private byte ELEVATOR_FLAG = (byte) 1;
	private byte SPACER = (byte) 0;
	private boolean stop = false;

	private int currentFloor = -1; //Current floor the elevator is on
	private int destinationFloor = -1; //where it is going to stop next
//	private int targetFloor = -1; //Floor requested by user (end destination)
	private int elevatorNumber = -1;
	private Direction direction = Direction.STATIONARY;
	private boolean arrived = false;
	private boolean isValid = true;
	private int errorCode;
	private int errorFloor;
	private ElevatorComponentStates doorStatus;
	private boolean forceCloseStatus = false;
	private boolean doorFailureStatus = false;
	private boolean shutdownStatus = false;

	public ElevatorMessage () {
		
	}
	
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
	
	public ElevatorMessage(int currentFloor, int destinationFloor, int elevNumber, Direction direction) {

		this.currentFloor = currentFloor;
		this.destinationFloor = destinationFloor;
		this.elevatorNumber = elevNumber;
		this.setDirection(direction);
	}
	
	public ElevatorMessage(int currentFloor, int destinationFloor, int elevNumber, Direction direction, int errorCode, int errorFloor) {

		this.currentFloor = currentFloor;
		this.destinationFloor = destinationFloor;
		this.elevatorNumber = elevNumber;
		this.errorCode = errorCode; 
		this.errorFloor = errorFloor;
		this.setDirection(direction);
	}

	public ElevatorMessage(byte[] data, int dataLength) {

		isValid = true;
		int i = 1;

		String str = new String(data, 0, dataLength, StandardCharsets.UTF_8);
		if (str.equals(FORCE_CLOSE)) {
			forceCloseStatus = true;
			return;
		} else if (str.equals(DOOR_FAILURE)) {
			doorFailureStatus = true;
			return;
		} else if (str.equals(SHUTDOWN)) {
			shutdownStatus = true;
			return;
		}
		
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
		// must be zero
		if (data[i++] != SPACER) {
			isValid = false;
		}

		errorCode = data[i++];
		// must be zero
		if (data[i++] != SPACER) {
			isValid = false;
		}
		
		errorFloor = data[i++];
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
			
			if (errorCode != -1 ) {
				stream.write(errorCode);
			} else {
				stream.write(SPACER);
			}
			
			// add space
			stream.write(SPACER);
			
			if (errorFloor != -1 ) {
				stream.write(errorFloor);
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
	
	public byte[] generateForceCloseMessage() throws CommunicationException {

		return generateCustomMessage(FORCE_CLOSE);
	}
	
	public byte[] generateDoorFailureMessage() throws CommunicationException {

		return generateCustomMessage(DOOR_FAILURE);
	}
	
	public byte[] generateShutdownMessage() throws CommunicationException {

		return generateCustomMessage(SHUTDOWN);
	}
	
	public byte[] generateCustomMessage(String message) throws CommunicationException {

		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			stream.write(message.getBytes());
			isValid = true;
			return stream.toByteArray();
		}  catch (NullPointerException | IOException e) {
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
	
	public int getElevatorNumber() {
		
		return this.elevatorNumber;
	}
	
	public boolean getArrivalSensor() {
		
		return arrived;
	}
	
	public int getErrorCode() {
		
		return errorCode;
	}
	
	public int getErrorFloor() {
		
		return errorFloor;
	}
	
	public void setArrivalSensor(boolean isArrive) {
		
		this.arrived = isArrive;
	}
	
	public boolean isStop() {
		
		return this.stop;
	}
	
	public ElevatorComponentStates getDoorStatus() {
		
		return doorStatus;
	}
	
	public boolean getForceCloseStatus() {
		
		return forceCloseStatus;
	}
	
	public boolean getDoorFailureStatus() {
		
		return doorFailureStatus;
	}
	
	public boolean getShutdownStatus() {
		
		return shutdownStatus;
	}
	
	public String toString() {

		return "Current Floor: " + currentFloor + " Destination Floor: " + destinationFloor + " Elevator Number: " + elevatorNumber;
	}

	@Override
	public SchedulerRequest toSchedulerRequest(InetAddress receivedAddress, int receivedPort) {
		Direction dir = null;
		if (this.currentFloor>this.destinationFloor) {
			dir = Direction.DOWN;
		}else {
			dir = Direction.UP;
		}
		return new SchedulerRequest(receivedAddress,receivedPort , SubsystemConstants.FLOOR, this.currentFloor, dir,this.elevatorNumber, this.destinationFloor,0,0 );
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}
}
