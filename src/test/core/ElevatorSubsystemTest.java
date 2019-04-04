package test.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;

import org.junit.FixMethodOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;

import core.Exceptions.CommunicationException;
import core.Exceptions.GeneralException;
import core.Exceptions.HostActionsException;
import core.Messages.ElevatorMessage;
import core.Subsystems.ElevatorSubsystem.ElevatorCarThread;
import core.Subsystems.ElevatorSubsystem.ElevatorComponentConstants;
import core.Subsystems.ElevatorSubsystem.ElevatorComponentStates;
import core.Subsystems.ElevatorSubsystem.ElevatorSubsystem;
import core.Subsystems.SchedulerSubsystem.SchedulerSubsystem;
import core.Utils.HostActions;

import static org.junit.jupiter.api.Assertions.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ElevatorSubsystemTest {
	private SchedulerSubsystem schedulerSub; 
	private StartupUtility startupUtility;
	private ElevatorSubsystem elevSub;
	private DatagramPacket sendPacket;
	private static DatagramSocket sendSocket;
	
	
	@BeforeEach
	void setUpBeforeEach() throws IOException, GeneralException, InterruptedException {
		startupUtility = StartupUtility.getInstance();
		StartupUtility.startupSubsystems();
		schedulerSub = startupUtility.getScheduler();
		elevSub = startupUtility.getElevatorSubsystem();
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			throw new SocketException("" + e);
		}
	}
	
	@Test
	@DisplayName("Test elev moving")
	void testA() throws CommunicationException, UnknownHostException, HostActionsException {
		byte[] byteArray;
		try {
			byteArray = new ElevatorMessage(2, 1, 1).generatePacketData();
		} catch (CommunicationException e1) {
			throw new CommunicationException("" + e1);
		}
		sendPacket = new DatagramPacket(byteArray, byteArray.length);
		Map<String, ElevatorCarThread> temp = elevSub.getCarPool();
		assertTrue(temp.get("ElevatorCar1").getMotorStatus().equals(ElevatorComponentStates.ELEV_MOTOR_IDLE));
		sendPacket.setPort(temp.get("ElevatorCar1").getPort());
		sendPacket.setAddress(InetAddress.getLocalHost());
		try {
			HostActions.send(sendPacket,Optional.of(sendSocket));
		} catch (HostActionsException e) {
			throw new HostActionsException("Failed to send packet!");
		}
		pause(500);
		assertTrue(temp.get("ElevatorCar1").getMotorStatus().equals(ElevatorComponentStates.ELEV_MOTOR_DOWN));
	}
		
	
	void pause(long time){
	    long time1 = System.currentTimeMillis();
	    long time2;
	    long runTime = 0;
	    while(runTime<time){
	    	time2 = System.currentTimeMillis();
	        runTime = time2 - time1;
	    }
	}
	
	
	@AfterEach
	void tearDown() {
		StartupUtility.tearDownSystems();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendSocket.close();
	}
	
	
}
