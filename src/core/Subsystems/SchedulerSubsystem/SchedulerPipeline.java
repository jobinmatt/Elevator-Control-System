package core.Subsystems.SchedulerSubsystem;

import java.net.DatagramSocket;

import core.Utils.SubsystemConstants;

public interface SchedulerPipeline {
	public SubsystemConstants getObjectType();
	public DatagramSocket getSendSocket();
	public DatagramSocket getReceiveSocket();
	public int getSendPort();
	public int getReceivePort();
	public int getPipeNumber();
}
