package core.Subsystems.FloorSubsystem;

import core.Exceptions.InputParserException;
import core.Utils.SimulationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketException;
import java.util.*;
import static core.Utils.InputParser.parseCVSFile;

/**
 * @author Dharina H.
 */
public class FloorSubsystem {


    private static Logger logger = LogManager.getLogger(FloorSubsystem.class);

    private Map<Integer, FloorThread> floors;
    String filename;
    List<SimulationEvent> events;
    int numOfFloors;

    public FloorSubsystem(String filename, int numOfFloors) {
        floors = new HashMap<>();
        this.filename = filename;
        this.numOfFloors = numOfFloors;
    }

    public void readFile() throws InputParserException {
        events = parseCVSFile(filename);
    }

    private void createFloorThreads() throws SocketException {

        for(int i = 1; i <= numOfFloors; i++ ) {
            floors.put(i,new FloorThread(23));
        }
    }

    private void addEvents() {
        for(SimulationEvent e: events) {
           floors.get(e.getFloor()).add(new Event(e.getStartTime(), e.getFloorButton(), e.getCarButton(), e.getIntervalTime()));
        }
    }

    public void startFloorThreads() throws SocketException {
        createFloorThreads();
        addEvents();
        logger.info("Initializing floor threads.");
        floors.forEach((k,v) -> v.start());
    }
    
}
