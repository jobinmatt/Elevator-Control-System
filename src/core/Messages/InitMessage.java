package core.Messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import core.Exceptions.CommunicationException;
import core.Subsystems.SchedulerSubsystem.SchedulerRequest;

public class InitMessage implements SubsystemMessage {

	private boolean isValid = false;
	private final String INIT_MESSAGE = "INIT_MESSAGE";
	private final byte TYPE = (byte)2;
	
	public InitMessage() {
		//doesnt do jack shit
		// when in doubt pinkys out
	}
	@Override
	public byte[] generatePacketData() throws CommunicationException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.write((byte)2);
		try {
			stream.write(INIT_MESSAGE.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stream.toByteArray();
	}

	@Override
	public SchedulerRequest toSchedulerRequest(InetAddress receivedAddress, int receivedPort) {
		return null;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return isValid;
	}

	
}
