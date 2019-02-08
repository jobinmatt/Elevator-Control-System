package core;

import java.net.InetAddress;

import core.Exceptions.CommunicationException;
import core.Subsystems.SchedulerSubsystem.SchedulerRequest;

public interface DatagramBuffer {

	public byte[] generatePacketData() throws CommunicationException;
	public SchedulerRequest toSchedulerRequest(InetAddress receivedAddress, int receivedPort);
	public boolean isValid();
	public String toString();
}
