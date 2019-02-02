package coreUnitTests.Subsystems.ElevatorControlSubsystem;

import core.Subsystems.ElevatorSubsystem.ElevatorSubsystem;
import core.Subsystems.FloorSubsystem.FloorSubsystem;
import core.Subsystems.FloorSubsystem.FloorThread;
import core.Subsystems.SchedulerSubsystem.SchedulerSubsystem;
import core.Utils.SimulationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.net.InetAddress;
import java.util.Timer;

public class communicationTest {


    static int NUM_ELEVATORS = 1;
    static int NUM_FLOORS = 10;
    InetAddress address;

    FloorSubsystem floorSystem;
    ElevatorSubsystem elevatorSystem;
    SchedulerSubsystem scheduler;

    @BeforeEach
    public void initialize() throws Exception {
        address = InetAddress.getLocalHost();

        floorSystem = new FloorSubsystem(NUM_FLOORS, address, 40);
        elevatorSystem = new ElevatorSubsystem(NUM_ELEVATORS, NUM_FLOORS, 50, address);
        scheduler = new SchedulerSubsystem(NUM_ELEVATORS, NUM_FLOORS, address, address, 50, 40);

    }

    @Test
    public void startTest() throws Exception{
        scheduler.startListeners();
        scheduler.startScheduling();

        elevatorSystem.activateElevators();
        floorSystem.startFloorThreads();
    }



}
