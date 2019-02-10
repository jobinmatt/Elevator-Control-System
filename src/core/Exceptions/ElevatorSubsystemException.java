//****************************************************************************
//
// Filename: ElevatorSubsystemException.java
//
// Description: Elevator Subsystem Exception class
//
//***************************************************************************

package core.Exceptions;

public class ElevatorSubsystemException extends GeneralException {

    private static final long serialVersionUID = -4002692838353295206L;

    public ElevatorSubsystemException(String message) {

        super(message);
    }

    public ElevatorSubsystemException(Throwable cause) {

        super(cause);
    }

    public ElevatorSubsystemException(String message, Throwable cause) {

        super(message, cause);
    }
}
