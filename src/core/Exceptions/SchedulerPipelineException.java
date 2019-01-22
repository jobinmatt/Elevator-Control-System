//****************************************************************************
//
// Filename: SchedulerPipelineException.java
//
// Description: SchedulerPipeline Exception class
//
//***************************************************************************

package core.Exceptions;

public class SchedulerPipelineException extends GeneralException {

	private static final long serialVersionUID = -475259545335704813L;

	public SchedulerPipelineException(String message) {

		super(message);
	}

	public SchedulerPipelineException(Throwable cause) {

		super(cause);
	}

	public SchedulerPipelineException(String message, Throwable cause) {

		super(message, cause);
	}
}
