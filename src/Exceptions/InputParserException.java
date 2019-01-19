//****************************************************************************
//
// Filename: InputParserException.java
//
// Description: Input Parser Exception class
//
//***************************************************************************
package Exceptions;

public class InputParserException extends Exception {

	private static final long serialVersionUID = -1913340013266999447L;


	public InputParserException(String message) {
		super(message);
	}

	public InputParserException(Throwable cause) {
		super(cause.getLocalizedMessage(), cause);
	}

	public InputParserException(String message, Throwable cause) {
		super(cause.getLocalizedMessage() + ": " + message, cause);
	}

}
