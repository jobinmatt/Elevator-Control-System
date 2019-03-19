package test.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;

import core.ConfigurationParser;
import core.Direction;
import core.Exceptions.CommunicationException;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.GeneralException;
import core.Exceptions.HostActionsException;
import core.Exceptions.SchedulerPipelineException;
import core.Exceptions.SchedulerSubsystemException;
import core.Subsystems.ElevatorSubsystem.ElevatorSubsystem;
import core.Subsystems.FloorSubsystem.FloorSubsystem;
import core.Subsystems.SchedulerSubsystem.Elevator;
import core.Subsystems.SchedulerSubsystem.SchedulerRequest;
import core.Subsystems.SchedulerSubsystem.SchedulerSubsystem;
import core.Utils.SubsystemConstants;

//test1: Check if the event get scheduled right
//test2: Check if all elevetors are busy nothing gets scheduled 

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SchedulerTest {
	private SchedulerSubsystem schedulerSub; 
	private StartupUtility startupUtility;
	private SchedulerRequest req1;
	private SchedulerRequest req2;
	private SchedulerRequest req3;
	
	@BeforeEach
	void setUpBeforeAll() throws IOException, GeneralException, InterruptedException {
		startupUtility = StartupUtility.getInstance();
		StartupUtility.startupSubsystems();
		schedulerSub = startupUtility.getScheduler();
		req1 = new SchedulerRequest(InetAddress.getLocalHost(), 0, SubsystemConstants.FLOOR, 1, Direction.UP, 3, 3, 3, 0, 0);
		req2 = new SchedulerRequest(InetAddress.getLocalHost(), 0, SubsystemConstants.FLOOR, 3, Direction.UP, 5, 5, 5, 0, 0);
		req3 = new SchedulerRequest(InetAddress.getLocalHost(), 0, SubsystemConstants.FLOOR, 5, Direction.DOWN, 1, 1, 1, 0, 0);
	}
	
	@Test
	@DisplayName("Testing event scheduling")
	void TestA() throws SchedulerSubsystemException, CommunicationException {//We know that we have two elevators and all events should be scheduled
		schedulerSub.scheduleEvent(req1);
		schedulerSub.scheduleEvent(req2);
		assertTrue(schedulerSub.getUnscheduledEventsSet().isEmpty());
	}
	
	@Test
	@DisplayName("Testing wrong event does not get scheduled")
	void TestB() throws SchedulerSubsystemException, CommunicationException {//Want to make sure all the UP direction events get scheduled and not the DOWN direction on
		schedulerSub.scheduleEvent(req1);
		schedulerSub.scheduleEvent(req2);
		schedulerSub.scheduleEvent(req3);
		assertTrue(schedulerSub.getUnscheduledEventsSet().size() == 1);
		assertTrue(schedulerSub.getUnscheduledEventsSet().iterator().next().equals(req3));
	}
	
	@Test
	@DisplayName("Testing out of service elevators")
	void TestC() throws SchedulerSubsystemException, CommunicationException {
		schedulerSub.removeElevator(1);
		schedulerSub.scheduleEvent(req1);
		assertTrue(schedulerSub.getElevatorStatusMap().size() == 1);
		assertTrue(schedulerSub.getElevatorStatusMap().get(2).getNumRequests() == 1);
	}
	
	@AfterEach
	void tearDown() {
		StartupUtility.tearDownSystems();
	}
}
