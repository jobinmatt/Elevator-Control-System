//****************************************************************************
//
// Filename: ConfigurationParserException.java
//
// Description: Configuration Parser Exception class
//
//***************************************************************************

package core.Exceptions;

public class ConfigurationParserException extends GeneralException {

    private static final long serialVersionUID = -4002692838353295206L;

    public ConfigurationParserException(String message) {

        super(message);
    }

    public ConfigurationParserException(Throwable cause) {

        super(cause);
    }

    public ConfigurationParserException(String message, Throwable cause) {

        super(message, cause);
    }
}
