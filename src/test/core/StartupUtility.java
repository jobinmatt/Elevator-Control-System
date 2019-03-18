package test.core;

import java.io.IOException;
import java.net.InetAddress;

import core.ConfigurationParser;
import core.Exceptions.CommunicationException;
import core.Exceptions.ConfigurationParserException;
import core.Exceptions.ElevatorSubsystemException;
import core.Exceptions.GeneralException;
import core.Exceptions.HostActionsException;
import core.Exceptions.SchedulerPipelineException;
import core.Exceptions.SchedulerSubsystemException;
import core.Subsystems.ElevatorSubsystem.ElevatorSubsystem;
import core.Subsystems.FloorSubsystem.FloorSubsystem;
import core.Subsystems.SchedulerSubsystem.SchedulerSubsystem;

class StartupUtility {
	private static StartupUtility startupUtility = null;
	private static SchedulerSubsystem scheduler;
	private static ElevatorSubsystem elevator;
	private static FloorSubsystem floor;
	
	public synchronized static StartupUtility getInstance() {
		if(startupUtility == null) {
			startupUtility = new StartupUtility();
		}
		return startupUtility;
	}

	public static void startupSubsystems() throws IOException, GeneralException, InterruptedException {
		ConfigurationParser configurationParser = ConfigurationParser.getInstance();
		int numElevators = configurationParser.getInt(ConfigurationParser.NUMBER_OF_ELEVATORS);
		int numFloors = configurationParser.getInt(ConfigurationParser.NUMBER_OF_FLOORS);
		int elevatorInitPort = configurationParser.getInt(ConfigurationParser.ELEVATOR_INIT_PORT);
		int floorInitPort = configurationParser.getInt(ConfigurationParser.FLOOR_INIT_PORT);
		InetAddress schedulerAddress = InetAddress.getByName(configurationParser.getString(ConfigurationParser.SCHEDULER_ADDRESS));
		SubsystemWrapper t = new SubsystemWrapper();
		//input the right address
		Thread schedulerStartup = new Thread(new SubsystemStartThread(numElevators, numFloors, elevatorInitPort, floorInitPort, scheduler, t), "SchedulerStartup");
		schedulerStartup.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Thread elevatorStartup = new Thread(new ElevatorStartThread(numElevators, numFloors, elevatorInitPort, schedulerAddress, elevator, t), "ElevatorStartup");
		elevatorStartup.start();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Thread floorStartup = new Thread(new FloorStartThread(numFloors, numElevators, floorInitPort, schedulerAddress, floor, t), "FloorStartup");
		floorStartup.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		schedulerStartup.join();
		elevatorStartup.join();
		floorStartup.join();
		
		scheduler = t.getS();
		elevator = t.getE();
		floor = t.getF();
		
	}
	
	public SchedulerSubsystem getScheduler() {
		return StartupUtility.scheduler;
	}
	
	public ElevatorSubsystem getElevatorSubsystem() {
		return StartupUtility.elevator;
	}

	public FloorSubsystem getFloorSubsystem() {
		return StartupUtility.floor;
	}
}

class SubsystemStartThread implements Runnable{
	private int numElevatorsScheduler;
	private int numFloorsScheduler;
	private int elevatorInitPort;
	private int floorInitPort;
	private volatile SchedulerSubsystem subsystem;
	private SubsystemWrapper t;

	public SubsystemStartThread(int numElevators, int numFloors, int elevatorInitPort, int floorInitPort, SchedulerSubsystem subsystem, SubsystemWrapper t) {
		super();
		
		this.numElevatorsScheduler = numElevators;
		this.numFloorsScheduler = numFloors;
		this.elevatorInitPort = elevatorInitPort;
		this.floorInitPort = floorInitPort;
		this.subsystem = subsystem;
		this.t= t;
	}

	public void run() {
		
		try {
			subsystem = new SchedulerSubsystem(numElevatorsScheduler, numFloorsScheduler, elevatorInitPort, floorInitPort);
		} catch (SchedulerPipelineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SchedulerSubsystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConfigurationParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HostActionsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t.setS(subsystem);
		Thread.currentThread().interrupt();
	}
	
}

class ElevatorStartThread implements Runnable{
	private int numElevatorsElevator;
	private int numFloorsElevator;
	private int elevatorInitPort;
	private volatile ElevatorSubsystem subsystem;
	private InetAddress schedulerAddress;
	private SubsystemWrapper t;

	public ElevatorStartThread(int numElevators, int numFloors, int elevatorInitPort, InetAddress schedulerAddress, ElevatorSubsystem subsystem, SubsystemWrapper t) {
		super();
		this.numElevatorsElevator = numElevators;
		this.numFloorsElevator = numFloors;
		this.elevatorInitPort = elevatorInitPort;
		this.subsystem = subsystem;
		this.schedulerAddress = schedulerAddress;
		this.t = t;
	}

	public void run() {

		try {
			subsystem = new ElevatorSubsystem(numElevatorsElevator, numFloorsElevator, elevatorInitPort, schedulerAddress);
		} catch (ElevatorSubsystemException | ConfigurationParserException | HostActionsException
				| CommunicationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t.setE(subsystem);
		Thread.currentThread().interrupt();
	}
}

class FloorStartThread implements Runnable{
	private int numElevatorsFloor;
	private int numFloorsFloor;
	private int floorInitPort;
	private FloorSubsystem subsystem;
	private InetAddress schedulerAddress;
	private SubsystemWrapper t;

	public FloorStartThread(int numFloors ,int numElevators, int floorInitPort, InetAddress schedulerAddress, FloorSubsystem subsystem, SubsystemWrapper t) {
		super();
		this.numElevatorsFloor = numElevators;
		this.numFloorsFloor = numFloors;
		this.floorInitPort = floorInitPort;
		this.subsystem = subsystem;
		this.schedulerAddress = schedulerAddress;
		this.t = t;
	}

	public void run() {

		try {
			subsystem = new FloorSubsystem(numFloorsFloor, schedulerAddress, floorInitPort, numElevatorsFloor);
		} catch (GeneralException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		t.setF(subsystem);
		Thread.currentThread().interrupt();
	}
	
}

