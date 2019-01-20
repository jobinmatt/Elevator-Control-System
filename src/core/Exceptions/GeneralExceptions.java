//****************************************************************************
//
// Filename: AppletInitializerException.java
//
// Description: applet installation exception class
//
// Copyright 2018 SafeNet. All rights reserved.
//
// All rights reserved. This file contains information that is
// proprietary to SafeNet and may not be distributed
// or copied without written consent from SafeNet.
//
//***************************************************************************

package core.Exceptions;

public class GeneralExceptions extends Exception {

    private static final long serialVersionUID = 4569467572726215310L;

    public GeneralExceptions(String s){

        super(s);
    }

    public GeneralExceptions(Throwable cause){

        super(cause.getLocalizedMessage(), cause);
    }

    public GeneralExceptions(String message, Throwable cause){

        super(cause.getLocalizedMessage() + ": " + message, cause);
    }
}
