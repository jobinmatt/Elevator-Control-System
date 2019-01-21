//****************************************************************************
//
// Filename: HostActionsException.java
//
// Description: Host Actions Exception class
//
//***************************************************************************

package core.Exceptions;

public class HostActionsException extends GeneralException {

	private static final long serialVersionUID = 1278214378079736536L;

	public HostActionsException(String message) {

        super(message);
    }

    public HostActionsException(Throwable cause) {
    	
        super(cause);
    }

    public HostActionsException(String message, Throwable cause) {
    	
        super(message, cause);
    }
}
