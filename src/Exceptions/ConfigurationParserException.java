//****************************************************************************
//
// Filename: ConfigurationParserException.java
//
// Description: Generic Exception to throw during configuration file parsing
//
// Copyright 2018 SafeNet. All rights reserved.
//
// All rights reserved. This file contains information that is
// proprietary to SafeNet and may not be distributed
// or copied without written consent from SafeNet.
//
//***************************************************************************

package Exceptions;

public class ConfigurationParserException extends GeneralExceptions {

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
