package core.Subsystems.SchedulerSubsystem;

import core.Direction;

public class ShutdownThread extends Thread {

	private SchedulerSubsystem scheduler;
	private ElevatorPipeline[] elevatorListeners;
	private FloorPipeline[] floorListeners;
	
	public ShutdownThread(SchedulerSubsystem scheduler, ElevatorPipeline[] elevatorListeners, FloorPipeline[] floorListeners) {
		
		this.scheduler = scheduler;
		this.elevatorListeners = elevatorListeners;
		this.floorListeners = floorListeners;
	}
	
	@Override
	public void run() {

		while (true) {

			if (scheduler.getEnd()) {
				
				boolean areMoving = false;
				for (int i: scheduler.elevatorStatus.keySet()) {
					if (scheduler.elevatorStatus.get(i).getRequestDirection() != Direction.STATIONARY) {
						areMoving = true;
					}
				}

				try {
					if (!areMoving) {
						for (ElevatorPipeline e: elevatorListeners) {
							e.sendShutdownMessage();
						}
	
						for (FloorPipeline f: floorListeners) {
							f.sendShutdownMessage();
						}
					}
				} catch (Exception e) {System.out.println(e);}
			}
		}
	}
}
