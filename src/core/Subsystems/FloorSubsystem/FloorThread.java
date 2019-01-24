//****************************************************************************
//
// Filename: FloorThread.java
//
// Description: Floor thread Class
//
// @author Dharina H.
//***************************************************************************

package core.Subsystems.FloorSubsystem;

import core.Exceptions.GeneralException;
import core.Exceptions.HostActionsException;
import core.Utils.HostActions;
import core.Utils.Utils;
import core.Utils.SimulationRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

/**
 * The FloorThread represents a floor on which a person can request an elevator. Maintains a queue of events.
 */
public class FloorThread extends Thread {

    private static Logger logger = LogManager.getLogger(FloorThread.class);

    private static final int PORT = 23;

    private Queue<SimulationRequest> events;
    private int floorNumber;
    private boolean up = false;
    private boolean down = false;
    private boolean lampStatus;
    private Map<Integer, Shaft> shafts;
    DatagramSocket receiveSocket;

    /**
     * Creates a floor thread
     */
    public FloorThread(int floorNumber, Map<Integer, Shaft> shafts) throws GeneralException {
    	
        super("FloorThread " + Integer.toString(floorNumber));
        events = new LinkedList<>();
        lampStatus = false; //going down
        this.shafts = shafts;
        this.floorNumber = floorNumber;

        try {
            receiveSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new GeneralException("Socket could not be created", e);
        }
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
            logger.info("Event request: " + event.toString());
            try {
                serviceRequest(event);
            } catch (HostActionsException e) {
                logger.error("", e);
            }
            events.remove(); //remove already serviced event from the queue
            up = false;
            down = false;
            try {
            	Utils.Sleep(event.getIntervalTime());
            } catch (Exception e) {
                logger.error("", e);
            }

        }

    }

    private void serviceRequest(SimulationRequest event) throws HostActionsException {
    	
        /**
         * 1. Set direction lamp based on event and print out this info
         * 2. Using HostActions.send(), Send request to scheduler including the direction pressed by user and the floorNumber
         * 3. Get timer from scheduler and poll
         * 4. Send message to scheduler saying that the elevator is here
         * 5. Print out that elevator is on the floor
         */

        if(event.getFloorButton() == true) {
            up = true;
            logger.info("Floor " + floorNumber + ": User request made. Direction button: UP" );
        }else {
            down = true;
            logger.info("Floor " + floorNumber + ": User request made. Direction button: DOWN" );
        }

        //1. send request to Scheduler
        byte[] data = event.toBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress, port); //InetAddress needed!
        logger.info("Floor: " + floorNumber + "Sending request to scheduler..");
        HostActions.send(packet, Optional.of(receiveSocket));

        //2. Get reply from scheduler that elevator is on the way
        //byte[] byte = new byte[SIZE];
        //DatagramPacket packet = new DatagramPacket(data, data.length)
        //HostActions.receive(packet)
        //parse received packet
        //Get elevatorNumber from received packet
        //shafts.get(elevatorNumber).run()


    }

	public void terminate() {
		
		//cleanup goes here
	}
}
