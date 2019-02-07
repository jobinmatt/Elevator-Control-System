package core;

import core.Exceptions.CommunicationException;

public interface DatagramBuffer {

	public byte[] generatePacketData() throws CommunicationException;
	public boolean isValid();
	public String toString();
}
