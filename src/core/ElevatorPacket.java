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

public class ElevatorPacket {
	
	private int current_Floor = -1;
	private int destination_Floor = -1; 
	private int requested_Floor = -1;
	private boolean isValid = true;

	public ElevatorPacket(int current_Floor, int destination_Floor, int selected_Floors) {

		this.current_Floor = current_Floor;
		this.destination_Floor = destination_Floor;
		this.requested_Floor = selected_Floors;
	}

	public ElevatorPacket(byte[] data, int dataLength) {

		isValid = true;
		int i = 1;
		
		current_Floor = data[i++];		
		// must be zero
		if (data[i++] != 0) {
			isValid = false;
		}

		destination_Floor = data[i++];
		// must be zero
		if (data[i++] != 0) {
			isValid = false;
		}
		
		requested_Floor = data[i++];
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

			if (current_Floor != -1 ) {
				stream.write(current_Floor);
			}
			// add 0
			stream.write(0);
			
			if (destination_Floor != -1 ) {
				stream.write(destination_Floor);
			}
			// add 0
			stream.write(0);
			
			if (requested_Floor != -1 ) {
				stream.write(requested_Floor);
			}
			// add 0
			stream.write(0);

			return stream.toByteArray();
		} catch (NullPointerException e) {
			throw new CommunicationException("Unable to generate packet", e);
		}
	}

	public boolean isValid() {

		if (!isValid) {
			return false;
		}
		if (current_Floor == -1) {
			return false;
		}
		if (destination_Floor == -1) {
			return false;
		}
		if (requested_Floor == -1) {
			return false;
		}

		return true;
	}

	public String toString() {

			return "Current Floor: " + current_Floor + " Destination Floor: " + destination_Floor + " Requested Floor: " + requested_Floor; 
	}
}
