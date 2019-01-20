package core.Exceptions;

/**
 * Exceptions thrown by HostActions
 */
public class HostActionsException extends GeneralExceptions {

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
