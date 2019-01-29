//****************************************************************************
//
// Filename: FloorThread.java
//
// Description: Floor thread Class
//
// @author Dharina H.
//***************************************************************************

package core.Subsystems.FloorSubsystem;

import core.Elevator_Direction;
import core.FloorPacket;
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
import java.util.Timer;
import java.util.TimerTask;

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
    private Timer atFloorTimer;

    /**
     * Creates a floor thread
     */
    public FloorThread(String name, int floorNumber, InetAddress floorSubsystemAddress, int port, Timer sharedTimer) throws GeneralException {
    	
        super(name);
        
        events = new LinkedList<>();
        this.floorNumber = floorNumber;
        this.floorSubsystemAddress = floorSubsystemAddress;
        this.port = port;
        this.atFloorTimer = sharedTimer;
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
        
        this.atFloorTimer.schedule(new TimerTask () {
        	public void run() {
        		try {
					serviceRequest(e);
				} catch (GeneralException e) {
					// TODO Auto-generated catch block
					logger.error(e);
				}
        	}
        }, e.getStartTime());
        
    }

    /**
     *Services each floor request
     */
    @Override
    public void run() {

        while(true) {
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
    	
        FloorPacket floorPacket = null;
        byte[] data = null; //data to be sent to the Scheduler

        if(event.getFloorButton() == true) {
            floorPacket = new FloorPacket(Elevator_Direction.UP, event.getFloor(), event.getStartTime(), event.getCarButton());
            data = floorPacket.generatePacketData();
        }else{
            floorPacket = new FloorPacket(Elevator_Direction.DOWN, event.getFloor(), event.getStartTime(), event.getCarButton());
            data = floorPacket.generatePacketData();
        }
    }

	public void terminate() {
		receiveSocket.close();
		//cleanup goes here
	}
}
