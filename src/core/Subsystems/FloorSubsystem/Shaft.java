//****************************************************************************
//
// Filename: Shaft.java
//
// Description: This creates an elevator shaft
//
//***************************************************************************

package core.Subsystems.FloorSubsystem;

import core.Utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Shaft extends Thread{

    private static Logger logger = LogManager.getLogger(Shaft.class);
    boolean arrivalSensor;
    int shaftNumber;

    public Shaft(int shaftNumber){
        this.shaftNumber = shaftNumber;
        arrivalSensor = false;
    }

    @Override
    public void run() {
        try {
            Utils.Sleep(3000);
            arrivalSensor = true;
        }catch(Exception e) {
            logger.error("", e);
        }

    }

    public synchronized boolean getStatus() {

        while(!arrivalSensor) {
            try {
                wait(5000);
            } catch (InterruptedException e) {
                logger.error("Shaft " + shaftNumber + ": ", e);
            }
        }
        notifyAll();
        return arrivalSensor;
    }

}
