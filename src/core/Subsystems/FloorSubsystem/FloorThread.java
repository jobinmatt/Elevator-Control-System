//****************************************************************************
//
// Filename: FloorThread.java
//
// Description: Floor thread Class
//
// @author Dharina H.
//***************************************************************************

package core.Subsystems.FloorSubsystem;

import core.Utils.Utils;
import core.Utils.SimulationRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The FloorThread represents a floor on which a person can request an elevator. Maintains a queue of events.
 */
public class FloorThread extends Thread {

    private static Logger logger = LogManager.getLogger(FloorThread.class);

    private static final int PORT = 23;

    private Queue<SimulationRequest> events;
    private int floorNumber;
    private boolean directionButton;
    private boolean lampStatus;

    /**
     * Creates a floor thread
     */
    public FloorThread() {
    	
        super();
        events = new LinkedList<>();
        directionButton = true; //going up by default
        lampStatus = false; //off
    }

    /**
     * Add a SimulationEvent to the queue
     * @param e
     */
    public void addEvent(SimulationRequest e) {

        events.add(e);
    }

    /**
     *Services each floor request
     */
    @Override
    public void run() {

        while(!events.isEmpty()) {
            SimulationRequest event = events.peek(); //first event in the queue
            logger.info(event.toString());
            serviceRequest(event);
            events.remove(); //remove already serviced event from the queue
            
            try {
            	Utils.Sleep(event.getIntervalTime());
            } catch (Exception e) {
                logger.error("", e);
            }

        }

    }

    private void serviceRequest(SimulationRequest event) {
    	
        /**
         * 1. Set direction lamp based on event and print out this info
         * 2. Using HostActions.send(), Send request to scheduler including the direction pressed by user and the floorNumber
         * 3. Get timer from scheduler and poll
         * 4. Send message to scheduler saying that the elevator is here
         * 5. Print out that elevator is on the floor
         */
    }

	public void terminate() {
		
		//cleanup goes here
	}
}
