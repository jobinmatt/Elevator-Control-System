package core.Messages;

import core.Exceptions.CommunicationException;

public class ElevatorSysMessageFactory {

	public static SubsystemMessage generateMessage(byte[] buffer, int length) throws CommunicationException {
		if (buffer[0] == (byte) 0) { // Floor
			return new FloorMessage(buffer, length);
		}
		else if (buffer[0] == (byte) 1) {
			return new ElevatorMessage(buffer, length);
		}
		else if (buffer[0] == (byte) 2) {
			return new InitMessage();
		}
		else {
			return null;
		}
	}
}
