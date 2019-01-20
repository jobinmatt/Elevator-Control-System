package core.Subsystems.FloorSubsystem;

import java.util.Date;

/**
 * @author Dharina H.
 */
public class Event {

    Date startTime;
    boolean direction;
    int destinationFloor;
    long intervalTime;

    public Event(Date startTime, boolean direction, int destinationFloor, long intervalTime) {
        this.startTime = startTime;
        this.direction = direction;
        this.destinationFloor = destinationFloor;
        this.intervalTime = intervalTime;

    }

    public long getIntervalTime() {
        return intervalTime;
    }

    @Override
    public String toString() {
        if(direction) {
            return "Time: " + startTime + " Direction: UP" + " destination floor: " + destinationFloor;
        }
        return "Time: " + startTime + " Direction: DOWN" + " destination floor: " + destinationFloor;
    }
}
