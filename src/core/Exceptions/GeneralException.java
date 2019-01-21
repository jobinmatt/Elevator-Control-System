//****************************************************************************
//
// Filename: GeneralException.java
//
// Description: General Exception class
//
//***************************************************************************

package core.Exceptions;

public class GeneralException extends Exception {

    private static final long serialVersionUID = 4569467572726215310L;

    public GeneralException(String s){

        super(s);
    }

    public GeneralException(Throwable cause){

        super(cause.getLocalizedMessage(), cause);
    }

    public GeneralException(String message, Throwable cause){

        super(cause.getLocalizedMessage() + ": " + message, cause);
    }
}
