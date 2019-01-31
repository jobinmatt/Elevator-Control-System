package test.core;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import core.Direction;
import core.ElevatorPacket;
import core.FloorPacket;
import core.Exceptions.CommunicationException;

class PacketDataTest {

	private ElevatorPacket packetElevator;
	private FloorPacket packetFloor;
	private int[] floorData1,floorData2;
	private Direction dir; 
	@BeforeEach
	void setUp() throws Exception {
		bufferSetup();
		packetElevator = new ElevatorPacket(floorData1[0], floorData1[1],floorData1[2],floorData1[3]);
		packetFloor = new FloorPacket(this.dir, floorData2[1],floorData2[2]);
	}

	@AfterEach
	void tearDown() throws Exception {
		this.floorData1 = null;
		this.floorData2 = null;
		
		this.packetElevator=null;
		this.packetFloor=null;
		this.dir = null;
	}

	@Test
	void test() {
		assertEquals(floorData1[0],packetElevator.getCurrentFloor(),floorData1[0]+"!= "+packetElevator.getCurrentFloor());
		assertEquals(floorData1[1],packetElevator.getDestinationFloor(),floorData1[1]+"!= "+packetElevator.getDestinationFloor());
		assertEquals(floorData1[2],packetElevator.getRequestedFloor(),floorData1[2]+"!= "+packetElevator.getRequestedFloor());
		assertEquals(floorData1[3],packetElevator.getElevator_Number(),floorData1[3]+"!= "+packetElevator.getElevator_Number());
		
		try {
			byte[] actualElevatorData = generateActualElevatorData(floorData1[0], floorData1[1],floorData1[2],floorData1[3]);
			assertArrayEquals(actualElevatorData,packetElevator.generatePacketData(),"Elevator buffer data was not generated correctly.");
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(this.dir,packetFloor.getDirection(),this.dir+"!= "+packetFloor.getDirection());
		assertEquals(floorData2[1],packetFloor.getSourceFloor(),floorData2[1]+"!= "+packetFloor.getSourceFloor());
		assertEquals(floorData2[2],packetFloor.getDestinationFloor(),floorData2[2]+"!= "+packetFloor.getDestinationFloor());
		
		
		try {
			byte[] actualFloorData = generateActualFloorData(this.dir, floorData2[1],floorData2[2]);
			assertArrayEquals(actualFloorData,packetFloor.generatePacketData(),"Elevator buffer data was not generated correctly.");
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	int random(int min, int max)
	{
	   int range = (max - min) + 1;     
	   return (int)(Math.random() * range) + min;
	}
	
	void bufferSetup() {
		floorData1 = new int[4];
		floorData2 = new int[3];
		
		for (int i=0;i<floorData1.length;i++) {
			floorData1[i] = random(1,10);
			
		}
		for (int i=0;i<floorData2.length;i++) {
			floorData2[i] = random(1,10);
			
		}
		this.dir = Direction.values()[random(0,2)];
	}
	
	byte[] generateActualElevatorData(int curr_f, int dest_f, int selected_f, int elevnum) throws CommunicationException {
		final byte  SPACER = (byte)0;
		final byte ELEVATOR_FLAG = (byte) 1;
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			stream.write(ELEVATOR_FLAG); // elevator packet flag

			if (curr_f != -1 ) {
				stream.write(curr_f);
			}
			// add space
			stream.write(SPACER);

			if (dest_f != -1 ) {
				stream.write(dest_f);
			}
			// add space
			stream.write(SPACER);

			if (selected_f != -1 ) {
				stream.write(selected_f);
			}
			// add space
			stream.write(SPACER);

			return stream.toByteArray();
		} catch (NullPointerException e) {
			throw new CommunicationException("Unable to generate packet", e);
		}
	}
	byte[] generateActualFloorData(Direction direction, int sourceFloor, int carButtonPressed) throws CommunicationException {
		byte FLOOR_FLAG = (byte) 0;
		byte SPACER = (byte) 0;

		final byte[] UP = {1, 1};
		final byte[] DOWN = {1, 2};
		final byte[] STATIONARY = {1, 3};

		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			stream.write(FLOOR_FLAG); // floor packet flag

			// write request bytes
			switch (direction) {
			case UP:
				stream.write(UP);
				break;
			case DOWN:
				stream.write(DOWN);
				break;
			case STATIONARY:
				stream.write(STATIONARY);
				break;
			default:
				throw new CommunicationException("Unable to generate packet");
			}

			// add spacer
			stream.write(SPACER);

			if (sourceFloor != -1) {
				stream.write(sourceFloor);
			}
			// add spacer
			stream.write(SPACER);

			stream.write(carButtonPressed); //add the button pressed in the elevator

			stream.write(SPACER);

			return stream.toByteArray();
		} catch (IOException | NullPointerException e) {
			throw new CommunicationException("Unable to generate packet", e);
		}
	}
}
