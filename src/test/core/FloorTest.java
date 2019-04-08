package test.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import core.Direction;
import core.Exceptions.CommunicationException;
import core.Exceptions.GeneralException;
import core.Exceptions.SchedulerSubsystemException;
import core.Subsystems.FloorSubsystem.FloorButton;
import core.Subsystems.FloorSubsystem.FloorSubsystem;
import core.Subsystems.FloorSubsystem.FloorThread;
import core.Subsystems.FloorSubsystem.FloorType;

public class FloorTest {
	private FloorSubsystem floorSub;
	private StartupUtility startupUtility;
	private Map<String, FloorThread> floors;
	private int NUM_OF_FLOORS = 22;
	private int NUM_OF_ELEVATORS = 2;

	@BeforeEach
	void setUpBeforeAll() throws IOException, GeneralException, InterruptedException {
		startupUtility = StartupUtility.getInstance();
		StartupUtility.startupSubsystems(NUM_OF_ELEVATORS, NUM_OF_FLOORS);
		floorSub = startupUtility.getFloorSubsystem();
		floors = floorSub.getFloorsMap();
	}

	@Test
	@DisplayName("Testing number of floor threads")
	void TestA() throws SchedulerSubsystemException, CommunicationException {
		assertTrue(floors.size() == NUM_OF_FLOORS);
	}

	@Test
	@DisplayName("Testing bottom floor")
	void TestB() throws SchedulerSubsystemException, CommunicationException {
		FloorType floorType = floors.get("Floor1").getFloorType();
		assertTrue(floorType == FloorType.BOTTOM);
	}

	@Test
	@DisplayName("Testing top floor")
	void TestC() throws SchedulerSubsystemException, CommunicationException {
		FloorType floorType = floors.get("Floor" + NUM_OF_FLOORS).getFloorType();
		assertTrue(floorType == FloorType.TOP);
	}

	@Test
	@DisplayName("Testing bottom floor button")
	void TestD() throws SchedulerSubsystemException, CommunicationException {
		FloorButton[] floorButtons = floors.get("Floor1").getFloorButtons();
		// there should be only one button on the bottom floor
		assertTrue(floorButtons.length == 1);
	}

	@Test
	@DisplayName("Testing top floor button")
	void TestE() throws SchedulerSubsystemException, CommunicationException {
		FloorButton[] floorButtons = floors.get("Floor" + NUM_OF_FLOORS).getFloorButtons();
		// there should be only one button on the top floor
		assertTrue(floorButtons.length == 1);
	}

	@Test
	@DisplayName("Testing normal floor button")
	void TestF() throws SchedulerSubsystemException, CommunicationException {
		FloorButton[] floorButtons = floors.get("Floor" + (NUM_OF_FLOORS - 1)).getFloorButtons();
		// normal floors must have 2 buttons
		assertTrue(floorButtons.length == 2);
	}

	@Test
	@DisplayName("Testing bottom floor button direction")
	void TestG() throws SchedulerSubsystemException, CommunicationException {
		FloorButton[] floorButtons = floors.get("Floor1").getFloorButtons();
		// the bottom floor button's direction must be up.
		assertTrue(floorButtons[0].getDirection().equals(Direction.UP));
	}

	@Test
	@DisplayName("Testing top floor button direction")
	void TestH() throws SchedulerSubsystemException, CommunicationException {
		FloorButton[] floorButtons = floors.get("Floor" + NUM_OF_FLOORS).getFloorButtons();
		// the top floor button's direction must be down.
		assertTrue(floorButtons[0].getDirection().equals(Direction.DOWN));
	}

	@Test
	@DisplayName("Testing normal floor button direction")
	void TestI() throws SchedulerSubsystemException, CommunicationException {
		FloorButton[] floorButtons = floors.get("Floor" + (NUM_OF_FLOORS - 1)).getFloorButtons();
		// a normal floor must have buttons in both directions.
		assertTrue(floorButtons[0].getDirection().equals(Direction.DOWN) && floorButtons[1].getDirection().equals(Direction.UP));
	}
}
