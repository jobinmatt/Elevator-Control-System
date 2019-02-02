package test.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import core.Exceptions.ElevatorSubystemException;
import core.Exceptions.HostActionsException;
import core.Subsystems.ElevatorSubsystem.ElevatorCarThread;
import core.Subsystems.ElevatorSubsystem.ElevatorComponentStates;
import core.Utils.HostActions;


class ElevatorCarStateTest {
	
	private static ElevatorCarThread eThread;
	private static DatagramSocket sendSocket;
	private DatagramPacket sendPacket;
	private static String ELEVATOR_CAR_NAME = "ElevatorCar1";
	private static int NUMBER_OF_FLOORS = 10;
	private static int PORT_NUMBER = 50001;
	private static InetAddress localAddress;

	@BeforeAll
	static void setUp() throws ElevatorSubystemException, UnknownHostException, SocketException, InterruptedException{
		System.out.println("Setup...");
	
		localAddress = InetAddress.getLocalHost();
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			throw new SocketException("" + e);
		}
		
		createElevatorCarAndStart();
		
		Thread.sleep(500);
		System.out.println("Setup Complete");
	}
	
	@Test
	void ElevatorCarState_Startup_Idle(){
		System.out.println("\nTest:");
		System.out.println("ElevatorCarState_Startup_Idle");
		assertEquals(ElevatorComponentStates.ELEV_MOTOR_IDLE, eThread.getMotorStatus());
		System.out.println("\nTest Successful");
	}
	
	@Test
	void ElevatorCarState_DestFloor5_MoveUP() throws HostActionsException{
		System.out.println("\nTest:");
		System.out.println("ElevatorCarState_DestFloor5_MoveUP");
		
		byte[] byteArray = {1,1,0,5,0,5,0,0,0,0};
		sendPacket = new DatagramPacket(byteArray, byteArray.length);
		sendPacket.setAddress(localAddress);
		sendPacket.setPort(PORT_NUMBER);
		
		try {
			HostActions.send(sendPacket,Optional.of(sendSocket));
		} catch (HostActionsException e) {
			throw new HostActionsException("Failed to send packet!");
		}
		pause(1000);
		assertEquals(ElevatorComponentStates.ELEV_MOTOR_UP, eThread.getMotorStatus());
		System.out.println("\nTest Successful");
	}
	
	@Test
	void ElevatorCarState_DestFloor2_MoveDOWN() throws HostActionsException, ElevatorSubystemException, UnknownHostException{
		System.out.println("\nTest:");
		System.out.println("ElevatorCarState_DestFloor2_MoveDOWN");
		createElevatorCarAndStart();
		byte[] byteArray = {1,10,0,2,0,0,0,0,0,0};
		sendPacket = new DatagramPacket(byteArray, byteArray.length);
		sendPacket.setAddress(localAddress);
		sendPacket.setPort(PORT_NUMBER);
		
		try {
			HostActions.send(sendPacket,Optional.of(sendSocket));
		} catch (HostActionsException e) {
			throw new HostActionsException("Failed to send packet!");
		}
		pause(1000);
		assertEquals(ElevatorComponentStates.ELEV_MOTOR_DOWN, eThread.getMotorStatus());
		System.out.println("\nTest Successful");
	}
	
	@Test
	void ElevatorCarState_DestFloor2Arrived_Stopping() throws HostActionsException, ElevatorSubystemException, UnknownHostException{
		System.out.println("\nTest:");
		System.out.println("ElevatorCarState_DestFloor2Arrived_Stopping");
		createElevatorCarAndStart();
		byte[] byteArray = {1,3,0,2,0,0,0,0,0,0};
		sendPacket = new DatagramPacket(byteArray, byteArray.length);
		sendPacket.setAddress(localAddress);
		sendPacket.setPort(PORT_NUMBER);
		
		try {
			HostActions.send(sendPacket,Optional.of(sendSocket));
		} catch (HostActionsException e) {
			throw new HostActionsException("Failed to send packet!");
		}
		pause(8000);
		assertEquals(ElevatorComponentStates.ELEV_MOTOR_IDLE, eThread.getMotorStatus());
		System.out.println("\nTest Successful");
	}
	
	@Test
	void ElevatorCarState_DoorOpening_DoorClosing() throws HostActionsException, ElevatorSubystemException, UnknownHostException{
		System.out.println("\nTest:");
		System.out.println("ElevatorCarState_DoorOpening_DoorClosing");
		createElevatorCarAndStart();
		byte[] byteArray = {1,2,0,2,0,0,0,0,0,0};
		sendPacket = new DatagramPacket(byteArray, byteArray.length);
		sendPacket.setAddress(localAddress);
		sendPacket.setPort(PORT_NUMBER);
		
		try {
			HostActions.send(sendPacket,Optional.of(sendSocket));
		} catch (HostActionsException e) {
			throw new HostActionsException("Failed to send packet!");
		}
		pause(1500);
		assertEquals(ElevatorComponentStates.ELEV_DOORS_OPEN, eThread.getDoorStatus());
		pause(3000);
		assertEquals(ElevatorComponentStates.ELEV_DOORS_CLOSE, eThread.getDoorStatus());
		System.out.println("\nTest Successful");
	}
	
	@AfterAll
	static void tearDown(){
		System.out.println("\nTearDown...");
		eThread.terminate();
		sendSocket.close();
		System.out.println("TearDown Complete");
	}
	
	static void createElevatorCarAndStart() throws ElevatorSubystemException, UnknownHostException {
		if(eThread != null) eThread.terminate();
		try {
			eThread = new ElevatorCarThread(ELEVATOR_CAR_NAME, NUMBER_OF_FLOORS, PORT_NUMBER, InetAddress.getLocalHost());
		} catch (ElevatorSubystemException e) {
			throw new ElevatorSubystemException("" + e);
		}catch(UnknownHostException e) {
			throw new UnknownHostException("" + e);
		}
		eThread.start();
	}
	
	static void pause(long time){
	    long time1 = System.currentTimeMillis();
	    long time2;
	    long runTime = 0;
	    while(runTime<time){
	    	time2 = System.currentTimeMillis();
	        runTime = time2 - time1;
	    }
	}
}
