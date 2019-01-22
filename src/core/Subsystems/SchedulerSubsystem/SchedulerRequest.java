//****************************************************************************
//
// Filename: SchedulerEvent.java
//
// Description: SchedulerEvent that can be used by the scheduler
//
//***************************************************************************
package core.Subsystems.SchedulerSubsystem;

import java.net.DatagramPacket;
import java.net.InetAddress;

import core.Utils.SimulationRequest;

/**
 *
 * This creates a SchedulerEvent based on a DatagramPacket
 * @author Jobin Mathew
 * */
public class SchedulerRequest {
	private SimulationRequest simulationEvent;
	private int elevatorNumber = -1;
	private int floorNumber = -1;
	private int receivedPort;
	private InetAddress receivedAddress;

	public SchedulerRequest(DatagramPacket packet) {
		receivedPort = packet.getPort();
		receivedAddress = packet.getAddress();
		// use packet.getData()
	}

	public SimulationRequest getSimulationEvent() {
		return simulationEvent;
	}

	public int getElevatorNumber() {
		return elevatorNumber;
	}

	public int getFloorNumber() {
		return floorNumber;
	}

	public int getReceivedPort() {
		return receivedPort;
	}

	public InetAddress getReceivedAddress() {
		return receivedAddress;
	}
}
