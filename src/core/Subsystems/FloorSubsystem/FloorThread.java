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
import java.io.ByteArrayOutputStream;
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
    private static final int DATA_SIZE = 50;

    private int port; //port to communicate with the scheduler
    private Queue<SimulationRequest> events;
    private int floorNumber;
    private Map<Integer, Shaft> shafts;
    DatagramSocket receiveSocket;
    private InetAddress floorSubsystemAddress;

    private boolean up = false;
    private boolean down = false;


    /**
     * Creates a floor thread
     */
    public FloorThread(int floorNumber, Map<Integer, Shaft> shafts, InetAddress floorSubsystemAddress, int port) throws GeneralException {
    	
        super("FloorThread " + Integer.toString(floorNumber));
        events = new LinkedList<>();
        this.shafts = shafts;
        this.floorNumber = floorNumber;
        this.floorSubsystemAddress = floorSubsystemAddress;
        this.port = port;

        try {
            receiveSocket = new DatagramSocket(port);
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
                if(event.getFloorButton() == true) {
                    up = true;
                    logger.info("Floor " + floorNumber + ": User request made. Direction button: UP" );
                    serviceRequest(event);
                    up = false;
                }else {
                    down = true;
                    logger.info("Floor " + floorNumber + ": User request made. Direction button: DOWN" );
                    serviceRequest(event);
                    down = false;
                }
            } catch (HostActionsException e) {
                logger.error("", e);
            } catch (GeneralException e) {
                logger.error(e);
            }
            events.remove(); //remove already serviced event from the queue

            try {
            	Utils.Sleep(event.getIntervalTime());
            } catch (Exception e) {
                logger.error("", e);
            }

        }

    }

    private void serviceRequest(SimulationRequest event) throws GeneralException {
    	
        /**
         * 1. Set direction lamp based on event and print out this info
         * 2. Using HostActions.send(), Send request to scheduler including the direction pressed by user and the floorNumber
         * 3. Get shaftNumber from Scheduler. Make shaft sleep, which then tells floor thread that shaft is awake
         * 4. Send message to scheduler saying that the elevator is here
         * 5. Print out that elevator is on the floor
         */

        //1. send request to Scheduler
        byte[] data = event.toBytes(); //@TODO change the SimulationRequest to a FloorPacket
        DatagramPacket packet = new DatagramPacket(data, data.length, floorSubsystemAddress, port);
        logger.info("Floor " + floorNumber + ": Sending request to scheduler..");
        HostActions.send(packet, Optional.of(receiveSocket));

        //2. Get reply from scheduler that elevator #? is on the way
        byte[] returnedData = new byte[DATA_SIZE];
        DatagramPacket receivedPacket = new DatagramPacket(returnedData, DATA_SIZE);
        HostActions.receive(receivedPacket, receiveSocket);

        //parse received packet
        CharSequence shaftNumber = Utils.bytesToHex(returnedData);
        Shaft shaft = shafts.get(Integer.getInteger(shaftNumber.toString()));
        shaft.run();

        //get ArrivalStatus from shaft and send arrival info to scheduler
        if(shaft.getStatus()) {
            //send to scheduler that: elevator #? is here
            ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            dataStream.write(floorNumber);
            dataStream.write(Integer.getInteger(shaftNumber.toString()));
            DatagramPacket packetToSend = new DatagramPacket(dataStream.toByteArray(), dataStream.toByteArray().length, floorSubsystemAddress, port);
            logger.info("Floor: " + floorNumber + ": Sending message to scheduler that elevator " + shaftNumber + " is here.");
            HostActions.send(packetToSend, Optional.of(receiveSocket));
        }
    }

	public void terminate() {
		receiveSocket.close();
		//cleanup goes here
	}
}
