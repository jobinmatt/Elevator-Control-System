//****************************************************************************
//
// Filename: ConfigurationParserException.java
//
// Description: Configuration Parser Exception class
//
//***************************************************************************

package core.Exceptions;

public class CommunicationException extends GeneralException {

	private static final long serialVersionUID = -6223162181492170454L;

	public CommunicationException(String message) {

        super(message);
    }

    public CommunicationException(Throwable cause) {

        super(cause);
    }

    public CommunicationException(String message, Throwable cause) {

        super(message, cause);
    }
}
