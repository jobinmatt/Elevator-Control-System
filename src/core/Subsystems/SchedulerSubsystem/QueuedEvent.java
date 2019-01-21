//****************************************************************************
//
// Filename: QueuedEvent.java
//
// Description: QueuedEvent that can be used by the scheduler
//
//***************************************************************************
package core.Subsystems.SchedulerSubsystem;

import java.net.DatagramPacket;
import java.net.InetAddress;

import core.Utils.SimulationEvent;

/**
 * 
 * This creates a QueuedEvent based on a DatagramPacket 
 * @author Jobin Mathew
 * */
public class QueuedEvent {
	private SimulationEvent simulationEvent;
	private int elevatorNumber = -1;
	private int floorNumber = -1;
	private int receivedPort;
	private InetAddress receivedAddress;

	public QueuedEvent(DatagramPacket packet) {
		receivedPort = packet.getPort();
		receivedAddress = packet.getAddress();
		// use packet.getData()
	}
}
