package core.Subsystems.FloorSubsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Dharina H.
 */
public class FloorThread extends Thread{

    private static Logger logger = LogManager.getLogger(FloorSubsystem.class);

    Queue<Event> events;
    DatagramSocket sendReceiveSocket;
    int floorNumber;
    boolean directionButton;
    boolean lampStatus;
    int port;

    public FloorThread(int port) throws SocketException {
        super();
        events = new LinkedList<>();
        directionButton = false; //going down by default
        lampStatus = false; //off
        this.port = port;

        sendReceiveSocket = new DatagramSocket(port);
    }

    public void addEvent(Event e) {
        events.add(e);
    }

    @Override
    public void run() {

        while(!events.isEmpty()) {
            Event e = events.peek(); //first event in the queue
            logger.info(e.toString());
            serviceRequest(e);
            events.remove(); //remove already serviced event from the queue
            try {
                this.sleep(e.getIntervalTime());
            } catch (InterruptedException e1) {
                e1.printStackTrace(); //***will be changed to throw an exception
            }

        }

    }

    private void serviceRequest(Event event) {
        /**
         * 1. Set direction lamp based on event and print out this info
         * 2. Send request to scheduler including the direction pressed by user and the floorNumber
         * 3. Get timer from scheduler and poll
         * 4. Send message to scheduler saying that the elevator is here
         * 5. Print out that elevator is on the floor
         */
    }
}
