package core.Exceptions;

/**
 * Exceptions thrown by HostActions
 */
public class HostActionsException extends GeneralExceptions {

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
