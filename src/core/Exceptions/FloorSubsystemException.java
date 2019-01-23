//****************************************************************************
//
// Filename: FloorSubsystemException.java
//
// Description: Floor Subsystem Exception class
//
//***************************************************************************

package core.Exceptions;

public class FloorSubsystemException extends GeneralException {

    private static final long serialVersionUID = -4002692838353295206L;

    public FloorSubsystemException(String message) {

        super(message);
    }

    public FloorSubsystemException(Throwable cause) {

        super(cause);
    }

    public FloorSubsystemException(String message, Throwable cause) {

        super(message, cause);
    }
}
