//****************************************************************************
//
// Filename: SchedulerSubsystemException.java
//
// Description: Scheduler Subsystem Exception class
//
//***************************************************************************
package core.Exceptions;

public class SchedulerSubsystemException extends GeneralException{

	private static final long serialVersionUID = 9164088387618893432L;

	public SchedulerSubsystemException(String message) {

		super(message);
	}

	public SchedulerSubsystemException(Throwable cause) {

		super(cause);
	}

	public SchedulerSubsystemException(String message, Throwable cause) {

		super(message, cause);
	}
}
