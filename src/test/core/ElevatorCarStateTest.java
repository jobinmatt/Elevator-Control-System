package test.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Optional;

import org.junit.FixMethodOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;

import core.ConfigurationParser;
import core.ElevatorPacket;
import core.Exceptions.CommunicationException;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.ElevatorSubystemException;
import core.Exceptions.HostActionsException;
import core.Subsystems.ElevatorSubsystem.ElevatorCarThread;
import core.Subsystems.ElevatorSubsystem.ElevatorComponentStates;
import core.Utils.HostActions;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ElevatorCarStateTest {
	
	private static ElevatorCarThread eThread;
	private static DatagramSocket sendSocket;
	private DatagramPacket sendPacket;
	private static String ELEVATOR_CAR_NAME = "ElevatorCar1";
	private static int NUMBER_OF_FLOORS = 10;
	private static int PORT_NUMBER = 50001;
	private static InetAddress localAddress;
	private static int floorTravelTime;
	private static int doorOpenTime;
	
	@BeforeAll
	static void setUpBeforeAll() throws UnknownHostException, SocketException, ElevatorSubystemException, ConfigurationParserException {
		localAddress = InetAddress.getLocalHost();
		try {
			floorTravelTime = ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_FLOOR_TRAVEL_TIME_SECONDS)*1000;
			doorOpenTime = ConfigurationParser.getInstance().getInt(ConfigurationParser.ELEVATOR_DOOR_TIME_SECONDS)*1000;
		} catch (ConfigurationParserException e1) {
			throw new ConfigurationParserException("" + e1);
		}
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			throw new SocketException("" + e);
		}
		createElevatorCarAndStart();
	}
	
	@Test
	@DisplayName("Testing Elevator Starts and is Idle")
	void TestA(){
		System.out.println("\nTest:");
		System.out.println("ElevatorCarState_Startup_Idle");
		assertEquals(ElevatorComponentStates.ELEV_MOTOR_IDLE, eThread.getMotorStatus());
		System.out.println("\nTest Successful");
	}
	
	@Test
	@DisplayName("Testing Elevator Moves Up")
	void TestB() throws HostActionsException, CommunicationException{
		System.out.println("\nTest:");
		System.out.println("ElevatorCarState_MotorUP");
		
		byte[] byteArray;
		try {
			byteArray = new ElevatorPacket(1, 2, 2).generatePacketData();
		} catch (CommunicationException e1) {
			throw new CommunicationException("" + e1);
		}
		sendPacket = new DatagramPacket(byteArray, byteArray.length);
		sendPacket.setAddress(localAddress);
		sendPacket.setPort(PORT_NUMBER);
		
		try {
			HostActions.send(sendPacket,Optional.of(sendSocket));
		} catch (HostActionsException e) {
			throw new HostActionsException("Failed to send packet!");
		}
		pause(500);
		assertEquals(ElevatorComponentStates.ELEV_MOTOR_UP, eThread.getMotorStatus());
		pause(floorTravelTime + 500);
		System.out.println("\nTest Successful");
	}
	
	@Test
	@DisplayName("Testing Elevator Moves Down")
	void TestC() throws HostActionsException, ElevatorSubystemException, UnknownHostException, CommunicationException{
		System.out.println("\nTest:");
		System.out.println("ElevatorCarState_MoveDOWN");
		
		byte[] byteArray;
		try {
			byteArray = new ElevatorPacket(2, 1, 1).generatePacketData();
		} catch (CommunicationException e1) {
			throw new CommunicationException("" + e1);
		}
		sendPacket = new DatagramPacket(byteArray, byteArray.length);
		sendPacket.setAddress(localAddress);
		sendPacket.setPort(PORT_NUMBER);
		
		try {
			HostActions.send(sendPacket,Optional.of(sendSocket));
		} catch (HostActionsException e) {
			throw new HostActionsException("Failed to send packet!");
		}
		pause(500);
		assertEquals(ElevatorComponentStates.ELEV_MOTOR_DOWN, eThread.getMotorStatus());
		pause(floorTravelTime * 1);
		System.out.println("\nTest Successful");
	}
	
	@Test
	@DisplayName("Testing Elevator Arrives at destination and Stops")
	void TestD() throws HostActionsException, ElevatorSubystemException, UnknownHostException{
		System.out.println("\nTest:");
		System.out.println("ElevatorCarState_Stopping");
		
		byte[] byteArray = {1,2,0,1,0,0,0,0,0,0};
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
		pause(floorTravelTime + 500);
		System.out.println("\nTest Successful");
	}
	
	@Test
	@DisplayName("Testing Elevator Arrives and Stops, Opens and Closes The Door")
	void TestE() throws HostActionsException, ElevatorSubystemException, UnknownHostException, CommunicationException{
		System.out.println("\nTest:");
		System.out.println("ElevatorCarState_DoorOpening_DoorClosing");
		
		byte[] byteArray;
		try {
			byteArray = new ElevatorPacket(1, 2, 2).generatePacketData();
			sendPacket = new DatagramPacket(byteArray, byteArray.length);
			sendPacket.setAddress(localAddress);
			sendPacket.setPort(PORT_NUMBER);
		} catch (CommunicationException e1) {
			throw new CommunicationException("" + e1);
		}
	
		
		try {
			HostActions.send(sendPacket,Optional.of(sendSocket));
		} catch (HostActionsException e) {
			throw new HostActionsException("Failed to send packet!");
		}
		pause(floorTravelTime + 1000);
		assertEquals(ElevatorComponentStates.ELEV_DOORS_OPEN, eThread.getDoorStatus());
		pause(doorOpenTime + 1000);
		assertEquals(ElevatorComponentStates.ELEV_DOORS_CLOSE, eThread.getDoorStatus());
		System.out.println("\nTest Successful");
	}
	
	static void createElevatorCarAndStart() throws ElevatorSubystemException, UnknownHostException {
		if (eThread != null) {
			eThread.terminate();
		}
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
	
	@AfterAll
	static void tearDown(){
		System.out.println("\nTearDown...");
		sendSocket.close();
		System.out.println("TearDown Complete");
	}
}
