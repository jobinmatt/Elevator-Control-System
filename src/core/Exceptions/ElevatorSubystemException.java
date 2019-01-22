//****************************************************************************
//
// Filename: ElevatorSubystemException.java
//
// Description: Elevator Subsystem Exception class
//
//***************************************************************************

package core.Exceptions;

public class ElevatorSubystemException extends GeneralException {

    private static final long serialVersionUID = -4002692838353295206L;

    public ElevatorSubystemException(String message) {

        super(message);
    }

    public ElevatorSubystemException(Throwable cause) {

        super(cause);
    }

    public ElevatorSubystemException(String message, Throwable cause) {

        super(message, cause);
    }
}
