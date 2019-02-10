package core.Subsystems.SchedulerSubsystem;

import java.net.DatagramPacket;

import core.Exceptions.CommunicationException;

public interface Pipeline {

	
	public void parsePacket(DatagramPacket packet) throws CommunicationException;
	public void terminate();
}
