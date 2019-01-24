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

    public Shaft(){
        arrivalSensor = false;
    }

    @Override
    public void run() {
        try {
            Utils.Sleep(2000);
            arrivalSensor = true;
        }catch(Exception e) {
            logger.error("", e);
        }

    }


    public synchronized boolean getStatus() {
        return arrivalSensor;
    }

}
