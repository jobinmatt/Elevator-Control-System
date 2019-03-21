package core.Messages;

import java.net.InetAddress;

import core.Direction;
import core.Exceptions.CommunicationException;
import core.Subsystems.SchedulerSubsystem.SchedulerRequest;

public class NotifyFloorMessage implements SubsystemMessage{
	
	private byte FLOOR_FLAG = (byte) 0;
	private byte SPACER = (byte) 0;
	
	private int elevatorNumber;
	private Direction direction;
	private int arrivedFloor;
	
	private final static byte[] UP = {1, 1};
	private final static byte[] DOWN = {1, 2};
	
	public NotifyFloorMessage(int elevatorNumber, Direction direction, int arrivedFloor) {
		this.elevatorNumber = elevatorNumber;
		this.direction = direction;
		this.arrivedFloor = arrivedFloor;
	}
	
	public NotifyFloorMessage(byte[] data, int dataLength) throws CommunicationException {
		//format:

				// FLOOR_FLAG SPACER SPACER ElevatorNumber SPACER Direction Direction SPACER ArrivedFloor SPACER SPACER
				// 0		  1		 2		3			   4	  5		    6         7      8            9		 10
				// extract read or write request
		
		}

	@Override
	public byte[] generatePacketData() throws CommunicationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchedulerRequest toSchedulerRequest(InetAddress receivedAddress, int receivedPort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
